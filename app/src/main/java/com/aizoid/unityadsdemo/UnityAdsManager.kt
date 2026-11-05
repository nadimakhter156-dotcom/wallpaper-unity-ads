package com.aizoid.unityadsdemo

import android.app.Activity
import android.content.Context
import android.util.Log
import com.aizoid.unityadsdemo.data.AdStatsRepository
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

sealed class SdkStatus {
    object Uninitialized : SdkStatus()
    object Initializing : SdkStatus()
    object Initialized : SdkStatus()
    data class Failed(val error: UnityAds.UnityAdsInitializationError?, val message: String) : SdkStatus()
}

sealed class AdPlacementState {
    object NotLoaded : AdPlacementState()
    object Loading : AdPlacementState()
    object Ready : AdPlacementState()
    object Showing : AdPlacementState()
    data class Error(val code: String, val message: String) : AdPlacementState()
}

sealed class UserUiMessage {
    data class Success(val message: String) : UserUiMessage()
    data class Info(val message: String) : UserUiMessage()
    data class Warning(val message: String) : UserUiMessage()
    data class Error(val message: String) : UserUiMessage()
}

class UnityAdsManager(
    private val context: Context,
    private val adStatsRepository: AdStatsRepository,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "UnityAdsManager"
        private const val RETRY_DELAY_MS = 5000L
        private const val PENDING_SHOW_TIMEOUT_MS = 30000L
    }

    private val _sdkStatus = MutableStateFlow<SdkStatus>(SdkStatus.Uninitialized)
    val sdkStatus: StateFlow<SdkStatus> = _sdkStatus.asStateFlow()

    private val _rewardedState = MutableStateFlow<AdPlacementState>(AdPlacementState.NotLoaded)
    val rewardedState: StateFlow<AdPlacementState> = _rewardedState.asStateFlow()

    private val _interstitialState = MutableStateFlow<AdPlacementState>(AdPlacementState.NotLoaded)
    val interstitialState: StateFlow<AdPlacementState> = _interstitialState.asStateFlow()

    private val _lastRawError = MutableStateFlow<String?>("No errors recorded yet")
    val lastRawError: StateFlow<String?> = _lastRawError.asStateFlow()

    private val _userMessages = MutableSharedFlow<UserUiMessage>(extraBufferCapacity = 16)
    val userMessages: SharedFlow<UserUiMessage> = _userMessages.asSharedFlow()

    // Track pending ad clicks while ad is loading
    private val _isPendingRewarded = MutableStateFlow(false)
    val isPendingRewarded: StateFlow<Boolean> = _isPendingRewarded.asStateFlow()

    private val _isPendingInterstitial = MutableStateFlow(false)
    val isPendingInterstitial: StateFlow<Boolean> = _isPendingInterstitial.asStateFlow()

    private var pendingRewardedJob: Job? = null
    private var pendingInterstitialJob: Job? = null
    private var activityRef: WeakReference<Activity>? = null

    private var retryRewardedJob: Job? = null
    private var retryInterstitialJob: Job? = null

    fun initialize(activity: Activity) {
        activityRef = WeakReference(activity)
        if (_sdkStatus.value is SdkStatus.Initializing || _sdkStatus.value is SdkStatus.Initialized) {
            return
        }

        _sdkStatus.value = SdkStatus.Initializing
        Log.d(TAG, "Initializing Unity Ads with Game ID: $UNITY_GAME_ID, TestMode: $TEST_MODE")

        UnityAds.initialize(
            context.applicationContext,
            UNITY_GAME_ID,
            TEST_MODE,
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    Log.d(TAG, "Unity Ads Initialization Complete")
                    _sdkStatus.value = SdkStatus.Initialized
                    scope.launch {
                        _userMessages.emit(UserUiMessage.Info("Unity Ads Initialized"))
                    }
                    // Auto load both placements after init success
                    loadRewardedAd()
                    loadInterstitialAd()
                }

                override fun onInitializationFailed(
                    error: UnityAds.UnityAdsInitializationError,
                    message: String
                ) {
                    val rawMsg = "InitFailed: $error - $message"
                    Log.e(TAG, rawMsg)
                    _sdkStatus.value = SdkStatus.Failed(error, message)
                    _lastRawError.value = rawMsg
                    scope.launch {
                        _userMessages.emit(UserUiMessage.Error("Init failed: $message"))
                    }
                }
            }
        )
    }

    fun reloadAllAds() {
        Log.d(TAG, "Manually reloading ads...")
        loadRewardedAd()
        loadInterstitialAd()
    }

    fun loadRewardedAd() {
        retryRewardedJob?.cancel()
        _rewardedState.value = AdPlacementState.Loading
        Log.d(TAG, "Loading Rewarded Ad: $REWARDED_PLACEMENT_ID")

        UnityAds.load(REWARDED_PLACEMENT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                Log.d(TAG, "Rewarded Ad Loaded successfully for: $placementId")
                _rewardedState.value = AdPlacementState.Ready
                scope.launch {
                    adStatsRepository.recordLoadSuccess()
                }

                // If user was waiting on button tap, trigger ad immediately
                if (_isPendingRewarded.value) {
                    pendingRewardedJob?.cancel()
                    _isPendingRewarded.value = false
                    val activity = activityRef?.get()
                    if (activity != null) {
                        showRewardedAd(activity)
                    }
                }
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String,
                error: UnityAds.UnityAdsLoadError,
                message: String
            ) {
                val rawMsg = "LoadFailed ($placementId): $error - $message"
                Log.e(TAG, rawMsg)
                _rewardedState.value = AdPlacementState.Error(error.name, message)
                _lastRawError.value = rawMsg
                scope.launch {
                    adStatsRepository.recordLoadFailed()
                }

                // Retry after 5 seconds repeatedly
                retryRewardedJob = scope.launch {
                    delay(RETRY_DELAY_MS)
                    Log.d(TAG, "Retrying Rewarded Ad load...")
                    loadRewardedAd()
                }
            }
        })
    }

    fun loadInterstitialAd() {
        retryInterstitialJob?.cancel()
        _interstitialState.value = AdPlacementState.Loading
        Log.d(TAG, "Loading Interstitial Ad: $INTERSTITIAL_PLACEMENT_ID")

        UnityAds.load(INTERSTITIAL_PLACEMENT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                Log.d(TAG, "Interstitial Ad Loaded successfully for: $placementId")
                _interstitialState.value = AdPlacementState.Ready
                scope.launch {
                    adStatsRepository.recordLoadSuccess()
                }

                // If user was waiting on button tap, trigger ad immediately
                if (_isPendingInterstitial.value) {
                    pendingInterstitialJob?.cancel()
                    _isPendingInterstitial.value = false
                    val activity = activityRef?.get()
                    if (activity != null) {
                        showInterstitialAd(activity)
                    }
                }
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String,
                error: UnityAds.UnityAdsLoadError,
                message: String
            ) {
                val rawMsg = "LoadFailed ($placementId): $error - $message"
                Log.e(TAG, rawMsg)
                _interstitialState.value = AdPlacementState.Error(error.name, message)
                _lastRawError.value = rawMsg
                scope.launch {
                    adStatsRepository.recordLoadFailed()
                }

                // Retry after 5 seconds repeatedly
                retryInterstitialJob = scope.launch {
                    delay(RETRY_DELAY_MS)
                    Log.d(TAG, "Retrying Interstitial Ad load...")
                    loadInterstitialAd()
                }
            }
        })
    }

    fun onRewardedButtonClicked(activity: Activity) {
        activityRef = WeakReference(activity)
        if (_isPendingRewarded.value) {
            return
        }

        if (_rewardedState.value is AdPlacementState.Ready) {
            showRewardedAd(activity)
            return
        }

        _isPendingRewarded.value = true
        if (_rewardedState.value !is AdPlacementState.Loading) {
            loadRewardedAd()
        }

        pendingRewardedJob?.cancel()
        pendingRewardedJob = scope.launch {
            delay(PENDING_SHOW_TIMEOUT_MS)
            if (_isPendingRewarded.value) {
                _isPendingRewarded.value = false
                _userMessages.emit(UserUiMessage.Warning("No ad available right now, please try again"))
            }
        }
    }

    fun onInterstitialButtonClicked(activity: Activity) {
        activityRef = WeakReference(activity)
        if (_isPendingInterstitial.value) {
            return
        }

        if (_interstitialState.value is AdPlacementState.Ready) {
            showInterstitialAd(activity)
            return
        }

        _isPendingInterstitial.value = true
        if (_interstitialState.value !is AdPlacementState.Loading) {
            loadInterstitialAd()
        }

        pendingInterstitialJob?.cancel()
        pendingInterstitialJob = scope.launch {
            delay(PENDING_SHOW_TIMEOUT_MS)
            if (_isPendingInterstitial.value) {
                _isPendingInterstitial.value = false
                _userMessages.emit(UserUiMessage.Warning("No ad available right now, please try again"))
            }
        }
    }

    private fun showRewardedAd(activity: Activity) {
        _rewardedState.value = AdPlacementState.Showing
        Log.d(TAG, "Showing Rewarded Ad...")

        UnityAds.show(
            activity,
            REWARDED_PLACEMENT_ID,
            UnityAdsShowOptions(),
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(
                    placementId: String,
                    error: UnityAds.UnityAdsShowError,
                    message: String
                ) {
                    val rawMsg = "ShowFailed ($placementId): $error - $message"
                    Log.e(TAG, rawMsg)
                    _rewardedState.value = AdPlacementState.Error(error.name, message)
                    _lastRawError.value = rawMsg
                    scope.launch {
                        _userMessages.emit(UserUiMessage.Error("Ad failed to show: $message"))
                    }
                    loadRewardedAd()
                }

                override fun onUnityAdsShowStart(placementId: String) {
                    Log.d(TAG, "Rewarded Ad playback started: $placementId")
                }

                override fun onUnityAdsShowClick(placementId: String) {
                    Log.d(TAG, "Rewarded Ad clicked: $placementId")
                    scope.launch {
                        adStatsRepository.recordClick()
                    }
                }

                override fun onUnityAdsShowComplete(
                    placementId: String,
                    state: UnityAds.UnityAdsShowCompletionState
                ) {
                    Log.d(TAG, "Rewarded Ad show complete: $state")
                    scope.launch {
                        if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                            adStatsRepository.recordRewardedCompleted(REWARD_COINS_PER_AD)
                            _userMessages.emit(
                                UserUiMessage.Success("🎉 Congratulations! +$REWARD_COINS_PER_AD Coins Added!")
                            )
                        } else {
                            adStatsRepository.recordSkipped()
                            _userMessages.emit(
                                UserUiMessage.Info("Ad skipped: No reward granted.")
                            )
                        }
                    }
                    loadRewardedAd()
                }
            }
        )
    }

    private fun showInterstitialAd(activity: Activity) {
        _interstitialState.value = AdPlacementState.Showing
        Log.d(TAG, "Showing Interstitial Ad...")

        UnityAds.show(
            activity,
            INTERSTITIAL_PLACEMENT_ID,
            UnityAdsShowOptions(),
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(
                    placementId: String,
                    error: UnityAds.UnityAdsShowError,
                    message: String
                ) {
                    val rawMsg = "ShowFailed ($placementId): $error - $message"
                    Log.e(TAG, rawMsg)
                    _interstitialState.value = AdPlacementState.Error(error.name, message)
                    _lastRawError.value = rawMsg
                    scope.launch {
                        _userMessages.emit(UserUiMessage.Error("Interstitial failed to show: $message"))
                    }
                    loadInterstitialAd()
                }

                override fun onUnityAdsShowStart(placementId: String) {
                    Log.d(TAG, "Interstitial Ad playback started: $placementId")
                }

                override fun onUnityAdsShowClick(placementId: String) {
                    Log.d(TAG, "Interstitial Ad clicked: $placementId")
                    scope.launch {
                        adStatsRepository.recordClick()
                    }
                }

                override fun onUnityAdsShowComplete(
                    placementId: String,
                    state: UnityAds.UnityAdsShowCompletionState
                ) {
                    Log.d(TAG, "Interstitial Ad completed/closed with state: $state")
                    scope.launch {
                        if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                            adStatsRepository.recordInterstitialShown()
                        } else {
                            adStatsRepository.recordSkipped()
                        }
                    }
                    loadInterstitialAd()
                }
            }
        )
    }
}
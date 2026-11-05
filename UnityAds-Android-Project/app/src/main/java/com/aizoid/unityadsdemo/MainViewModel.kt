package com.aizoid.unityadsdemo

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aizoid.unityadsdemo.data.AdStats
import com.aizoid.unityadsdemo.data.AdStatsRepository
import com.aizoid.unityadsdemo.data.NetworkLocationInfo
import com.aizoid.unityadsdemo.data.NetworkLocationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val adStatsRepository = AdStatsRepository(application)
    private val networkLocationService = NetworkLocationService()

    val adsManager = UnityAdsManager(
        context = application,
        adStatsRepository = adStatsRepository,
        scope = viewModelScope
    )

    val adStats: StateFlow<AdStats> = adStatsRepository.adStatsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdStats()
    )

    private val _networkInfo = MutableStateFlow(NetworkLocationInfo())
    val networkInfo: StateFlow<NetworkLocationInfo> = _networkInfo.asStateFlow()

    private val _isLoadingNetwork = MutableStateFlow(false)
    val isLoadingNetwork: StateFlow<Boolean> = _isLoadingNetwork.asStateFlow()

    private val _sessionSeconds = MutableStateFlow(0L)
    val sessionSeconds: StateFlow<Long> = _sessionSeconds.asStateFlow()

    private val _showResetDialog = MutableStateFlow(false)
    val showResetDialog: StateFlow<Boolean> = _showResetDialog.asStateFlow()

    private val _confettiTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val confettiTrigger: SharedFlow<Unit> = _confettiTrigger.asSharedFlow()

    private var timerJob: Job? = null

    init {
        startSessionTimer()
        fetchNetworkInfo()
        listenToRewardEvents()
    }

    fun initializeAds(activity: Activity) {
        adsManager.initialize(activity)
    }

    private fun startSessionTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _sessionSeconds.value += 1
            }
        }
    }

    private fun listenToRewardEvents() {
        viewModelScope.launch {
            adsManager.userMessages.collect { msg ->
                if (msg is UserUiMessage.Success) {
                    _confettiTrigger.emit(Unit)
                }
            }
        }
    }

    fun fetchNetworkInfo() {
        viewModelScope.launch {
            _isLoadingNetwork.value = true
            _networkInfo.value = networkLocationService.fetchLocationInfo()
            _isLoadingNetwork.value = false
        }
    }

    fun onRewardedClick(activity: Activity) {
        adsManager.onRewardedButtonClicked(activity)
    }

    fun onInterstitialClick(activity: Activity) {
        adsManager.onInterstitialButtonClicked(activity)
    }

    fun reloadAds() {
        adsManager.reloadAllAds()
    }

    fun openResetDialog() {
        _showResetDialog.value = true
    }

    fun dismissResetDialog() {
        _showResetDialog.value = false
    }

    fun confirmResetStats() {
        _showResetDialog.value = false
        viewModelScope.launch {
            adStatsRepository.resetAllStats()
        }
    }
}
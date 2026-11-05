package com.aizoid.unityadsdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.aizoid.unityadsdemo.ui.components.ActionButtons
import com.aizoid.unityadsdemo.ui.components.AdPerformanceCard
import com.aizoid.unityadsdemo.ui.components.AdsEngineStatusCard
import com.aizoid.unityadsdemo.ui.components.AppFooter
import com.aizoid.unityadsdemo.ui.components.CoinBalanceCard
import com.aizoid.unityadsdemo.ui.components.CoinConfetti
import com.aizoid.unityadsdemo.ui.components.DeviceSessionCard
import com.aizoid.unityadsdemo.ui.components.HeaderCard
import com.aizoid.unityadsdemo.ui.components.NetworkLocationCard
import com.aizoid.unityadsdemo.ui.components.ResetStatsDialog
import com.aizoid.unityadsdemo.ui.theme.DarkBackgroundEnd
import com.aizoid.unityadsdemo.ui.theme.DarkBackgroundStart
import com.aizoid.unityadsdemo.ui.theme.UnityAdsDemoTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.initializeAds(this)

        setContent {
            UnityAdsDemoTheme {
                MainScreen(
                    viewModel = viewModel,
                    onWatchRewarded = { viewModel.onRewardedClick(this) },
                    onShowInterstitial = { viewModel.onInterstitialClick(this) }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onWatchRewarded: () -> Unit,
    onShowInterstitial: () -> Unit
) {
    val adStats by viewModel.adStats.collectAsState()
    val sdkStatus by viewModel.adsManager.sdkStatus.collectAsState()
    val rewardedState by viewModel.adsManager.rewardedState.collectAsState()
    val interstitialState by viewModel.adsManager.interstitialState.collectAsState()
    val lastRawError by viewModel.adsManager.lastRawError.collectAsState()
    val isPendingRewarded by viewModel.adsManager.isPendingRewarded.collectAsState()
    val isPendingInterstitial by viewModel.adsManager.isPendingInterstitial.collectAsState()

    val networkInfo by viewModel.networkInfo.collectAsState()
    val isLoadingNetwork by viewModel.isLoadingNetwork.collectAsState()
    val sessionSeconds by viewModel.sessionSeconds.collectAsState()
    val showResetDialog by viewModel.showResetDialog.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.adsManager.userMessages.collectLatest { message ->
            val text = when (message) {
                is UserUiMessage.Success -> message.message
                is UserUiMessage.Info -> message.message
                is UserUiMessage.Warning -> message.message
                is UserUiMessage.Error -> message.message
            }
            snackbarHostState.showSnackbar(text)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.confettiTrigger.collectLatest {
            showConfetti = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkBackgroundStart, DarkBackgroundEnd)
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                HeaderCard()
                CoinBalanceCard(coins = adStats.totalCoins)
                ActionButtons(
                    isPendingRewarded = isPendingRewarded,
                    isPendingInterstitial = isPendingInterstitial,
                    onWatchRewardedClick = onWatchRewarded,
                    onShowInterstitialClick = onShowInterstitial
                )
                AdsEngineStatusCard(
                    sdkStatus = sdkStatus,
                    rewardedState = rewardedState,
                    interstitialState = interstitialState,
                    lastRawError = lastRawError,
                    onReloadAds = { viewModel.reloadAds() }
                )
                NetworkLocationCard(
                    networkInfo = networkInfo,
                    isLoading = isLoadingNetwork,
                    onRefresh = { viewModel.fetchNetworkInfo() }
                )
                AdPerformanceCard(
                    stats = adStats,
                    onResetClick = { viewModel.openResetDialog() }
                )
                DeviceSessionCard(
                    sessionSeconds = sessionSeconds,
                    sdkStatus = sdkStatus
                )
                AppFooter()
                Spacer(modifier = Modifier.height(16.dp))
            }

            CoinConfetti(
                trigger = showConfetti,
                onFinished = { showConfetti = false }
            )

            ResetStatsDialog(
                isOpen = showResetDialog,
                onConfirm = { viewModel.confirmResetStats() },
                onDismiss = { viewModel.dismissResetDialog() }
            )
        }
    }
}
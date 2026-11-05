package com.aizoid.unityadsdemo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aizoid.unityadsdemo.AdPlacementState
import com.aizoid.unityadsdemo.SdkStatus
import com.aizoid.unityadsdemo.ui.theme.*

@Composable
fun AdsEngineStatusCard(
    sdkStatus: SdkStatus,
    rewardedState: AdPlacementState,
    interstitialState: AdPlacementState,
    lastRawError: String?,
    onReloadAds: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (sdkStatus) {
                                    is SdkStatus.Initialized -> StatusGreen
                                    is SdkStatus.Initializing -> StatusYellow
                                    is SdkStatus.Failed -> StatusRed
                                    else -> TextMuted
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ads Engine Status",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = onReloadAds,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceCardInner,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Reload Ads", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCardInner)
                    .padding(14.dp)
            ) {
                StatusRow(
                    label = "Unity SDK Status",
                    value = when (sdkStatus) {
                        is SdkStatus.Initialized -> "Initialized (Production Mode)"
                        is SdkStatus.Initializing -> "Initializing..."
                        is SdkStatus.Failed -> "Initialization Failed"
                        is SdkStatus.Uninitialized -> "Uninitialized"
                    },
                    statusColor = when (sdkStatus) {
                        is SdkStatus.Initialized -> StatusGreen
                        is SdkStatus.Initializing -> StatusYellow
                        is SdkStatus.Failed -> StatusRed
                        else -> TextMuted
                    }
                )

                StatusRow(
                    label = "Rewarded Placement",
                    value = formatAdState(rewardedState),
                    statusColor = getAdStateColor(rewardedState)
                )

                StatusRow(
                    label = "Interstitial Placement",
                    value = formatAdState(interstitialState),
                    statusColor = getAdStateColor(interstitialState)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "RAW SDK TELEMETRY / ERROR LOG",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xE6080812))
                    .border(1.dp, Color(0x33EF4444), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = lastRawError?.takeIf { it.isNotBlank() } ?: "No errors recorded. Listening for Unity callbacks...",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (lastRawError != null && (lastRawError.contains("Failed") || lastRawError.contains("Error"))) {
                        Color(0xFFFCA5A5)
                    } else {
                        Color(0xFF86EFAC)
                    },
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String, statusColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}

private fun formatAdState(state: AdPlacementState): String {
    return when (state) {
        is AdPlacementState.Ready -> "Ready to Show"
        is AdPlacementState.Loading -> "Loading Ad..."
        is AdPlacementState.Showing -> "Playing on Screen"
        is AdPlacementState.NotLoaded -> "Not Loaded"
        is AdPlacementState.Error -> "Failed: ${state.code}"
    }
}

private fun getAdStateColor(state: AdPlacementState): Color {
    return when (state) {
        is AdPlacementState.Ready -> StatusGreen
        is AdPlacementState.Loading -> StatusYellow
        is AdPlacementState.Showing -> Color(0xFF60A5FA)
        is AdPlacementState.NotLoaded -> TextMuted
        is AdPlacementState.Error -> StatusRed
    }
}
package com.aizoid.unityadsdemo.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aizoid.unityadsdemo.data.AdStats
import com.aizoid.unityadsdemo.ui.theme.*
import java.util.Locale

@Composable
fun AdPerformanceCard(
    stats: AdStats,
    onResetClick: () -> Unit,
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
                Column {
                    Text(
                        text = "Ad Performance & Telemetry",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Persistent DataStore real metrics",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = onResetClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x33EF4444),
                        contentColor = StatusRed
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Reset",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Reset Stats", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricTile(
                        title = "Total Ad Clicks",
                        value = stats.totalClicks,
                        icon = Icons.Default.Mouse,
                        gradient = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        title = "CTR Rate",
                        customValue = String.format(Locale.US, "%.1f%%", stats.ctrPercent),
                        icon = Icons.Default.Percent,
                        gradient = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricTile(
                        title = "Rewarded Watched",
                        value = stats.rewardedWatched,
                        icon = Icons.Default.Visibility,
                        gradient = listOf(Color(0xFF10B981), Color(0xFF047857)),
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        title = "Interstitials Shown",
                        value = stats.interstitialsShown,
                        icon = Icons.Default.Tv,
                        gradient = listOf(Color(0xFF06B6D4), Color(0xFF0E7490)),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricTile(
                        title = "Ads Skipped",
                        value = stats.adsSkipped,
                        icon = Icons.Default.SkipNext,
                        gradient = listOf(Color(0xFFF59E0B), Color(0xFFB45309)),
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        title = "Coins Earned",
                        value = stats.totalCoins,
                        icon = Icons.Default.MonetizationOn,
                        gradient = listOf(Color(0xFFEAB308), Color(0xFFA16207)),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricTile(
                        title = "Load Success",
                        value = stats.loadSuccessCount,
                        icon = Icons.Default.CheckCircle,
                        gradient = listOf(Color(0xFF059669), Color(0xFF065F46)),
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        title = "Load Failed",
                        value = stats.loadFailedCount,
                        icon = Icons.Default.Close,
                        gradient = listOf(Color(0xFFE11D48), Color(0xFF9F1239)),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0x3310B981), Color(0x3306B6D4))
                        )
                    )
                    .border(1.dp, Color(0x6610B981), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x3310B981))
                                .border(1.dp, Color(0xFF34D399), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = "Earnings",
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Estimated Ad Revenue",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format(Locale.US, "$%.4f USD", stats.estimatedEarningsUsd),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6EE7B7)
                            )
                        }
                    }

                    Text(
                        text = "eCPM ~$2.00",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Estimated (check Unity Dashboard for real earnings)",
                fontSize = 11.sp,
                color = TextMuted,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: Int = 0,
    customValue: String? = null,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    val animatedCount by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "MetricCount"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(gradient.map { it.copy(alpha = 0.28f) }))
            .border(1.dp, gradient.first().copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, color = TextSecondary, maxLines = 1)
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = gradient.first(),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = customValue ?: animatedCount.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}
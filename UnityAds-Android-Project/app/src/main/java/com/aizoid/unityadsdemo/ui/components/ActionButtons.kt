package com.aizoid.unityadsdemo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aizoid.unityadsdemo.ui.theme.*

@Composable
fun ActionButtons(
    isPendingRewarded: Boolean,
    isPendingInterstitial: Boolean,
    onWatchRewardedClick: () -> Unit,
    onShowInterstitialClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ButtonAnimations")

    val giftRotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GiftWiggle"
    )

    val tvBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TvBounce"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BigAdButton(
            title = if (isPendingRewarded) "Loading ad..." else "Watch Rewarded Ad (+10 Coins)",
            subtitle = if (isPendingRewarded) "Will play automatically once loaded" else "Guaranteed coin reward on full completion",
            icon = Icons.Default.CardGiftcard,
            iconTint = GoldCoin,
            iconModifier = Modifier.graphicsLayer { rotationZ = giftRotation },
            gradientColors = listOf(Color(0xFF9333EA), Color(0xFF6B21A8), Color(0xFF4C1D95)),
            glowColor = Color(0xFFA855F7).copy(alpha = glowAlpha),
            isLoading = isPendingRewarded,
            onClick = onWatchRewardedClick
        )

        BigAdButton(
            title = if (isPendingInterstitial) "Loading ad..." else "Show Interstitial Ad",
            subtitle = if (isPendingInterstitial) "Will play automatically once loaded" else "Full screen brand showcase ad",
            icon = Icons.Default.Tv,
            iconTint = Color(0xFF38BDF8),
            iconModifier = Modifier.graphicsLayer { translationY = tvBounce },
            gradientColors = listOf(Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF075985)),
            glowColor = Color(0xFF0284C7).copy(alpha = glowAlpha),
            isLoading = isPendingInterstitial,
            onClick = onShowInterstitialClick
        )
    }
}

@Composable
private fun BigAdButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconModifier: Modifier = Modifier,
    gradientColors: List<Color>,
    glowColor: Color,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "ButtonScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.horizontalGradient(gradientColors))
            .border(1.5.dp, Color(0x66FFFFFF), RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x33000000))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier
                            .size(26.dp)
                            .then(iconModifier)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xCCF8FAFC)
                )
            }
        }
    }
}
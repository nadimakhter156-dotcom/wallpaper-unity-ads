package com.aizoid.unityadsdemo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.aizoid.unityadsdemo.ui.theme.GoldCoin
import kotlin.random.Random

private data class Particle(
    val initialX: Float,
    val initialY: Float,
    val targetX: Float,
    val targetY: Float,
    val size: Float,
    val color: Color
)

@Composable
fun CoinConfetti(
    trigger: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!trigger) return

    val progress = remember { Animatable(0f) }
    val particles = remember {
        val colors = listOf(
            GoldCoin,
            Color(0xFFFFB703),
            Color(0xFFFB8500),
            Color(0xFF38BDF8),
            Color(0xFFA855F7),
            Color(0xFF34D399)
        )
        List(40) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val distance = Random.nextFloat() * 450f + 150f
            Particle(
                initialX = 0f,
                initialY = 0f,
                targetX = (kotlin.math.cos(angle) * distance),
                targetY = (kotlin.math.sin(angle) * distance) + 120f,
                size = Random.nextFloat() * 12f + 8f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = FastOutLinearInEasing)
        )
        onFinished()
    }

    val currentProgress = progress.value
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height * 0.35f

        particles.forEach { particle ->
            val x = centerX + particle.targetX * currentProgress
            val y = centerY + particle.targetY * currentProgress
            val alpha = (1f - currentProgress).coerceIn(0f, 1f)

            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.size * (1f - (currentProgress * 0.4f)),
                center = Offset(x, y)
            )
        }
    }
}
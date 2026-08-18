package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.model.PlaybackState
import com.example.ui.theme.ImmersiveAccent
import com.example.ui.theme.ImmersiveAccentGlow
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveSurfaceVariant

@Composable
fun WaveformVisualizer(
    playbackState: PlaybackState,
    amplitudes: List<Float>,
    modifier: Modifier = Modifier
) {
    val isPlaying = playbackState == PlaybackState.PLAYING
    val isBuffering = playbackState == PlaybackState.BUFFERING

    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_phase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ImmersiveSurfaceVariant)
            .border(BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.6f)), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val barCount = 18
            for (i in 0 until barCount) {
                val baseAmp = if (i < amplitudes.size) amplitudes[i] else 0.2f
                val heightFraction = when {
                    isPlaying -> {
                        val staggered = (baseAmp * ((i % 5 + 1) * 0.22f + 0.45f)).coerceIn(0.15f, 1.0f)
                        staggered
                    }
                    isBuffering -> {
                        val offset = (i.toFloat() / barCount)
                        val wave = (kotlin.math.sin((pulsePhase * 6.28f) + (offset * 3.14f)) + 1f) / 2f
                        0.2f + (wave * 0.6f)
                    }
                    else -> 0.12f
                }

                val barHeight = (heightFraction * 36).dp.coerceAtLeast(4.dp)

                val barBrush = Brush.verticalGradient(
                    colors = if (isPlaying) {
                        listOf(ImmersiveAccentGlow, ImmersiveAccent, ImmersiveAccent.copy(alpha = 0.7f))
                    } else if (isBuffering) {
                        listOf(ImmersiveAccent.copy(alpha = 0.6f), ImmersiveAccent.copy(alpha = 0.3f))
                    } else {
                        listOf(ImmersiveBorder, ImmersiveBorder.copy(alpha = 0.5f))
                    }
                )

                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(2.dp))
                        .background(barBrush)
                )
            }
        }
    }
}

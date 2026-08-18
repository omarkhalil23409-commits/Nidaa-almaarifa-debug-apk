package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FrequencyChannel
import com.example.model.PlaybackState
import com.example.ui.theme.ImmersiveAccent
import com.example.ui.theme.ImmersiveAccentContainer
import com.example.ui.theme.ImmersiveAccentGlow
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveDialCenter
import com.example.ui.theme.ImmersiveDialDark
import com.example.ui.theme.ImmersiveLiveRed
import com.example.ui.theme.ImmersiveOnAccent
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceVariant
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.theme.ImmersiveTextTertiary

@Composable
fun RadioPlayerCard(
    playbackState: PlaybackState,
    currentChannel: FrequencyChannel,
    audioAmplitudes: List<Float>,
    volume: Float,
    noteDegree: Float = 0.05f,
    sleepTimerMinutes: Int?,
    errorMessage: String?,
    onTogglePlay: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onNoteDegreeChange: (Float) -> Unit = {},
    onIncrementNote: () -> Unit = {},
    onDecrementNote: () -> Unit = {},
    onResetNote: () -> Unit = {},
    onSetSleepTimer: (Int) -> Unit,
    onCancelSleepTimer: () -> Unit,
    onPreviousChannel: (() -> Unit)? = null,
    onNextChannel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    val isPlaying = playbackState == PlaybackState.PLAYING
    val isBuffering = playbackState == PlaybackState.BUFFERING
    val isRadioOn = isPlaying || isBuffering

    // Dial rotation animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "dial_anim")
    val dialRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val livePulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("radio_player_card"),
        shape = RoundedCornerShape(28.dp),
        color = ImmersiveSurface,
        border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.6f)),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Top Status Bar: Live badge, Radio Power switch, & Sleep timer button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live / Power Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isPlaying) ImmersiveLiveRed.copy(alpha = 0.15f)
                            else if (isBuffering) ImmersiveAccent.copy(alpha = 0.15f)
                            else ImmersiveSurfaceVariant
                        )
                        .border(
                            1.dp,
                            if (isPlaying) ImmersiveLiveRed.copy(alpha = 0.5f)
                            else if (isBuffering) ImmersiveAccent.copy(alpha = 0.5f)
                            else ImmersiveBorder.copy(alpha = 0.4f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(if (isPlaying) livePulseScale else 1.0f)
                            .clip(CircleShape)
                            .background(
                                if (isPlaying) ImmersiveLiveRed
                                else if (isBuffering) ImmersiveAccent
                                else ImmersiveTextTertiary
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlaying) "بث حي مباشر" else if (isBuffering) "جارٍ الاتصال..." else "الراديو متوقف",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaying) ImmersiveLiveRed else if (isBuffering) ImmersiveAccent else ImmersiveTextSecondary
                        )
                    )
                }

                // Power Toggle Button (تشغيل / إيقاف الراديو)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isRadioOn) ImmersiveAccent.copy(alpha = 0.18f) else ImmersiveSurfaceVariant,
                    border = BorderStroke(1.dp, if (isRadioOn) ImmersiveAccent else ImmersiveBorder.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onTogglePlay)
                        .testTag("radio_power_toggle_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = if (isRadioOn) "إيقاف الراديو" else "تشغيل الراديو",
                            tint = if (isRadioOn) ImmersiveAccent else ImmersiveTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRadioOn) "إيقاف الراديو" else "تشغيل الراديو",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isRadioOn) ImmersiveAccent else ImmersiveTextSecondary
                            )
                        )
                    }
                }

                // Sleep timer button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (sleepTimerMinutes != null) ImmersiveAccent.copy(alpha = 0.2f) else ImmersiveSurfaceVariant,
                    border = BorderStroke(1.dp, if (sleepTimerMinutes != null) ImmersiveAccent else ImmersiveBorder.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { showSleepTimerDialog = true }
                        .testTag("sleep_timer_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "مؤقت النوم",
                            tint = if (sleepTimerMinutes != null) ImmersiveAccent else ImmersiveTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (sleepTimerMinutes != null) "$sleepTimerMinutes د" else "مؤقت",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (sleepTimerMinutes != null) ImmersiveAccent else ImmersiveTextSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Immersive Circular Tuner Dial (Hero element)
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ImmersiveDialCenter.copy(alpha = 0.85f),
                                ImmersiveDialDark,
                                ImmersiveSurfaceVariant
                            )
                        )
                    )
                    .border(BorderStroke(4.dp, ImmersiveBorder), CircleShape)
                    .clickable(onClick = onTogglePlay),
                contentAlignment = Alignment.Center
            ) {
                // Background subtle ring glow when playing
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.92f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(ImmersiveAccent.copy(alpha = 0.18f), Color.Transparent)
                                )
                            )
                    )
                }

                // Rotating dial tick ring indicator
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(if (isPlaying) dialRotation else 0f)
                ) {
                    // Top dial tick marks around the perimeter
                    for (angle in 0 until 12) {
                        val deg = angle * 30f
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(deg),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .size(width = 2.dp, height = if (angle % 3 == 0) 8.dp else 4.dp)
                                    .background(
                                        if (angle % 3 == 0) (if (isRadioOn) ImmersiveAccent.copy(alpha = 0.8f) else ImmersiveBorder)
                                        else ImmersiveBorder.copy(alpha = 0.5f)
                                    )
                            )
                        }
                    }
                }

                // Glowing Needle indicator at the top
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 4.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isRadioOn) ImmersiveAccent else ImmersiveTextTertiary)
                            .shadow(if (isRadioOn) 8.dp else 0.dp, spotColor = ImmersiveAccent)
                    )
                }

                // Center Frequency and Station Label
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = currentChannel.frequencyMhz.replace(" FM", "").replace(" للبث", ""),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 48.sp,
                            letterSpacing = (-0.5).sp,
                            color = if (isRadioOn) ImmersiveAccent else ImmersiveTextSecondary
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (isRadioOn) "FM STEREO • ${currentChannel.powerKw}" else "وضع الاستعداد",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = if (isRadioOn) ImmersiveTextSecondary else ImmersiveTextTertiary
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isRadioOn) currentChannel.coverageRegion else "اضغط هنا لتشغيل الراديو",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isRadioOn) ImmersiveTextTertiary else ImmersiveAccent,
                            fontSize = 11.sp,
                            fontWeight = if (!isRadioOn) FontWeight.Bold else FontWeight.Normal
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Station Name & Program Title
            Text(
                text = "إذاعة نداء المعرفة",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = ImmersiveTextPrimary
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "صوت الحق والمعرفة من لبنان إلى العالم",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ImmersiveTextSecondary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Waveform Visualizer
            WaveformVisualizer(
                playbackState = playbackState,
                amplitudes = audioAmplitudes
            )

            // Error banner if any
            AnimatedVisibility(visible = errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        IconButton(
                            onClick = onTogglePlay,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "إعادة المحاولة",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Immersive Media Controls: Skip Prev / Big Glowing Play-Stop Power Button / Skip Next
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Channel / Tune Down Button
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(enabled = onPreviousChannel != null) {
                            onPreviousChannel?.invoke()
                        },
                    shape = CircleShape,
                    color = ImmersiveSurfaceVariant,
                    border = BorderStroke(1.dp, ImmersiveBorder)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "التردد السابق",
                            tint = ImmersiveTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Center Hero Play/Pause/Turn-Off Button in Immersive Accent
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Ambient Accent Glow when playing
                    if (isPlaying) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(ImmersiveAccent.copy(alpha = 0.35f), Color.Transparent)
                                    )
                                )
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .clickable(enabled = true, onClick = onTogglePlay)
                            .testTag("main_play_pause_button"),
                        shape = CircleShape,
                        color = if (isPlaying) ImmersiveAccent else if (isBuffering) ImmersiveAccentContainer else ImmersiveAccent,
                        shadowElevation = 10.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (isBuffering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = ImmersiveAccent,
                                    strokeWidth = 3.5.dp
                                )
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "إلغاء وإيقاف الراديو",
                                    tint = ImmersiveAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "إيقاف الراديو" else "تشغيل الراديو",
                                    tint = ImmersiveOnAccent,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }
                    }
                }

                // Next Channel / Tune Up Button
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(enabled = onNextChannel != null) {
                            onNextChannel?.invoke()
                        },
                    shape = CircleShape,
                    color = ImmersiveSurfaceVariant,
                    border = BorderStroke(1.dp, ImmersiveBorder)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "التردد التالي",
                            tint = ImmersiveTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Volume Control Slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeDown,
                    contentDescription = "مستوى الصوت منخفض",
                    tint = ImmersiveTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("volume_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = ImmersiveAccent,
                        activeTrackColor = ImmersiveAccent,
                        inactiveTrackColor = ImmersiveBorder
                    )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "مستوى الصوت عالي",
                    tint = ImmersiveTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Audio Note & Pitch Fine-Tuning Control (درجة النوتة والتنغيم الصوتي)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("note_pitch_control_card"),
                color = ImmersiveSurfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Header: Title and Current Note Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = ImmersiveAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "درجة النوتة (نبرة الصوت)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ImmersiveTextPrimary
                                )
                            )
                        }

                        // Degree Value Badge
                        val noteFormatted = if (noteDegree > 0f) "+%.2f".format(noteDegree) else if (noteDegree < 0f) "%.2f".format(noteDegree) else "0.00"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ImmersiveAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, ImmersiveAccent.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = noteFormatted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ImmersiveAccent
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Slider & Fine Tuning Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDecrementNote,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "خفض النوتة",
                                tint = ImmersiveTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Slider(
                            value = noteDegree,
                            onValueChange = onNoteDegreeChange,
                            valueRange = -0.20f..0.20f,
                            steps = 19,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("note_pitch_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = ImmersiveAccent,
                                activeTrackColor = ImmersiveAccent,
                                inactiveTrackColor = ImmersiveBorder
                            )
                        )

                        IconButton(
                            onClick = onIncrementNote,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "رفع النوتة",
                                tint = ImmersiveAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Quick Note Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf(
                            Pair("0.0 (الأساس)", 0.0f),
                            Pair("+0.05 (أعلى قليلاً)", 0.05f),
                            Pair("+0.10 (تلاوة واضحة)", 0.10f),
                            Pair("+0.15 (رنان)", 0.15f)
                        )

                        presets.forEach { (label, value) ->
                            val isSelected = kotlin.math.abs(noteDegree - value) < 0.02f
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onNoteDegreeChange(value) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) ImmersiveAccent.copy(alpha = 0.2f) else ImmersiveSurface,
                                border = BorderStroke(1.dp, if (isSelected) ImmersiveAccent else ImmersiveBorder.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) ImmersiveAccent else ImmersiveTextSecondary,
                                        textAlign = TextAlign.Center
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            containerColor = ImmersiveSurface,
            titleContentColor = ImmersiveTextPrimary,
            textContentColor = ImmersiveTextSecondary,
            title = {
                Text(
                    text = "مؤقت إيقاف البث تلقائياً",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "اختر المدة المطلوبة لإيقاف البث الإذاعي تلقائياً للنوم أو الاسترخاء:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ImmersiveTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(15, 30, 45, 60, 90).forEach { minutes ->
                        Button(
                            onClick = {
                                onSetSleepTimer(minutes)
                                showSleepTimerDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (sleepTimerMinutes == minutes) ImmersiveAccent else ImmersiveSurfaceVariant,
                                contentColor = if (sleepTimerMinutes == minutes) ImmersiveOnAccent else ImmersiveTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (sleepTimerMinutes == minutes) ImmersiveAccent else ImmersiveBorder)
                        ) {
                            Text(text = "$minutes دقيقة", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (sleepTimerMinutes != null) {
                        OutlinedButton(
                            onClick = {
                                onCancelSleepTimer()
                                showSleepTimerDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ImmersiveLiveRed)
                        ) {
                            Text(text = "إلغاء المؤقت", color = ImmersiveLiveRed)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text(text = "إغلاق", color = ImmersiveAccent)
                }
            }
        )
    }
}

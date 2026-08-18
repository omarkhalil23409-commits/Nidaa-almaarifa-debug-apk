package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.PlaybackState
import com.example.ui.components.AboutContactSection
import com.example.ui.components.FrequenciesGuideSection
import com.example.ui.components.FrequencySelector
import com.example.ui.components.PrayerTimesSection
import com.example.ui.components.ProgramsSection
import com.example.ui.components.RadioPlayerCard
import com.example.ui.theme.ImmersiveAccent
import com.example.ui.theme.ImmersiveAccentContainer
import com.example.ui.theme.ImmersiveBackground
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveLiveRed
import com.example.ui.theme.ImmersiveOnAccent
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceVariant
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.theme.ImmersiveTextTertiary
import com.example.viewmodel.RadioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainRadioScreen(
    viewModel: RadioViewModel,
    modifier: Modifier = Modifier
) {
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val currentChannel by viewModel.currentChannel.collectAsStateWithLifecycle()
    val audioAmplitudes by viewModel.audioAmplitudes.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()
    val noteDegree by viewModel.noteDegree.collectAsStateWithLifecycle()
    val sleepTimerMinutes by viewModel.sleepTimerMinutesLeft.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedCityIndex by viewModel.selectedCityIndex.collectAsStateWithLifecycle()
    val currentTimeString by viewModel.currentTimeString.collectAsStateWithLifecycle()
    val programs by viewModel.programs.collectAsStateWithLifecycle()
    val selectedProgramCategory by viewModel.selectedProgramCategory.collectAsStateWithLifecycle()

    // Force Arabic RTL layout for authentic Islamic broadcast radio experience
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = ImmersiveBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(ImmersiveSurface)
                                    .border(BorderStroke(1.5.dp, ImmersiveAccent), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.nidaa_radio_icon_1786790478314),
                                    contentDescription = "شعار إذاعة نداء المعرفة",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "إذاعة نداء المعرفة",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = ImmersiveTextPrimary
                                    )
                                )
                                Text(
                                    text = "91.1 • 91.3 • 91.5 FM لبنان",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ImmersiveAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    },
                    actions = {
                        val isRadioActive = playbackState == PlaybackState.PLAYING || playbackState == PlaybackState.BUFFERING

                        // Live indicator badge
                        if (playbackState == PlaybackState.PLAYING) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ImmersiveLiveRed.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, ImmersiveLiveRed.copy(alpha = 0.5f)),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(ImmersiveLiveRed)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "مباشر",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ImmersiveLiveRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        // Top Bar Power ON/OFF Button
                        IconButton(
                            onClick = { viewModel.togglePlayback() },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("top_bar_power_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = if (isRadioActive) "إيقاف الراديو" else "تشغيل الراديو",
                                tint = if (isRadioActive) ImmersiveAccent else ImmersiveTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ImmersiveSurfaceVariant
                    )
                )
            },
            bottomBar = {
                Column {
                    // Mini Player Bar (visible when navigating other tabs while playing)
                    if (selectedTab != 0 && (playbackState == PlaybackState.PLAYING || playbackState == PlaybackState.BUFFERING)) {
                        MiniPlayerBar(
                            channel = currentChannel,
                            playbackState = playbackState,
                            onTogglePlay = { viewModel.togglePlayback() },
                            onClick = { viewModel.selectTab(0) }
                        )
                    }

                    // Immersive Bottom Navigation Bar
                    NavigationBar(
                        containerColor = ImmersiveSurfaceVariant,
                        tonalElevation = 6.dp,
                        modifier = Modifier.border(BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.4f)))
                    ) {
                        val navItems = listOf(
                            Triple("البث المباشر", Icons.Default.Radio, 0),
                            Triple("البرامج", Icons.Default.CalendarMonth, 1),
                            Triple("المواقيت", Icons.Default.Mosque, 2),
                            Triple("الترددات", Icons.Default.CellTower, 3),
                            Triple("عن الإذاعة", Icons.Default.Info, 4)
                        )

                        navItems.forEach { (label, icon, index) ->
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = { viewModel.selectTab(index) },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ImmersiveAccent,
                                    selectedTextColor = ImmersiveAccent,
                                    indicatorColor = ImmersiveAccentContainer,
                                    unselectedIconColor = ImmersiveTextSecondary,
                                    unselectedTextColor = ImmersiveTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Live Radio & Frequency Selector Tab
                        item {
                            RadioPlayerCard(
                                playbackState = playbackState,
                                currentChannel = currentChannel,
                                audioAmplitudes = audioAmplitudes,
                                volume = volume,
                                noteDegree = noteDegree,
                                sleepTimerMinutes = sleepTimerMinutes,
                                errorMessage = errorMessage,
                                onTogglePlay = { viewModel.togglePlayback() },
                                onVolumeChange = { viewModel.setVolume(it) },
                                onNoteDegreeChange = { viewModel.setNoteDegree(it) },
                                onIncrementNote = { viewModel.incrementNote() },
                                onDecrementNote = { viewModel.decrementNote() },
                                onResetNote = { viewModel.resetNoteToDefault() },
                                onSetSleepTimer = { viewModel.setSleepTimer(it) },
                                onCancelSleepTimer = { viewModel.cancelSleepTimer() },
                                onPreviousChannel = { viewModel.previousChannel() },
                                onNextChannel = { viewModel.nextChannel() }
                            )
                        }

                        item {
                            FrequencySelector(
                                selectedChannel = currentChannel,
                                onSelectChannel = { viewModel.selectChannel(it) }
                            )
                        }

                        item {
                            PrayerTimesSection(
                                cities = viewModel.lebaneseCities,
                                selectedCityIndex = selectedCityIndex,
                                currentTimeString = currentTimeString,
                                onSelectCity = { viewModel.selectCity(it) }
                            )
                        }
                    }

                    1 -> {
                        // Program Schedule Tab
                        item {
                            ProgramsSection(
                                programs = programs,
                                selectedCategory = selectedProgramCategory,
                                onSelectCategory = { viewModel.filterCategory(it) }
                            )
                        }
                    }

                    2 -> {
                        // Lebanese Prayer Times Tab
                        item {
                            PrayerTimesSection(
                                cities = viewModel.lebaneseCities,
                                selectedCityIndex = selectedCityIndex,
                                currentTimeString = currentTimeString,
                                onSelectCity = { viewModel.selectCity(it) }
                            )
                        }
                    }

                    3 -> {
                        // Frequencies Guide Tab (91.1, 91.3, 91.5 FM details)
                        item {
                            FrequenciesGuideSection()
                        }
                    }

                    4 -> {
                        // About & Contact Tab
                        item {
                            AboutContactSection()
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun MiniPlayerBar(
    channel: com.example.model.FrequencyChannel,
    playbackState: PlaybackState,
    onTogglePlay: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPlaying = playbackState == PlaybackState.PLAYING
    val isBuffering = playbackState == PlaybackState.BUFFERING

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("mini_player_bar"),
        color = ImmersiveSurface,
        border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.6f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ImmersiveAccentContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = ImmersiveAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "إذاعة نداء المعرفة",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveTextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${channel.frequencyMhz} • ${channel.coverageRegion}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ImmersiveAccent
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier.testTag("mini_play_button")
            ) {
                if (isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = ImmersiveAccent,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                        tint = ImmersiveAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

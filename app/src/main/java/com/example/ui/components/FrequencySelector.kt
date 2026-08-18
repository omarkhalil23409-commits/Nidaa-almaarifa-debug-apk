package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FrequencyChannel
import com.example.model.RadioChannels
import com.example.ui.theme.ImmersiveAccent
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveOnAccent
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceVariant
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.theme.ImmersiveTextTertiary

@Composable
fun FrequencySelector(
    selectedChannel: FrequencyChannel,
    onSelectChannel: (FrequencyChannel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "موجات البث والترددات في لبنان",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CellTower,
                    contentDescription = null,
                    tint = ImmersiveAccent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "91.1 • 91.3 • 91.5",
                    style = MaterialTheme.typography.labelSmall,
                    color = ImmersiveAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Immersive Segmented Capsule Pill Selector
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = ImmersiveSurfaceVariant,
            border = BorderStroke(1.dp, ImmersiveBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RadioChannels.CHANNELS.forEach { channel ->
                    val isSelected = channel.id == selectedChannel.id
                    val shortName = when (channel.id) {
                        "freq_91_1" -> "91.1 FM"
                        "freq_91_3" -> "91.3 FM"
                        "freq_91_5" -> "91.5 FM"
                        else -> "HD راديو"
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) ImmersiveAccent else Color.Transparent)
                            .clickable { onSelectChannel(channel) }
                            .padding(vertical = 10.dp)
                            .testTag("freq_capsule_${channel.frequencyMhz.replace(" ", "_")}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shortName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                color = if (isSelected) ImmersiveOnAccent else ImmersiveTextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Grid 2x2 for detailed channel info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FrequencyCard(
                channel = RadioChannels.CHANNELS[0],
                isSelected = selectedChannel.id == RadioChannels.CHANNELS[0].id,
                onClick = { onSelectChannel(RadioChannels.CHANNELS[0]) },
                modifier = Modifier.weight(1f)
            )
            FrequencyCard(
                channel = RadioChannels.CHANNELS[1],
                isSelected = selectedChannel.id == RadioChannels.CHANNELS[1].id,
                onClick = { onSelectChannel(RadioChannels.CHANNELS[1]) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FrequencyCard(
                channel = RadioChannels.CHANNELS[2],
                isSelected = selectedChannel.id == RadioChannels.CHANNELS[2].id,
                onClick = { onSelectChannel(RadioChannels.CHANNELS[2]) },
                modifier = Modifier.weight(1f)
            )
            FrequencyCard(
                channel = RadioChannels.CHANNELS[3],
                isSelected = selectedChannel.id == RadioChannels.CHANNELS[3].id,
                onClick = { onSelectChannel(RadioChannels.CHANNELS[3]) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun FrequencyCard(
    channel: FrequencyChannel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) ImmersiveAccent else ImmersiveBorder.copy(alpha = 0.5f),
        label = "freq_border"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("freq_channel_${channel.frequencyMhz.replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        color = if (isSelected) ImmersiveSurfaceVariant else ImmersiveSurface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = channel.frequencyMhz,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (isSelected) ImmersiveAccent else ImmersiveTextPrimary
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ImmersiveAccent.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "مُحدد",
                            style = MaterialTheme.typography.labelSmall,
                            color = ImmersiveAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Icon(
                        imageVector = if (channel.id == "freq_hd") Icons.Default.WifiTethering else Icons.Default.Radio,
                        contentDescription = null,
                        tint = ImmersiveTextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val shortRegion = when (channel.id) {
                "freq_91_1" -> "بيروت وجبل لبنان"
                "freq_91_3" -> "الشمال وطرابلس وعكار"
                "freq_91_5" -> "الجنوب والبقاع وصيدا"
                else -> "البث الرقمي (عالمي)"
            }

            Text(
                text = shortRegion,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) ImmersiveTextPrimary else ImmersiveTextSecondary,
                maxLines = 1
            )

            Text(
                text = channel.powerKw,
                style = MaterialTheme.typography.labelSmall,
                color = ImmersiveTextTertiary
            )
        }
    }
}

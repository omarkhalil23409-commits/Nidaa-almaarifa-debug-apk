package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioChannels
import com.example.ui.theme.ImmersiveAccent
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceVariant
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.theme.ImmersiveTextTertiary

@Composable
fun FrequenciesGuideSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CellTower,
                contentDescription = null,
                tint = ImmersiveAccent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "دليل موجات وتغطية إذاعة نداء المعرفة",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveTextPrimary
                )
            )
        }

        // Introduction Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("frequencies_intro_card"),
            shape = RoundedCornerShape(20.dp),
            color = ImmersiveSurface,
            border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.6f)),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ImmersiveAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = null,
                            tint = ImmersiveAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "3 ترددات رئيسية تغطي كافة الأراضي اللبنانية",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveTextPrimary
                            )
                        )
                        Text(
                            text = "صوت نداء المعرفة يصلكم بأعلى جودة نقاء FM ستيريو",
                            style = MaterialTheme.typography.bodySmall,
                            color = ImmersiveTextSecondary
                        )
                    }
                }
            }
        }

        // Channel Cards
        RadioChannels.CHANNELS.forEach { channel ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guide_card_${channel.frequencyMhz.replace(" ", "_")}"),
                shape = RoundedCornerShape(20.dp),
                color = ImmersiveSurface,
                border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.6f)),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Frequency badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ImmersiveSurfaceVariant,
                            border = BorderStroke(1.5.dp, ImmersiveAccent)
                        ) {
                            Text(
                                text = channel.frequencyMhz,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = ImmersiveAccent
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        // Transmitter power & Location
                        Text(
                            text = "${channel.powerKw} • ${channel.transmitterLocation}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ImmersiveTextTertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = channel.nameArabic,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = channel.coverageRegion,
                        style = MaterialTheme.typography.bodySmall,
                        color = ImmersiveTextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Coverage Areas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationCity,
                            contentDescription = null,
                            tint = ImmersiveAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "المناطق المشمولة: ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ImmersiveAccent
                        )
                        Text(
                            text = when (channel.id) {
                                "freq_91_1" -> "بيروت، المتن، بعبدا، عاليه، كسروان، الشوف، جبيل"
                                "freq_91_3" -> "طرابلس، الميناء، زغرتا، الكورة، البترون، عكار، المنية"
                                "freq_91_5" -> "صيدا، صور، النبطية، جزين، زحلة، بعلبك، البقاع الغربي"
                                else -> "بث رقمي عالي الجودة عبر الإنترنت في جميع دول العالم"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = ImmersiveTextSecondary
                        )
                    }
                }
            }
        }
    }
}

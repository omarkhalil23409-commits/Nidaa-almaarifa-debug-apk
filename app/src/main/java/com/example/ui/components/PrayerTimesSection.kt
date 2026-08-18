package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LebaneseCityPrayer
import com.example.ui.theme.ImmersiveAccent
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveOnAccent
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceVariant
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.theme.ImmersiveTextTertiary

@Composable
fun PrayerTimesSection(
    cities: List<LebaneseCityPrayer>,
    selectedCityIndex: Int,
    currentTimeString: String,
    onSelectCity: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentCity = cities.getOrElse(selectedCityIndex) { cities.first() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prayer_times_card"),
        shape = RoundedCornerShape(24.dp),
        color = ImmersiveSurface,
        border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.6f)),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = ImmersiveAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "مواقيت الصلاة في لبنان",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveTextPrimary
                            )
                        )
                        Text(
                            text = "بتوقيت إذاعة نداء المعرفة",
                            style = MaterialTheme.typography.labelSmall,
                            color = ImmersiveTextSecondary
                        )
                    }
                }

                // Current Time Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ImmersiveSurfaceVariant,
                    border = BorderStroke(1.dp, ImmersiveBorder)
                ) {
                    Text(
                        text = currentTimeString,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveAccent
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Lebanese Cities Horizontal Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cities.forEachIndexed { index, city ->
                    val isSelected = index == selectedCityIndex
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCity(index) },
                        label = {
                            Text(
                                text = city.cityNameArabic,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = ImmersiveOnAccent
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ImmersiveAccent,
                            selectedLabelColor = ImmersiveOnAccent,
                            containerColor = ImmersiveSurfaceVariant,
                            labelColor = ImmersiveTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) ImmersiveAccent else ImmersiveBorder,
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("city_chip_$index")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prayer Times Grid (Fajr, Shurooq, Dhuhr, Asr, Maghrib, Isha)
            val prayers = listOf(
                Pair("الفجر", currentCity.fajr),
                Pair("الشروق", currentCity.sunrise),
                Pair("الظهر", currentCity.dhuhr),
                Pair("العصر", currentCity.asr),
                Pair("المغرب", currentCity.maghrib),
                Pair("العشاء", currentCity.isha)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                prayers.forEach { (name, time) ->
                    PrayerPill(
                        name = name,
                        time = time,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Broadcast Notice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ImmersiveSurfaceVariant)
                    .border(BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = ImmersiveAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "يُرفع الأذان على الهواء مباشرة عبر الترددات 91.1 - 91.3 - 91.5 FM في أوقات الصلاة",
                    style = MaterialTheme.typography.labelSmall,
                    color = ImmersiveTextSecondary
                )
            }
        }
    }
}

@Composable
fun PrayerPill(
    name: String,
    time: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ImmersiveSurfaceVariant,
        border = BorderStroke(1.dp, ImmersiveBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveAccent
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Black,
                    color = ImmersiveTextPrimary,
                    fontSize = 11.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

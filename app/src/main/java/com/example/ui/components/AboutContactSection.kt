package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ImmersiveAccent
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveOnAccent
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceVariant
import com.example.ui.theme.ImmersiveTextPrimary
import com.example.ui.theme.ImmersiveTextSecondary
import com.example.ui.theme.ImmersiveTextTertiary

@Composable
fun AboutContactSection(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun dialPhone(phone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun sendEmail(email: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "تواصل عبر تطبيق إذاعة نداء المعرفة")
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun shareApp() {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT,
                    "استمع إلى إذاعة نداء المعرفة على الموجات 91.1 - 91.3 - 91.5 FM في لبنان وعبر البث المباشر: http://nidaa.fm"
                )
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "مشاركة إذاعة نداء المعرفة")
            context.startActivity(shareIntent)
        } catch (_: Exception) {}
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // About Header Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("about_station_card"),
            shape = RoundedCornerShape(24.dp),
            color = ImmersiveSurface,
            border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.6f)),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(ImmersiveSurfaceVariant)
                        .border(BorderStroke(2.dp, ImmersiveAccent), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.nidaa_radio_icon_1786790478314),
                        contentDescription = "شعار إذاعة نداء المعرفة",
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "إذاعة نداء المعرفة",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = ImmersiveTextPrimary
                    )
                )

                Text(
                    text = "Voice of Knowledge Radio • Beirut, Lebanon",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = ImmersiveAccent,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "إذاعة إسلامية ثقافية تربوية تبث من بيروت - لبنان على الترددات 91.1 و 91.3 و 91.5 FM لتغطي كافة الأراضي اللبنانية، وتقدم برامج علمية، وتلاوات قرآنية عطرة، ومحاضرات دينية، ومواعظ هادفة تنشر الوسطية والاعتدال والمحبة.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ImmersiveTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Contact & Communication Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("contact_info_card"),
            shape = RoundedCornerShape(20.dp),
            color = ImmersiveSurface,
            border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.6f)),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "معلومات التواصل والترددات",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveTextPrimary
                    )
                )

                ContactActionRow(
                    icon = Icons.Default.Language,
                    title = "الموقع الرسمي للبث",
                    subtitle = "www.nidaa.fm",
                    onClick = { openUrl("http://nidaa.fm") }
                )

                ContactActionRow(
                    icon = Icons.Default.Call,
                    title = "هاتف الإذاعة والاستوديو",
                    subtitle = "+961 1 300 000 (لبنان)",
                    onClick = { dialPhone("+9611300000") }
                )

                ContactActionRow(
                    icon = Icons.Default.Email,
                    title = "البريد الإلكتروني",
                    subtitle = "info@nidaa.fm",
                    onClick = { sendEmail("info@nidaa.fm") }
                )

                ContactActionRow(
                    icon = Icons.Default.LocationOn,
                    title = "المقر الرئيسي",
                    subtitle = "بيروت - الجمهورية اللبنانية",
                    onClick = {}
                )

                ContactActionRow(
                    icon = Icons.Default.Share,
                    title = "مشاركة التطبيق والإذاعة",
                    subtitle = "انشر الخير مع عائلتك وأصدقائك",
                    onClick = { shareApp() }
                )
            }
        }
    }
}

@Composable
fun ContactActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = ImmersiveSurfaceVariant,
        border = BorderStroke(1.dp, ImmersiveBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ImmersiveAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ImmersiveAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveTextPrimary
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ImmersiveTextSecondary
                )
            }
        }
    }
}

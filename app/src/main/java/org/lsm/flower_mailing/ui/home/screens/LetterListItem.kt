package org.lsm.flower_mailing.ui.home.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.lsm.flower_mailing.data.letter.Letter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun LetterListItem(
    letter: Letter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = letter.judulSurat,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatSmartTime(letter.tanggalMasuk),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (letter.jenisSurat.equals("keluar", true)) letter.pengirim else letter.pengirim,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Numbers,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = letter.nomorSurat,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TypeBadge(type = letter.jenisSurat)

                if (!letter.prioritas.isNullOrBlank() && !letter.prioritas.equals("biasa", ignoreCase = true)) {
                    SifatBadge(sifat = letter.prioritas)
                }

                StatusBadge(status = letter.status)
            }
        }
    }
}

@Composable
fun TypeBadge(type: String) {
    val isMasuk = type.equals("masuk", ignoreCase = true)
    val color = if (isMasuk) Color(0xFF1565C0) else Color(0xFF7B1FA2)
    val bg = if (isMasuk) Color(0xFFE3F2FD) else Color(0xFFF3E5F5)

    Badge(text = if (isMasuk) "MASUK" else "KELUAR", backgroundColor = bg, textColor = color)
}

@Composable
fun SifatBadge(sifat: String) {
    val (bg, text) = when (sifat.lowercase()) {
        "penting" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        "segera" -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
        else -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
    }
    Badge(text = sifat.uppercase(), backgroundColor = bg, textColor = text)
}

@Composable
fun StatusBadge(status: String) {
    val (bg, text) = when (status) {
        "draft" -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        "perlu_verifikasi", "perlu_revisi" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        "belum_disposisi", "perlu_persetujuan" -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        "sudah_disposisi", "disetujui", "terkirim" -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    val label = status.replace('_', ' ').uppercase()

    Badge(text = label, backgroundColor = bg, textColor = text)
}

@Composable
fun Badge(text: String, backgroundColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = textColor
        )
    }
}

@Composable
fun CenteredMessage(icon: ImageVector, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

fun formatSmartTime(timestamp: String): String {
    if (timestamp.isBlank()) return ""

    return try {
        var parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        var date = parser.parse(timestamp)

        if (date == null) {
            parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            parser.timeZone = TimeZone.getTimeZone("UTC")
            date = parser.parse(timestamp)
        }

        if (date == null) return ""

        val now = Calendar.getInstance()
        val letterDate = Calendar.getInstance()
        letterDate.time = date

        val format: SimpleDateFormat = if (now.get(Calendar.YEAR) == letterDate.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == letterDate.get(Calendar.DAY_OF_YEAR)) {
            SimpleDateFormat("HH:mm", Locale.getDefault())
        } else if (now.get(Calendar.YEAR) == letterDate.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) - letterDate.get(Calendar.DAY_OF_YEAR) == 1) {
            return "Kemarin"
        } else {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        }

        format.format(date)
    } catch (e: Exception) {
        ""
    }
}
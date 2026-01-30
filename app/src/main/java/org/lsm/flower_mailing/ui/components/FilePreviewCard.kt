package org.lsm.flower_mailing.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun FilePreviewCard(
        fileName: String,
        fileType: String = "", // "pdf", "image", etc.
        fileUrl: Any? = null, // Can be Uri, String URL, or null
        onRemove: (() -> Unit)? = null, // If null, hide X button
        onClick: (() -> Unit)? = null,
        modifier: Modifier = Modifier
) {
        val isImage =
                fileType.contains("image", ignoreCase = true) ||
                        fileName.endsWith(".jpg", ignoreCase = true) ||
                        fileName.endsWith(".png", ignoreCase = true) ||
                        fileName.endsWith(".jpeg", ignoreCase = true)

        val isPdf =
                fileType.contains("pdf", ignoreCase = true) ||
                        fileName.endsWith(".pdf", ignoreCase = true)

        val cardModifier =
                if (onClick != null) {
                        modifier.fillMaxWidth().clickable(onClick = onClick)
                } else {
                        modifier.fillMaxWidth()
                }

        Card(
                modifier = cardModifier,
                shape = RoundedCornerShape(12.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor =
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
        ) {
                Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        // Thumbnail / Icon
                        Box(
                                modifier =
                                        Modifier.size(48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                        MaterialTheme.colorScheme
                                                                .surfaceContainerHigh
                                                ),
                                contentAlignment = Alignment.Center
                        ) {
                                if (isImage && fileUrl != null) {
                                        AsyncImage(
                                                model = fileUrl,
                                                contentDescription = "Preview",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                        )
                                } else {
                                        val icon: ImageVector =
                                                when {
                                                        isPdf -> Icons.Default.PictureAsPdf
                                                        isImage -> Icons.Default.Image
                                                        else -> Icons.Default.Description
                                                }
                                        Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                        )
                                }
                        }

                        // File Info
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = fileName,
                                        style =
                                                MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Medium
                                                ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                )
                                if (fileType.isNotBlank()) {
                                        Text(
                                                text = fileType.uppercase(),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                }
                        }

                        // Remove Button
                        if (onRemove != null) {
                                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                                        Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove file",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                        )
                                }
                        }
                }
        }
}

package org.lsm.flower_mailing.ui.home.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.lsm.flower_mailing.ui.home.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DirekturDashboardScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToSuratMasuk: () -> Unit,
    onNavigateToSuratKeluar: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val inboxList by viewModel.inboxList.collectAsState()
    val suratKeluarList by viewModel.suratKeluarList.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchInboxLetters()
        viewModel.fetchOutboxLetter()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            DirekturHeader(
                userName = userName ?: "Direktur",
                userRole = "Direktur"
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DirekturStatCard(
                    title = "Perlu Disposisi",
                    count = inboxList.size.toString(),
                    icon = Icons.Default.Inbox,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    onColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToSuratMasuk
                )
                DirekturStatCard(
                    title = "Perlu Persetujuan",
                    count = suratKeluarList.size.toString(),
                    icon = Icons.Default.Approval,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    onColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToSuratKeluar
                )
            }

            DirekturMenuSection(
                onNavigateToSuratMasuk = onNavigateToSuratMasuk,
                onNavigateToSuratKeluar = onNavigateToSuratKeluar,
                onNavigateToHistory = onNavigateToHistory
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun DirekturHeader(
    userName: String,
    userRole: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "Selamat Datang,",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = userRole,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun DirekturStatCard(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    onColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.height(110.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .border(
                        width = 1.5.dp,
                        color = onColor.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = onColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = onColor.copy(alpha = 0.8f)
                )
                Text(
                    text = count,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = onColor
                )
            }
        }
    }
}

@Composable
private fun DirekturMenuSection(
    onNavigateToSuratMasuk: () -> Unit,
    onNavigateToSuratKeluar: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val currentDate = remember {
        val sdf = try {
            SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        } catch (e: Exception) {
            SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        }
        sdf.format(Date())
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Menu Utama",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = currentDate,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column {
                DirekturMenuItem(
                    icon = Icons.Default.Inbox,
                    title = "Surat Masuk",
                    subtitle = "Disposisi surat masuk",
                    onClick = onNavigateToSuratMasuk
                )
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))

                DirekturMenuItem(
                    icon = Icons.Default.Outbox,
                    title = "Surat Keluar",
                    subtitle = "Persetujuan surat keluar",
                    onClick = onNavigateToSuratKeluar
                )
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))

                DirekturMenuItem(
                    icon = Icons.Default.History,
                    title = "Riwayat Surat",
                    subtitle = "Arsip semua surat",
                    onClick = onNavigateToHistory
                )
            }
        }
    }
}

@Composable
private fun DirekturMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    }
}
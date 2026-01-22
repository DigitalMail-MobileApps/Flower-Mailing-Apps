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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.lsm.flower_mailing.ui.home.HomeViewModel

@Composable
fun UmumDashboardScreen(
        viewModel: HomeViewModel = viewModel(),
        onNavigateToAddLetter: () -> Unit,
        onNavigateToSuratMasuk: () -> Unit,
        onNavigateToDraft: () -> Unit,
        onNavigateToHistory: () -> Unit,
        onNavigateToSuratKeluar: () -> Unit
) {
        val userName by viewModel.userName.collectAsState()
        val userRole by viewModel.userRole.collectAsState()
        val inboxList by viewModel.inboxList.collectAsState()
        val draftList by viewModel.draftList.collectAsState()
        val suratKeluarList by viewModel.suratKeluarList.collectAsState()

        LaunchedEffect(Unit) {
                viewModel.fetchInboxLetters()
                viewModel.fetchDraftLetters()
                viewModel.fetchOutboxLetter()
        }

        Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                floatingActionButton = {
                        FloatingActionButton(
                                onClick = onNavigateToAddLetter,
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                shape = RoundedCornerShape(16.dp)
                        ) {
                                Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Buat Surat Baru"
                                )
                        }
                }
        ) { innerPadding ->
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(innerPadding)
                                        .padding(horizontal = 20.dp)
                                        .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        DashboardHeader(
                                userName = userName ?: "Loading...",
                                userRole = userRole ?: "Loading...",
                        )

                        val isStafProgram =
                                userRole?.equals("staf_program", ignoreCase = true) == true
                        val isStafLembaga =
                                userRole?.equals("staf_lembaga", ignoreCase = true) == true
                        val isManajer =
                                userRole?.contains("manajer", ignoreCase = true) == true ||
                                        userRole?.contains("manager", ignoreCase = true) == true
                        val isAdmin = userRole?.equals("admin", ignoreCase = true) == true

                        when {
                                // STAF PROGRAM: Only creates Surat Keluar (External scope)
                                isStafProgram -> {
                                        Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                                StatCard(
                                                        title = "Surat Keluar Saya",
                                                        count = suratKeluarList.size.toString(),
                                                        icon = Icons.Default.Outbox,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .tertiaryContainer,
                                                        onColor =
                                                                MaterialTheme.colorScheme
                                                                        .onTertiaryContainer,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        onClick = onNavigateToSuratKeluar
                                                )
                                                StatCard(
                                                        title = "Riwayat",
                                                        count = "Lihat",
                                                        icon = Icons.Default.History,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .surfaceVariant,
                                                        onColor =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        onClick = onNavigateToHistory
                                                )
                                        }
                                }
                                // STAF LEMBAGA: Creates Surat Masuk + Internal Surat Keluar
                                isStafLembaga -> {
                                        Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(16.dp)
                                                ) {
                                                        StatCard(
                                                                title = "Surat Masuk",
                                                                count = inboxList.size.toString(),
                                                                icon = Icons.Default.Inbox,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .primaryContainer,
                                                                onColor =
                                                                        MaterialTheme.colorScheme
                                                                                .onPrimaryContainer,
                                                                modifier = Modifier.weight(1f),
                                                                onClick = onNavigateToSuratMasuk
                                                        )
                                                        StatCard(
                                                                title = "Surat Keluar",
                                                                count =
                                                                        suratKeluarList.size
                                                                                .toString(),
                                                                icon = Icons.Default.Outbox,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .tertiaryContainer,
                                                                onColor =
                                                                        MaterialTheme.colorScheme
                                                                                .onTertiaryContainer,
                                                                modifier = Modifier.weight(1f),
                                                                onClick = onNavigateToSuratKeluar
                                                        )
                                                }
                                                StatCard(
                                                        title = "Riwayat",
                                                        count = "Lihat",
                                                        icon = Icons.Default.History,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .surfaceVariant,
                                                        onColor =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        onClick = onNavigateToHistory
                                                )
                                        }
                                }
                                isManajer -> {
                                        Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                                StatCard(
                                                        title = "Perlu Verifikasi",
                                                        count =
                                                                suratKeluarList.size
                                                                        .toString(), // Logic
                                                        // updated in
                                                        // VM to fetch
                                                        // verification list
                                                        icon = Icons.Default.Outbox,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .errorContainer,
                                                        onColor =
                                                                MaterialTheme.colorScheme
                                                                        .onErrorContainer,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        onClick = onNavigateToSuratKeluar // This
                                                        // leads to
                                                        // the list
                                                        )
                                                StatCard(
                                                        title = "Riwayat Verifikasi",
                                                        count = "Lihat", // Placeholder or fetch
                                                        // history count
                                                        icon = Icons.Default.History,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .surfaceVariant,
                                                        onColor =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        onClick = onNavigateToHistory
                                                )
                                        }
                                }
                                isAdmin -> {
                                        // Admin View
                                        Card(
                                                colors =
                                                        CardDefaults.cardColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .secondaryContainer
                                                        ),
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(vertical = 16.dp)
                                        ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                        Text(
                                                                "Panel Admin",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .titleMedium,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(
                                                                "Gunakan menu pengaturan untuk manajemen user (Fitur mendatang)."
                                                        )
                                                }
                                        }
                                }
                                else -> {
                                        // Generic Staff
                                        Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(16.dp)
                                                ) {
                                                        StatCard(
                                                                title = "Surat Masuk",
                                                                count = inboxList.size.toString(),
                                                                icon = Icons.Default.Inbox,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .primaryContainer,
                                                                onColor =
                                                                        MaterialTheme.colorScheme
                                                                                .onPrimaryContainer,
                                                                modifier = Modifier.weight(1f),
                                                                onClick = onNavigateToSuratMasuk
                                                        )
                                                        StatCard(
                                                                title = "Surat Keluar",
                                                                count =
                                                                        suratKeluarList.size
                                                                                .toString(),
                                                                icon = Icons.Default.Outbox,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .tertiaryContainer,
                                                                onColor =
                                                                        MaterialTheme.colorScheme
                                                                                .onTertiaryContainer,
                                                                modifier = Modifier.weight(1f),
                                                                onClick = onNavigateToSuratKeluar
                                                        )
                                                }
                                                StatCard(
                                                        title = "Draft",
                                                        count = draftList.size.toString(),
                                                        icon = Icons.Default.Description,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .secondaryContainer,
                                                        onColor =
                                                                MaterialTheme.colorScheme
                                                                        .onSecondaryContainer,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        onClick = onNavigateToDraft
                                                )
                                        }
                                }
                        }

                        MenuSection(
                                onNavigateToSuratMasuk = onNavigateToSuratMasuk,
                                onNavigateToDraft = onNavigateToDraft,
                                onNavigateToHistory = onNavigateToHistory,
                                onNavigateToSuratKeluar = onNavigateToSuratKeluar,
                                showDraft = !isManajer && !isAdmin, // Hide draft for Manager/Admin
                                showSuratKeluar =
                                        !isAdmin, // Everyone except Admin sees Surat Keluar
                                showSuratMasuk =
                                        isStafLembaga, // Only Staf Lembaga registers Surat Masuk
                                showHistory = !isAdmin // Hide History for Admin
                        )

                        Spacer(modifier = Modifier.height(80.dp))
                }
        }
}

@Composable
fun DashboardHeader(userName: String, userRole: String, modifier: Modifier = Modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
                Box(
                        modifier =
                                Modifier.size(56.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                ) {
                        Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
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
                                style =
                                        MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold
                                        ),
                                color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                                text =
                                        userRole.replace('_', ' ').replaceFirstChar {
                                                it.uppercase()
                                        },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                        )
                }
        }
}

@Composable
fun StatCard(
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
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Box(
                                modifier =
                                        Modifier.size(56.dp)
                                                .border(
                                                        width = 1.5.dp,
                                                        color = onColor.copy(alpha = 0.3f),
                                                        shape = CircleShape
                                                )
                                                .background(
                                                        Color.White.copy(alpha = 0.15f),
                                                        CircleShape
                                                ),
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
                        Column(verticalArrangement = Arrangement.Center) {
                                Text(
                                        text = title,
                                        style =
                                                MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 13.sp
                                                ),
                                        color = onColor.copy(alpha = 0.8f)
                                )
                                Text(
                                        text = count,
                                        style =
                                                MaterialTheme.typography.headlineLarge.copy(
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
fun MenuSection(
        onNavigateToSuratMasuk: () -> Unit,
        onNavigateToDraft: () -> Unit,
        onNavigateToHistory: () -> Unit,
        onNavigateToSuratKeluar: () -> Unit = {},
        showDraft: Boolean = true,
        showSuratKeluar: Boolean = true,
        showSuratMasuk: Boolean = true,
        showHistory: Boolean = true
) {
        val currentDate = remember {
                val sdf =
                        try {
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
                                style =
                                        MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                        )
                        )
                        Text(
                                text = currentDate,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }

                Card(
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                        Column {
                                if (showSuratMasuk) {
                                        MenuItem(
                                                icon = Icons.Default.Inbox,
                                                title = "Surat Masuk",
                                                subtitle = "Cek surat yang perlu diproses",
                                                onClick = onNavigateToSuratMasuk
                                        )
                                }

                                if (showSuratKeluar) {
                                        // Divider logic needs to be smarter if previous item was
                                        // hidden
                                        // For now, assume if SuratKeluar is shown, SuratMasuk might
                                        // be there
                                        if (showSuratMasuk) {
                                                HorizontalDivider(
                                                        thickness = 0.5.dp,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .outlineVariant,
                                                )
                                        }
                                        MenuItem(
                                                icon = Icons.Default.Outbox,
                                                title = "Surat Keluar",
                                                subtitle = "Kelola surat keluar",
                                                onClick = onNavigateToSuratKeluar
                                        )
                                }

                                if (showDraft) {
                                        HorizontalDivider(
                                                thickness = 0.5.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant,
                                        )
                                        MenuItem(
                                                icon = Icons.Default.Description,
                                                title = "Draft Surat",
                                                subtitle = "Lanjutkan surat yang tertunda",
                                                onClick = onNavigateToDraft
                                        )
                                }

                                if (showHistory) {
                                        HorizontalDivider(
                                                thickness = 0.5.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant,
                                        )
                                        MenuItem(
                                                icon = Icons.Default.History,
                                                title = "Riwayat Surat",
                                                subtitle = "Arsip surat yang sudah selesai",
                                                onClick = onNavigateToHistory
                                        )
                                }
                        }
                }
        }
}

@Composable
fun MenuItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
        Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                Box(
                        modifier =
                                Modifier.size(40.dp)
                                        .background(
                                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                                RoundedCornerShape(10.dp)
                                        ),
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
                                style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.SemiBold
                                        )
                        )
                        Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }

                Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
        }
}

package org.lsm.flower_mailing.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.lsm.flower_mailing.data.auth.User

@Composable
fun SettingsScreen(
        viewModel: SettingsViewModel = viewModel(),
        onNavigateToEditProfile: () -> Unit,
        onNavigateToChangePassword: () -> Unit,
        onLoggedOut: () -> Unit
) {
        val userProfile by viewModel.userProfile.collectAsState()
        val isLoading = viewModel.isLoading

        // Fetch profile on load
        LaunchedEffect(Unit) { viewModel.fetchProfile() }

        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading && userProfile == null) {
                        CircularProgressIndicator()
                } else {
                        ProfileHeaderSection(profile = userProfile)
                        Spacer(modifier = Modifier.height(32.dp))

                        SettingsGroupTitle("Akun Saya")
                        SettingsGroupCard {
                                SettingsTile(
                                        icon = Icons.Default.Person,
                                        title = "Edit Profil",
                                        subtitle = "Nama, Jabatan, Atribut",
                                        onClick = onNavigateToEditProfile
                                )
                                HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                )
                                SettingsTile(
                                        icon = Icons.Default.Lock,
                                        title = "Ganti Kata Sandi",
                                        onClick = onNavigateToChangePassword
                                )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        SettingsGroupTitle("Umum")
                        SettingsGroupCard {
                                SettingsTile(
                                        icon = Icons.Default.Info,
                                        title = "Versi Aplikasi",
                                        subtitle = "v1.0.0",
                                        showArrow = false,
                                        onClick = {}
                                )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                                onClick = { viewModel.logout(onLoggedOut) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme.errorContainer,
                                                contentColor =
                                                        MaterialTheme.colorScheme.onErrorContainer
                                        ),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                                Icon(Icons.Default.Logout, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                        "Keluar Aplikasi",
                                        style =
                                                MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.Bold
                                                )
                                )
                        }
                }
                Spacer(modifier = Modifier.height(40.dp))
        }
}

@Composable
private fun ProfileHeaderSection(profile: User?) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
        ) {
                Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                                Modifier.size(100.dp)
                                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                        Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                        text = "${profile?.firstName ?: ""} ${profile?.lastName ?: ""}",
                        style =
                                MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                ),
                        color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                        text = profile?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(50),
                ) {
                        Text(
                                text = profile?.role?.replace('_', ' ')?.uppercase() ?: "USER",
                                style =
                                        MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold
                                        ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                }

                if (!profile?.jabatan.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                                text = "${profile?.jabatan}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                        )
                }
        }
}

@Composable
private fun SettingsGroupTitle(title: String) {
        Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp)
        )
}

@Composable
private fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
        Card(
                colors =
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border =
                        androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
        ) { Column(content = content) }
}

@Composable
private fun SettingsTile(
        icon: ImageVector,
        title: String,
        subtitle: String? = null,
        showArrow: Boolean = true,
        onClick: () -> Unit
) {
        Row(
                modifier =
                        Modifier.fillMaxWidth()
                                .clickable(onClick = onClick)
                                .padding(horizontal = 16.dp, vertical = 16.dp),
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
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                        Text(
                                text = title,
                                style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Medium
                                        ),
                                color = MaterialTheme.colorScheme.onSurface
                        )
                        if (subtitle != null) {
                                Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                }
                if (showArrow) {
                        Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                }
        }
}

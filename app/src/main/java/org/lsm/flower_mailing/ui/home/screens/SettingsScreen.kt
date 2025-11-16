package org.lsm.flower_mailing.ui.home.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.lsm.flower_mailing.R
import org.lsm.flower_mailing.ui.home.HomeViewModel
import org.lsm.flower_mailing.ui.theme.FlowermailingTheme

@Composable
fun SettingsScreen(
    homeViewModel: HomeViewModel = viewModel()
) {
    val userName by homeViewModel.userName.collectAsState()
    val userEmail by homeViewModel.userEmail.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        ProfileHeader(
            userName = userName ?: "Loading...",
            userEmail = userEmail ?: "Loading...",
            profileImagePainter = rememberVectorPainter(Icons.Default.AccountCircle),
            cameraIconPainter = rememberVectorPainter(Icons.Default.CameraAlt),
        )

        Spacer(modifier = Modifier.height(24.dp))
        SettingsMenuItem(
            icon = Icons.Default.PersonOutline,
            title = "Informasi Pribadi",
            onClick = { /* TODO: Navigate to Informasi Pribadi screen */ }
        )
        SettingsMenuItem(
            icon = Icons.Default.Key,
            title = "Keamanan",
            onClick = { /* TODO: Navigate to Keamanan screen */ }
        )
        SettingsMenuItem(
            icon = Icons.Default.HelpOutline,
            title = "Bantuan",
            onClick = { /* TODO: Navigate to Bantuan screen */ }
        )
        SettingsMenuItem(
            icon = Icons.Default.LightMode,
            title = "Tampilan",
            trailingText = "Terang", // Dynamic text for theme
            onClick = { /* TODO: Toggle theme or navigate to theme settings */ }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { homeViewModel.logout() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0x66FF0000)),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error
            ),
        ) {
            Text(text = "Keluar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    userEmail: String,
    profileImagePainter: Painter,
    cameraIconPainter: Painter,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
            ) {
                Image(
                    painter = profileImagePainter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant) // Placeholder
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .align(Alignment.BottomEnd)
                        .clickable { /* TODO: Open camera/gallery */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = cameraIconPainter,
                        contentDescription = "Edit Profile Picture",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = userEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (trailingText != null) {
                    Text(
                        text = trailingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileHeaderPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        FlowermailingTheme {
            Spacer(modifier = Modifier.height(16.dp))
            ProfileHeader(
                userName = "Ikbar Siddiq",
                userEmail = "ikbar.siddiq@gmail.com",
                profileImagePainter = rememberVectorPainter(Icons.Default.AccountCircle),
                cameraIconPainter = rememberVectorPainter(Icons.Default.CameraAlt)
            )
            Column() {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsMenuItem(
                    icon = Icons.Default.PersonOutline,
                    title = "Informasi Pribadi",
                    onClick = { /* TODO: Navigate to Informasi Pribadi screen */ }
                )
                SettingsMenuItem(
                    icon = Icons.Default.Key,
                    title = "Keamanan",
                    onClick = { /* TODO: Navigate to Keamanan screen */ }
                )
                SettingsMenuItem(
                    icon = Icons.Default.HelpOutline,
                    title = "Bantuan",
                    onClick = { /* TODO: Navigate to Bantuan screen */ }
                )
                SettingsMenuItem(
                    icon = Icons.Default.LightMode,
                    title = "Tampilan",
                    trailingText = "Terang", // Dynamic text for theme
                    onClick = { /* TODO: Toggle theme or navigate to theme settings */ }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {TODO()},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0x66FF0000)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                ) {
                    Text(text = "Keluar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

}
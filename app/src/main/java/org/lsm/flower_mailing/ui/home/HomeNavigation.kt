package org.lsm.flower_mailing.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.lsm.flower_mailing.ui.home.screens.DirekturDashboardScreen
import org.lsm.flower_mailing.ui.home.screens.DraftScreen
import org.lsm.flower_mailing.ui.home.screens.HistoryScreen
import org.lsm.flower_mailing.ui.home.screens.SuratKeluarScreen
import org.lsm.flower_mailing.ui.home.screens.SuratMasukScreen
import org.lsm.flower_mailing.ui.home.screens.UmumDashboardScreen
import org.lsm.flower_mailing.ui.settings.ChangePasswordScreen
import org.lsm.flower_mailing.ui.settings.EditProfileScreen
import org.lsm.flower_mailing.ui.settings.SettingsScreen
import org.lsm.flower_mailing.ui.settings.SettingsViewModel

sealed class HomeRoute(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : HomeRoute("home", "Home", Icons.Default.Home)
    object SuratMasuk : HomeRoute("surat_masuk", "Surat Masuk", Icons.Default.Inbox)
    object Draft : HomeRoute("draft", "Draft", Icons.Default.Drafts)
    object History : HomeRoute("history", "History", Icons.Default.History)
    object DirekturDashboard : HomeRoute("direktur_dash", "Dashboard", Icons.Default.Home)
    object Settings : HomeRoute("settings", "Settings", Icons.Default.Settings)
    object SuratKeluar : HomeRoute("surat_keluar", "Surat Keluar", Icons.Default.Outbox)
    object Notification : HomeRoute("notification", "Notification", Icons.Default.Settings)
    object EditProfile : HomeRoute("settings/edit_profile", "Edit Profil", Icons.Default.Person)
    object ChangePassword : HomeRoute("settings/change_password", "Ganti Password", Icons.Default.Lock)
}

@Composable
fun HomeNavHost(
    navController: NavHostController,
    startDestination: String,
    viewModel: HomeViewModel,
    onNavigateToAddLetter: () -> Unit,
    onNavigateToLetterDetail: (Int) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        composable("loading") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        composable("unrecognized") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: Unrecognized User Role")
            }
        }

        composable(HomeRoute.Home.route) {
            UmumDashboardScreen(
                viewModel = viewModel,
                onNavigateToAddLetter = onNavigateToAddLetter,
                onNavigateToSuratMasuk = { navController.navigate(HomeRoute.SuratMasuk.route) },
                onNavigateToDraft = { navController.navigate(HomeRoute.Draft.route) },
                onNavigateToHistory = { navController.navigate(HomeRoute.History.route)},
                onNavigateToSuratKeluar = {navController.navigate(HomeRoute.SuratKeluar.route)}
            )
        }
        composable(HomeRoute.SuratMasuk.route) {
            SuratMasukScreen(viewModel = viewModel, onNavigateToLetterDetail = onNavigateToLetterDetail)
        }
        composable(HomeRoute.Draft.route) {
            DraftScreen(viewModel = viewModel, onNavigateToLetterDetail = onNavigateToLetterDetail)
        }
        composable(HomeRoute.History.route) {
            HistoryScreen(viewModel = viewModel, onNavigateToLetterDetail = onNavigateToLetterDetail)
        }
        composable(HomeRoute.DirekturDashboard.route) {
            DirekturDashboardScreen(
                viewModel = viewModel,
                onNavigateToSuratMasuk = { navController.navigate(HomeRoute.SuratMasuk.route) },
                onNavigateToSuratKeluar = { navController.navigate(HomeRoute.SuratKeluar.route) },
                onNavigateToHistory = { navController.navigate(HomeRoute.History.route) }
            )
        }

        composable(HomeRoute.SuratKeluar.route) {
            SuratKeluarScreen(
                viewModel = viewModel,
                onNavigateToDetail = onNavigateToLetterDetail
            )
        }

        composable(HomeRoute.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateToEditProfile = { navController.navigate(HomeRoute.EditProfile.route) },
                onNavigateToChangePassword = { navController.navigate(HomeRoute.ChangePassword.route) },
                onLoggedOut = {}
            )
        }

        composable(HomeRoute.EditProfile.route) {
            val settingsViewModel: SettingsViewModel = viewModel()
            EditProfileScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(HomeRoute.ChangePassword.route) {
            val settingsViewModel: SettingsViewModel = viewModel()
            ChangePasswordScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
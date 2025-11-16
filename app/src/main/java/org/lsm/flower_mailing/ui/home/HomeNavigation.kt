package org.lsm.flower_mailing.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.lsm.flower_mailing.ui.home.screens.DirekturDashboardScreen
import org.lsm.flower_mailing.ui.home.screens.DraftScreen
import org.lsm.flower_mailing.ui.home.screens.HistoryScreen
import org.lsm.flower_mailing.ui.home.screens.SettingsScreen
import org.lsm.flower_mailing.ui.home.screens.SuratMasukScreen
import org.lsm.flower_mailing.ui.home.screens.UmumDashboardScreen

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
}

@Composable
fun HomeNavHost(
    navController: NavHostController,
    startDestination: String,
    viewModel: HomeViewModel,
    onNavigateToAddLetter: () -> Unit,
    onNavigateToLetterDetail: (Int) -> Unit
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
            UmumDashboardScreen(homeViewModel = viewModel, onNavigateToAddLetter = onNavigateToAddLetter, onNavigateToLetterDetail = onNavigateToLetterDetail)
        }
        composable(HomeRoute.SuratMasuk.route) {
            SuratMasukScreen(viewModel = viewModel, onNavigateToLetterDetail = onNavigateToLetterDetail)
        }
        composable(HomeRoute.Draft.route) {
            DraftScreen(viewModel = viewModel, onNavigateToDetail = onNavigateToLetterDetail, onNavigateToLetterDetail = onNavigateToLetterDetail)
        }
        composable(HomeRoute.History.route) {
            HistoryScreen(viewModel = viewModel, onNavigateToLetterDetail = onNavigateToLetterDetail)
        }
        composable(HomeRoute.DirekturDashboard.route) {
            DirekturDashboardScreen()
        }
        composable(HomeRoute.Settings.route) {
            SettingsScreen(homeViewModel = viewModel)        }
    }
}
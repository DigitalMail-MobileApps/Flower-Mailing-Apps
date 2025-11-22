package org.lsm.flower_mailing.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onLoggedOut: () -> Unit,
    onNavigateToAddLetter: () -> Unit,
    onNavigateToLetterDetail: (Int) -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val userRole by viewModel.userRole.collectAsState()
    val isLoggedOut by viewModel.isLoggedOut.collectAsState()
    val nestedNavController = rememberNavController()

    // Track current route to update TopBar
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(key1 = isLoggedOut) {
        if (isLoggedOut) {
            onLoggedOut()
        }
    }

    // Define Bottom Bar Items based on Role
    val (navItems, startDestination) = when {
        userRole.equals("direktur", ignoreCase = true) -> {
            listOf(
                HomeRoute.DirekturDashboard, HomeRoute.SuratMasuk,
                HomeRoute.History, HomeRoute.Settings
            ) to HomeRoute.DirekturDashboard.route
        }
        userRole.equals("adc", ignoreCase = true) -> {
            listOf(
                HomeRoute.Home, HomeRoute.SuratMasuk, HomeRoute.SuratKeluar,
                HomeRoute.History, HomeRoute.Settings
            ) to HomeRoute.Home.route
        }
        userRole.equals("bagian_umum", ignoreCase = true) -> {
            listOf(
                HomeRoute.Home, HomeRoute.SuratMasuk, HomeRoute.Draft,
                HomeRoute.History, HomeRoute.Settings
            ) to HomeRoute.Home.route
        }
        else -> emptyList<HomeRoute>() to "loading"
    }

    // Dynamic Top Bar Configuration
    val isSubScreen = currentRoute == HomeRoute.EditProfile.route ||
            currentRoute == HomeRoute.ChangePassword.route

    val topBarTitle = when (currentRoute) {
        HomeRoute.Settings.route -> "Pengaturan"
        HomeRoute.EditProfile.route -> "Edit Profil"
        HomeRoute.ChangePassword.route -> "Ganti Password"
        HomeRoute.SuratMasuk.route -> "Surat Masuk"
        HomeRoute.Draft.route -> "Draft Surat"
        HomeRoute.History.route -> "Riwayat"
        HomeRoute.SuratKeluar.route -> "Surat Keluar"
        HomeRoute.Home.route, HomeRoute.DirekturDashboard.route -> "Flower Mailing"
        else -> "Flower Mailing"
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                title = topBarTitle,
                userRole = userRole,
                showBackButton = isSubScreen, // Show arrow only on sub-screens
                onBackClick = { nestedNavController.popBackStack() },
                onSettingsClick = {
                    nestedNavController.navigate(HomeRoute.Settings.route) {
                        popUpTo(nestedNavController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNotificationClick = onNavigateToNotifications
            )
        },
        bottomBar = {
            // Hide BottomBar on sub-screens if desired, or keep it.
            // Usually hidden for Edit/Detail screens.
            if (navItems.isNotEmpty() && !isSubScreen) {
                HomeBottomBar(
                    navController = nestedNavController,
                    navItems = navItems
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            HomeNavHost(
                navController = nestedNavController,
                startDestination = startDestination,
                viewModel = viewModel,
                onNavigateToAddLetter = onNavigateToAddLetter,
                onNavigateToLetterDetail = onNavigateToLetterDetail,
            )
        }
    }
}
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
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(key1 = isLoggedOut) {
        if (isLoggedOut) {
            onLoggedOut()
        }
    }

    val (navItems, startDestination) =
            when {
                // DIREKTUR: Sees incoming for disposition, outgoing for approval (NO history - no
                // API access)
                userRole.equals("direktur", ignoreCase = true) -> {
                    listOf(
                            HomeRoute.DirekturDashboard,
                            HomeRoute.SuratMasuk, // Disposition queue
                            HomeRoute.SuratKeluar, // Approval queue
                    ) to HomeRoute.DirekturDashboard.route
                }
                // STAF PROGRAM: Creates Surat Keluar (External scope only) - NO Surat Masuk
                userRole.equals("staf_program", ignoreCase = true) -> {
                    listOf(
                            HomeRoute.Home,
                            HomeRoute.SuratKeluar, // Their outgoing letters
                            HomeRoute.History,
                    ) to HomeRoute.Home.route
                }
                // STAF LEMBAGA: Creates Surat Masuk + Surat Keluar (Internal scope)
                userRole.equals("staf_lembaga", ignoreCase = true) -> {
                    listOf(
                            HomeRoute.Home,
                            HomeRoute.SuratMasuk, // Register incoming
                            HomeRoute.SuratKeluar, // Internal outgoing
                            HomeRoute.History,
                    ) to HomeRoute.Home.route
                }
                // MANAGERS (KPP, Pemas, PKL): Verification dashboard (NO history - no API access)
                userRole?.contains("manajer", ignoreCase = true) == true ||
                        userRole?.contains("manager", ignoreCase = true) == true -> {
                    listOf(
                            HomeRoute.Home, // Verification Dashboard
                            HomeRoute.SuratKeluar, // Letters to verify
                    ) to HomeRoute.Home.route
                }
                // ADMIN: Settings/management only (no letter operations)
                userRole.equals("admin", ignoreCase = true) -> {
                    listOf(HomeRoute.Home, HomeRoute.Settings) to HomeRoute.Home.route
                }
                else -> {
                    // Default fallback for unknown roles
                    listOf(
                            HomeRoute.Home,
                    ) to HomeRoute.Home.route
                }
            }

    val isSubScreen =
            currentRoute == HomeRoute.EditProfile.route ||
                    currentRoute == HomeRoute.ChangePassword.route

    val topBarTitle =
            when (currentRoute) {
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
                        showBackButton = isSubScreen,
                        onBackClick = { nestedNavController.popBackStack() },
                        onSettingsClick = {
                            nestedNavController.navigate(HomeRoute.Settings.route) {
                                popUpTo(nestedNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNotificationClick = onNavigateToNotifications
                )
            },
            bottomBar = {
                if (navItems.isNotEmpty() && !isSubScreen) {
                    HomeBottomBar(navController = nestedNavController, navItems = navItems)
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
                    onLoggedOut = onLoggedOut
            )
        }
    }
}

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
                userRole.equals("direktur", ignoreCase = true) -> {
                    listOf(
                            HomeRoute.DirekturDashboard,
                            HomeRoute.SuratMasuk, // Monitor Incoming
                            HomeRoute.History,
                    ) to HomeRoute.DirekturDashboard.route
                }
                userRole.equals("adc", ignoreCase = true) -> {
                    listOf(
                            HomeRoute.Home,
                            HomeRoute.SuratMasuk,
                            HomeRoute.SuratKeluar, // ADC manages Outgoing drafts
                            HomeRoute.History,
                    ) to HomeRoute.Home.route
                }
                userRole.equals("bagian_umum", ignoreCase = true) -> {
                    listOf(
                            HomeRoute.Home,
                            HomeRoute.SuratMasuk,
                            HomeRoute.Draft,
                            HomeRoute.History,
                    ) to HomeRoute.Home.route
                }
                // Generic Staff (Program, Lembaga, etc.)
                userRole?.startsWith("staf", ignoreCase = true) == true ||
                        userRole?.startsWith("budi", ignoreCase = true) ==
                                true || // Explicit check if names used as roles inadvertently
                        userRole?.contains("program", ignoreCase = true) == true ||
                        userRole?.contains("lembaga", ignoreCase = true) == true -> {
                    listOf(
                            HomeRoute.Home,
                            HomeRoute.SuratMasuk, // Can register
                            // HomeRoute.Draft, // Optional
                            HomeRoute.SuratKeluar, // Can view My Letters
                            HomeRoute.History,
                    ) to HomeRoute.Home.route
                }
                // Managers (KPP, Pemas, PKL) -> Focus on Verification
                userRole?.contains("manajer", ignoreCase = true) == true ||
                        userRole?.contains("manager", ignoreCase = true) == true -> {
                    listOf(
                            HomeRoute.Home, // Verification Dashboard
                            // Managers might not register incoming letters or view generic history
                            // depending on scope
                            // But let's keep History if they need to check past verifications
                            HomeRoute.History
                    ) to HomeRoute.Home.route
                }
                // Admin -> No specific dashboard yet, but must have valid start destination
                userRole.equals("admin", ignoreCase = true) ||
                        userRole.equals("administrasi", ignoreCase = true) -> {
                    // Safe fallback to Home, even if empty menu
                    emptyList<HomeRoute>() to HomeRoute.Home.route
                }
                else -> {
                    // Default fallback for unknown roles to avoid infinite loading
                    listOf(
                            HomeRoute.Home,
                            HomeRoute.History,
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

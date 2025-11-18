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
import androidx.navigation.compose.rememberNavController

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onLoggedOut: () -> Unit,
    onNavigateToAddLetter: () -> Unit,
    onNavigateToLetterDetail: (Int) -> Unit,
    onNavigateToNotifications: () -> Unit,
) {
    val userRole by viewModel.userRole.collectAsState()
    val isLoggedOut by viewModel.isLoggedOut.collectAsState()
    val nestedNavController = rememberNavController()

    LaunchedEffect(key1 = isLoggedOut) {
        if (isLoggedOut) {
            onLoggedOut()
        }
    }

    val (navItems, startDestination) = when {
        userRole.equals("direktur", ignoreCase = true) -> {
            listOf(
                HomeRoute.DirekturDashboard,
                HomeRoute.SuratMasuk,
                HomeRoute.History,
            ) to HomeRoute.DirekturDashboard.route
        }
        userRole.equals("adc", ignoreCase = true) -> {
            listOf(
                HomeRoute.Home,
                HomeRoute.SuratMasuk,
                HomeRoute.Draft,
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
        else -> {
            emptyList<HomeRoute>() to (if (userRole == null) "loading" else "unrecognized")
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                title = "Flower Mailing",
                userRole = userRole,
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
            if (navItems.isNotEmpty()) {
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
                onNavigateToLetterDetail = onNavigateToLetterDetail
            )
        }
    }
}
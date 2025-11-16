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
    onNavigateToLetterDetail: (Int) -> Unit
) {
    val userRole by viewModel.userRole.collectAsState()
    val isLoggedOut by viewModel.isLoggedOut.collectAsState()
    val nestedNavController = rememberNavController()

    LaunchedEffect(key1 = isLoggedOut) {
        if (isLoggedOut) {
            onLoggedOut()
        }
    }

    val (navItems, startDestination) = when (userRole) {
        "direktur" -> {
            listOf(HomeRoute.DirekturDashboard) to HomeRoute.DirekturDashboard.route
        }
        "adc" -> {
            listOf(
                HomeRoute.Home,
                HomeRoute.SuratMasuk,
                HomeRoute.Draft,
                HomeRoute.History
            ) to HomeRoute.Home.route
        }
        "bagian_umum" -> {
            listOf(
                HomeRoute.Home,
                HomeRoute.SuratMasuk,
                HomeRoute.Draft,
                HomeRoute.History
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
                onSettingsClick = {
                    nestedNavController.navigate(HomeRoute.Settings.route) {
                        popUpTo(nestedNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
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
                onNavigateToLetterDetail = onNavigateToLetterDetail,
            )
        }
    }
}
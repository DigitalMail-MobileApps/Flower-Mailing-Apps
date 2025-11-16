package org.lsm.flower_mailing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.lsm.flower_mailing.ui.SplashScreen
import org.lsm.flower_mailing.ui.auth.ForgotPasswordScreen
import org.lsm.flower_mailing.ui.auth.LoginScreen
import org.lsm.flower_mailing.ui.auth.LoginViewModel
import org.lsm.flower_mailing.ui.home.HomeScreen
import org.lsm.flower_mailing.ui.theme.FlowermailingTheme
import org.lsm.flower_mailing.ui.add_letter.AddLetterScreen
import org.lsm.flower_mailing.ui.letter_detail.LetterDetailScreen

private object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val Forgot = "forgot"
    const val Home = "home"
    const val AddLetter = "add_letter"
    const val LetterDetail = "letter_detail/{letterId}"
    fun letterDetail(letterId: Int) = "letter_detail/$letterId"

}

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FlowermailingTheme {
                val nav = rememberNavController()
                val isLoggedIn by loginViewModel.isLoggedIn.collectAsStateWithLifecycle()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                        AppNavHost(
                            nav = nav,
                            isLoggedIn = isLoggedIn,
                            loginViewModel = loginViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun AppNavHost(
    nav: NavHostController,
    isLoggedIn: Boolean?,
    loginViewModel: LoginViewModel
) {
    NavHost(
        navController = nav,
        startDestination = Routes.Splash
    ) {
        splashRoute(nav, isLoggedIn)
        loginRoute(nav, loginViewModel)
        forgotRoute(nav, loginViewModel)
        homeRoute(nav, loginViewModel)
        addLetterRoute(nav)
        letterDetailRoute(nav)
    }
}

private fun NavGraphBuilder.splashRoute(
    nav: NavHostController,
    isLoggedIn: Boolean?
) {
    composable(Routes.Splash) {
        SplashScreen(
            onTimeout = {
                if (isLoggedIn!!) {
                    nav.navigate(Routes.Home) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                } else {
                    nav.navigate(Routes.Login) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            }
        )
        LaunchedEffect(isLoggedIn) {
            if (isLoggedIn != null) {
                val destination = if (isLoggedIn) Routes.Home else Routes.Login
                nav.navigate(destination) {
                    popUpTo(Routes.Splash) { inclusive = true }
                }
            }
        }
    }
}

private fun NavGraphBuilder.loginRoute(
    nav: NavHostController,
    loginViewModel: LoginViewModel
) {
    composable(Routes.Login) {
        LoginScreen(
            loginViewModel = loginViewModel,
            onNavigateToForgotPassword = { nav.navigate(Routes.Forgot) },
            onLoggedIn = {
                nav.navigate(Routes.Home) {
                    popUpTo(nav.graph.findStartDestination().id) {
                        inclusive = true
                    }
                }
            }
        )
    }
}

private fun NavGraphBuilder.forgotRoute(
    nav: NavHostController,
    loginViewModel: LoginViewModel
) {
    composable(Routes.Forgot) {
        ForgotPasswordScreen(
            loginViewModel = loginViewModel,
            onNavigateBack = { nav.popBackStack() }
        )
    }
}

private fun NavGraphBuilder.homeRoute(
    nav: NavHostController,
    loginViewModel: LoginViewModel
) {
    composable(Routes.Home) {
        HomeScreen(
            onLoggedOut = {
                loginViewModel.onLogout()
                nav.navigate(Routes.Login) {
                    popUpTo(nav.graph.findStartDestination().id) {
                        inclusive = true
                    }
                }
            },
            onNavigateToAddLetter = { nav.navigate(Routes.AddLetter) },
            onNavigateToLetterDetail = { letterId -> nav.navigate(Routes.letterDetail(letterId)) },
        )
    }
}

private fun NavGraphBuilder.addLetterRoute(
    nav: NavHostController
) {
    composable(Routes.AddLetter) {
        AddLetterScreen(
            onNavigateBack = {
                nav.popBackStack()
            }
        )
    }
}

private fun NavGraphBuilder.letterDetailRoute(
    nav: NavHostController
) {
    composable(
        route = Routes.LetterDetail,
        arguments = listOf(navArgument("letterId") { type = NavType.StringType })
    ) {
        LetterDetailScreen(
            onNavigateBack = {
                nav.popBackStack()
            }
        )
    }
}

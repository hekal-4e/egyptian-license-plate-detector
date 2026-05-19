package com.depi.graduationproject.core.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.depi.graduationproject.presentation.dashboard.DashboardScreen
import com.depi.graduationproject.presentation.history.HistoryScreen
import com.depi.graduationproject.presentation.scanner.ScannerScreen
import com.depi.graduationproject.presentation.scanner.ZoneSelectionScreen
import com.depi.graduationproject.presentation.scanner.TicketScreen
import com.depi.graduationproject.presentation.checkout.CheckoutScreen
import com.depi.graduationproject.presentation.manualentry.ManualEntryScreen
import com.depi.graduationproject.presentation.settings.SettingsScreen
import com.depi.graduationproject.presentation.splash.SplashScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Splash.route) {
                SplashScreen(
                    onNavigateNext = {
                        navController.navigate(Routes.Dashboard.route) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.Dashboard.route) {
                DashboardScreen(
                    onNavigateToScanner = { navController.navigate(Routes.Scanner.route) },
                    onNavigateToManualEntry = { navController.navigate(Routes.ManualEntry.route) },
                    onNavigateToCheckout = { sessionId ->
                        navController.navigate(Routes.Checkout.createRoute(sessionId = sessionId))
                    },
                    onNavigateToHistory = { navController.navigate(Routes.History.route) },
                    onNavigateToSettings = { navController.navigate(Routes.Settings.route) }
                )
            }

            composable(Routes.Scanner.route) {
                ScannerScreen(
                    onClose = { navController.popBackStack() },
                    onNavigateToZoneSelection = { plateText ->
                        navController.navigate(Routes.ZoneSelection.createRoute(plateText))
                    },
                    onNavigateToCheckout = { plateText ->
                        navController.navigate(Routes.Checkout.createRoute(plateText = plateText))
                    },
                    onNavigateToManualEntry = {
                        navController.navigate(Routes.ManualEntry.route)
                    }
                )
            }

composable(
                route = Routes.ZoneSelection.route,
                arguments = listOf(navArgument("plateText") { type = NavType.StringType })
            ) {
                ZoneSelectionScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTicket = { sessionId ->
                        navController.navigate(Routes.Ticket.createRoute(sessionId)) {
                            popUpTo(Routes.Dashboard.route) { inclusive = false }
                        }
                    }
                )
            }

            composable(
                route = Routes.Ticket.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) {
                TicketScreen(
                    onDone = {
                        navController.navigate(Routes.Dashboard.route) {
                            popUpTo(Routes.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Routes.Checkout.route,
                arguments = listOf(
                    navArgument("sessionId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("plateText") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                CheckoutScreen(
                    onNavigateBack = { navController.popBackStack() },
                    scannedSessionId = backStackEntry.arguments?.getString("sessionId"),
                    scannedPlateText = backStackEntry.arguments?.getString("plateText")
                )
            }

            composable(Routes.ManualEntry.route) {
                ManualEntryScreen(
                    onConfirm = { plateText ->
                        navController.navigate(Routes.ZoneSelection.createRoute(plateText)) {
                            popUpTo(Routes.ManualEntry.route) { inclusive = true }
                        }
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.History.route) {
                HistoryScreen(
                    onNavigateToHome = {
                        navController.navigate(Routes.Dashboard.route) {
                            popUpTo(Routes.Dashboard.route) { inclusive = true }
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Routes.Settings.route) {
                            popUpTo(Routes.Dashboard.route) { inclusive = false }
                        }
                    },
                    onNavigateToCheckout = { sessionId ->
                        navController.navigate(Routes.Checkout.createRoute(sessionId = sessionId))
                    },
                    onNavigateToManualEntry = {
                        navController.navigate(Routes.ManualEntry.route) {
                            popUpTo(Routes.Dashboard.route) { inclusive = false }
                        }
                    }
                )
            }

            composable(Routes.Settings.route) {
                SettingsScreen(
                    onNavigateToHome = {
                        navController.navigate(Routes.Dashboard.route) {
                            popUpTo(Routes.Dashboard.route) { inclusive = true }
                        }
                    },
                    onNavigateToHistory = {
                        navController.navigate(Routes.History.route) {
                            popUpTo(Routes.Dashboard.route) { inclusive = false }
                        }
                    },
                    onNavigateToManualEntry = {
                        navController.navigate(Routes.ManualEntry.route) {
                            popUpTo(Routes.Dashboard.route) { inclusive = false }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String, onClick: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Placeholder for $name\n(Click to proceed if splash)")
    }
}
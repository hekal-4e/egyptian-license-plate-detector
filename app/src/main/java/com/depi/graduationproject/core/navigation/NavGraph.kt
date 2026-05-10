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

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Splash.route) {
                PlaceholderScreen("Splash Screen") {
                    navController.navigate(Routes.Dashboard.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            }

            composable(Routes.Dashboard.route) {
                PlaceholderScreen("Dashboard Screen")
            }

            composable(Routes.Scanner.route) {
                PlaceholderScreen("Scanner Screen")
            }

            composable(Routes.ZoneSelection.route) {
                PlaceholderScreen("Zone Selection")
            }

            composable(
                route = Routes.Ticket.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) {
                val sessionId = it.arguments?.getString("sessionId") ?: ""
                PlaceholderScreen("Ticket Screen for: $sessionId")
            }

            composable(Routes.Checkout.route) {
                PlaceholderScreen("Checkout Screen")
            }

            composable(Routes.ManualEntry.route) {
                PlaceholderScreen("Manual Entry Screen")
            }

            composable(Routes.History.route) {
                PlaceholderScreen("History Screen")
            }

            composable(Routes.Settings.route) {
                PlaceholderScreen("Settings Screen")
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

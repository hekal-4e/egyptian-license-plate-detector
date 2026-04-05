package com.depi.graduationproject.ui.navigation

import android.graphics.Bitmap
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.depi.graduationproject.ui.screens.CameraScreen
import com.depi.graduationproject.ui.screens.HistoryScreen
import com.depi.graduationproject.ui.screens.DashboardScreen // تأكد من تغيير اسم HomeScreen لـ DashboardScreen
import com.depi.graduationproject.ui.viewmodels.MainViewModel

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    onImageCaptured: (Bitmap) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // لا تظهر الشريط السفلي في شاشة الكاميرا لتوفير مساحة كاملة للمسح
            if (currentRoute != ScreenRoute.Camera.route) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == ScreenRoute.Dashboard.route,
                        onClick = {
                            if (currentRoute != ScreenRoute.Dashboard.route)
                                navController.navigate(ScreenRoute.Dashboard.route)
                        },
                        label = { Text("Dashboard") },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == ScreenRoute.History.route,
                        onClick = {
                            if (currentRoute != ScreenRoute.History.route)
                                navController.navigate(ScreenRoute.History.route)
                        },
                        label = { Text("Records") },
                        icon = { Icon(Icons.Default.History, contentDescription = null) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenRoute.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ScreenRoute.Dashboard.route) {
                DashboardScreen( // الشاشة المحدثة التي صممناها سابقاً
                    onStartCamera = { navController.navigate(ScreenRoute.Camera.route) },
                    onViewHistory = { navController.navigate(ScreenRoute.History.route) }
                )
            }

            composable(ScreenRoute.Camera.route) {
                CameraScreen(
                    viewModel = viewModel,
                    onNavigateToHistory = { navController.navigate(ScreenRoute.History.route) },
                    onImageCaptured = onImageCaptured
                )
            }

            composable(ScreenRoute.History.route) {
                HistoryScreen(viewModel = viewModel)
            }
        }
    }
}
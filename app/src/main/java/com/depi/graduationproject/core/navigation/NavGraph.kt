package com.depi.graduationproject.core.navigation

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
            if (currentRoute != Routes.Camera.route) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.Dashboard.route,
                        onClick = {
                            if (currentRoute != Routes.Dashboard.route)
                                navController.navigate(Routes.Dashboard.route)
                        },
                        label = { Text("Dashboard") },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.History.route,
                        onClick = {
                            if (currentRoute != Routes.History.route)
                                navController.navigate(Routes.History.route)
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
            startDestination = Routes.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Dashboard.route) {
                DashboardScreen( // الشاشة المحدثة التي صممناها سابقاً
                    onStartCamera = { navController.navigate(Routes.Camera.route) },
                    onViewHistory = { navController.navigate(Routes.History.route) }
                )
            }

            composable(Routes.Camera.route) {
                CameraScreen(
                    viewModel = viewModel,
                    onNavigateToHistory = { navController.navigate(Routes.History.route) },
                    onImageCaptured = onImageCaptured
                )
            }

            composable(Routes.History.route) {
                HistoryScreen(viewModel = viewModel)
            }
        }
    }
}

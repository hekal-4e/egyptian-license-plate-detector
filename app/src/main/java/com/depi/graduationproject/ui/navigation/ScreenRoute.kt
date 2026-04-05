package com.depi.graduationproject.ui.navigation

sealed class ScreenRoute(val route: String) {
    data object Dashboard : ScreenRoute("dashboard_screen")
    data object Camera : ScreenRoute("camera_screen")
    data object History : ScreenRoute("history_screen")
}
package com.depi.graduationproject.core.navigation

sealed class Routes(val route: String) {
    data object Dashboard : Routes("dashboard_screen")
    data object Camera : Routes("camera_screen")
    data object History : Routes("history_screen")
}

package com.depi.graduationproject.core.navigation

sealed class Routes(val route: String) {
    data object Splash : Routes("splash")
    data object Dashboard : Routes("dashboard")
    data object Scanner : Routes("scanner")
    data object ZoneSelection : Routes("zone_selection")
    data object Ticket : Routes("ticket/{sessionId}") {
        fun createRoute(sessionId: String) = "ticket/$sessionId"
    }
    data object Checkout : Routes("checkout")
    data object ManualEntry : Routes("manual_entry")
    data object History : Routes("history")
    data object Settings : Routes("settings")
}

package com.depi.graduationproject.core.navigation

import android.net.Uri

sealed class Routes(val route: String) {
    data object Splash : Routes("splash")
    data object Dashboard : Routes("dashboard")
    data object Scanner : Routes("scanner")
    data object ZoneSelection : Routes("zone_selection/{plateText}") {
        fun createRoute(plateText: String) = "zone_selection/${Uri.encode(plateText)}"
    }
    data object Ticket : Routes("ticket/{sessionId}") {
        fun createRoute(sessionId: String) = "ticket/$sessionId"
    }
    data object Checkout : Routes("checkout?sessionId={sessionId}&plateText={plateText}") {
        fun createRoute(sessionId: String? = null, plateText: String? = null): String {
            val params = buildList {
                if (!sessionId.isNullOrBlank()) add("sessionId=${Uri.encode(sessionId)}")
                if (!plateText.isNullOrBlank()) add("plateText=${Uri.encode(plateText)}")
            }
            return if (params.isEmpty()) {
                "checkout"
            } else {
                "checkout?${params.joinToString("&")}"
            }
        }
    }
    data object ManualEntry : Routes("manual_entry")
    data object History : Routes("history")
    data object Settings : Routes("settings")
}
package com.depi.graduationproject.domain.model

data class GarageSettings(
    val hourlyRateEgp: Double = 10.0,
    val totalCapacity: Int = 90,
    val autoOpenGate: Boolean = false,
    val pushNotifications: Boolean = true
)

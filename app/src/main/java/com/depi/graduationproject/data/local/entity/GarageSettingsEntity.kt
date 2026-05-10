package com.depi.graduationproject.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "garage_settings")
data class GarageSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val hourlyRateEgp: Double = 10.0,
    val totalCapacity: Int = 90,
    val autoOpenGate: Boolean = false,
    val pushNotifications: Boolean = true
)
package com.depi.graduationproject.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zones")
data class ZoneEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val totalCapacity: Int,
    val occupiedSpots: Int = 0
)
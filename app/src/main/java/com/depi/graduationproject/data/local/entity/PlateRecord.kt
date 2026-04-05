package com.depi.graduationproject.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plates_table")
data class PlateRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val plateNumber: String,
    val timestamp: Long,
    val dateDisplay: String
)

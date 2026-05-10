package com.depi.graduationproject.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.depi.graduationproject.domain.model.SessionStatus
import com.depi.graduationproject.domain.model.SyncStatus

@Entity(
    tableName = "parking_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ZoneEntity::class,
            parentColumns = ["id"],
            childColumns = ["zoneId"],
            onDelete = ForeignKey.RESTRICT // Prevent zone deletion while sessions exist
        )
    ],
    indices = [
        Index(value = ["licensePlate", "status"]) ,
        Index(value = ["status"]),
        Index(value = ["syncStatus"]),
        Index(value = ["zoneId"])  // Required for FK performance
    ]
)
data class ParkingSessionEntity(
    @PrimaryKey val id: String,
    val licensePlate: String,
    val zoneId: String,
    val spotId: String? = null,
    val entryTime: Long,
    val exitTime: Long? = null,
    val durationHours: Int? = null,
    val hourlyRateApplied: Double,
    val totalFee: Double? = null,
    val status: SessionStatus,
    val syncStatus: SyncStatus,
    val createdAt: Long
)
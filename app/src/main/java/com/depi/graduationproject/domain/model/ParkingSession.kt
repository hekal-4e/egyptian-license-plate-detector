package com.depi.graduationproject.domain.model



data class ParkingSession(
    val id: String,
    val licensePlate: String,
    val zoneId: String,
    val spotId: String? = null,
    val entryTime: Long,
    val exitTime: Long? = null,
    val durationHours: Int? = null,
    val hourlyRateApplied: Double,
    val totalFee: Double? = null,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

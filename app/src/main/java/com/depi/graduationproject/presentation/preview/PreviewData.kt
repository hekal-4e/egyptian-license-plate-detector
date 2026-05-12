package com.depi.graduationproject.presentation.preview

import com.depi.graduationproject.domain.model.GarageSettings
import com.depi.graduationproject.domain.model.ParkingSession
import com.depi.graduationproject.domain.model.SessionStatus
import com.depi.graduationproject.domain.model.SyncStatus
import com.depi.graduationproject.domain.model.Zone

object PreviewSessions {
    val session1 = ParkingSession(
        id = "session-1",
        licensePlate = "1234 أ ب ج",
        zoneId = "zone_a",
        spotId = "A-12",
        entryTime = System.currentTimeMillis() - 2 * 60 * 60 * 1000,
        exitTime = null,
        durationHours = 2,
        hourlyRateApplied = 15.0,
        totalFee = 30.0,
        status = SessionStatus.ACTIVE,
        syncStatus = SyncStatus.SYNCED,
        createdAt = System.currentTimeMillis() - 2 * 60 * 60 * 1000
    )

    val session2 = ParkingSession(
        id = "session-2",
        licensePlate = "5678 د ه و",
        zoneId = "zone_b",
        spotId = "B-5",
        entryTime = System.currentTimeMillis() - 4 * 60 * 60 * 1000,
        exitTime = null,
        durationHours = 4,
        hourlyRateApplied = 15.0,
        totalFee = 60.0,
        status = SessionStatus.ACTIVE,
        syncStatus = SyncStatus.SYNCED,
        createdAt = System.currentTimeMillis() - 4 * 60 * 60 * 1000
    )

    val completedSession1 = ParkingSession(
        id = "session-3",
        licensePlate = "9012 ز ح ط",
        zoneId = "zone_a",
        spotId = "A-8",
        entryTime = System.currentTimeMillis() - 6 * 60 * 60 * 1000,
        exitTime = System.currentTimeMillis() - 2 * 60 * 60 * 1000,
        durationHours = 4,
        hourlyRateApplied = 15.0,
        totalFee = 60.0,
        status = SessionStatus.COMPLETED,
        syncStatus = SyncStatus.SYNCED,
        createdAt = System.currentTimeMillis() - 6 * 60 * 60 * 1000
    )
}

object PreviewZones {
    val zoneA = Zone(
        id = "zone_a",
        name = "Zone A",
        description = "Premium Covered",
        totalCapacity = 20,
        occupiedSpots = 12
    )

    val zoneB = Zone(
        id = "zone_b",
        name = "Zone B",
        description = "Standard Open",
        totalCapacity = 30,
        occupiedSpots = 18
    )

    val zoneC = Zone(
        id = "zone_c",
        name = "Zone C",
        description = "Economy Parking",
        totalCapacity = 25,
        occupiedSpots = 25
    )

    val allZones = listOf(zoneA, zoneB, zoneC)
}

object PreviewSettings {
    val defaultSettings = GarageSettings(
        hourlyRateEgp = 15.0,
        totalCapacity = 75,
        autoOpenGate = true,
        pushNotifications = true
    )
}

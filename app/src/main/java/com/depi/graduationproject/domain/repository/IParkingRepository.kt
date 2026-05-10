package com.depi.graduationproject.domain.repository

import com.depi.graduationproject.domain.model.ParkingSession
import com.depi.graduationproject.domain.model.Zone
import kotlinx.coroutines.flow.Flow

interface IParkingRepository {
    // Flow-based queries for reactive UI
    fun getActiveSessionsFlow(): Flow<List<ParkingSession>>
    fun getActiveCountFlow(): Flow<Int>
    fun getAvailableZonesFlow(): Flow<List<Zone>>
    fun getAllZonesFlow(): Flow<List<Zone>>

    // Suspend operations
    suspend fun getSessionById(id: String): ParkingSession?
    suspend fun getActiveSessionByPlate(licensePlate: String): ParkingSession?
    suspend fun checkIn(session: ParkingSession)
    suspend fun checkout(sessionId: String, zoneId: String, totalFee: Double, durationHours: Int)
    suspend fun getByDateRange(startTime: Long, endTime: Long): List<ParkingSession>
    suspend fun getTodayRevenue(startOfDay: Long, endOfDay: Long): Double
    suspend fun searchByPlate(query: String): List<ParkingSession>
    suspend fun deleteSession(session: ParkingSession)
}

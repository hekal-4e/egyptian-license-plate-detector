package com.depi.graduationproject.data.repository

import androidx.room.withTransaction
import com.depi.graduationproject.data.local.GarageDatabase
import com.depi.graduationproject.domain.model.SessionStatus
import com.depi.graduationproject.data.local.dao.ParkingSessionDao
import com.depi.graduationproject.data.local.dao.ZoneDao
import com.depi.graduationproject.data.local.entity.ParkingSessionEntity
import com.depi.graduationproject.data.local.entity.ZoneEntity
import com.depi.graduationproject.domain.model.ParkingSession
import com.depi.graduationproject.domain.model.Zone
import com.depi.graduationproject.domain.repository.IParkingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParkingRepositoryImpl @Inject constructor(
    private val database: GarageDatabase,
    private val parkingDao: ParkingSessionDao,
    private val zoneDao: ZoneDao
) : IParkingRepository {

    override fun getActiveSessionsFlow(): Flow<List<ParkingSession>> =
        parkingDao.getActiveSessionsFlow().map { entities -> entities.map { it.toDomain() } }

    override fun getActiveCountFlow(): Flow<Int> = parkingDao.getActiveCountFlow()

    override fun getAvailableZonesFlow(): Flow<List<Zone>> =
        zoneDao.getAvailableZones().map { entities -> entities.map { it.toDomain() } }

    override fun getAllZonesFlow(): Flow<List<Zone>> =
        zoneDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getSessionById(id: String): ParkingSession? = withContext(Dispatchers.IO) {
        parkingDao.getById(id)?.toDomain()
    }

    override suspend fun getActiveSessionByPlate(licensePlate: String): ParkingSession? =
        withContext(Dispatchers.IO) {
            parkingDao.getActiveSessionByPlate(licensePlate)?.toDomain()
        }

    override suspend fun checkIn(session: ParkingSession) = withContext(Dispatchers.IO) {
        database.withTransaction {
            parkingDao.insert(session.toEntity())
            zoneDao.incrementOccupied(session.zoneId)
        }
    }

    override suspend fun checkout(
        sessionId: String,
        zoneId: String,
        totalFee: Double,
        durationHours: Int
    ) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val session = parkingDao.getById(sessionId) ?: return@withTransaction
            val updatedSession = session.copy(
                exitTime = System.currentTimeMillis(),
                totalFee = totalFee,
                durationHours = durationHours,
                status = SessionStatus.COMPLETED
            )
            parkingDao.update(updatedSession)
            zoneDao.decrementOccupied(zoneId)
        }
    }

    override suspend fun getByDateRange(startTime: Long, endTime: Long): List<ParkingSession> =
        withContext(Dispatchers.IO) {
            parkingDao.getByDateRange(startTime, endTime).map { it.toDomain() }
        }

    override suspend fun getTodayRevenue(startOfDay: Long, endOfDay: Long): Double =
        withContext(Dispatchers.IO) {
            parkingDao.getTodayRevenue(startOfDay, endOfDay) ?: 0.0
        }

    override suspend fun searchByPlate(query: String): List<ParkingSession> =
        withContext(Dispatchers.IO) {
            parkingDao.getByLicensePlate(query).map { it.toDomain() }
        }

    override suspend fun deleteSession(session: ParkingSession) = withContext(Dispatchers.IO) {
        parkingDao.delete(session.toEntity())
    }

    // --- Mappers ---

    private fun ParkingSessionEntity.toDomain(): ParkingSession = ParkingSession(
        id = id,
        licensePlate = licensePlate,
        zoneId = zoneId,
        spotId = spotId,
        entryTime = entryTime,
        exitTime = exitTime,
        durationHours = durationHours,
        hourlyRateApplied = hourlyRateApplied,
        totalFee = totalFee,
        status = status,
        syncStatus = syncStatus,
        createdAt = createdAt
    )

    private fun ParkingSession.toEntity(): ParkingSessionEntity = ParkingSessionEntity(
        id = id,
        licensePlate = licensePlate,
        zoneId = zoneId,
        spotId = spotId,
        entryTime = entryTime,
        exitTime = exitTime,
        durationHours = durationHours,
        hourlyRateApplied = hourlyRateApplied,
        totalFee = totalFee,
        status = status,
        syncStatus = syncStatus,
        createdAt = createdAt
    )

    private fun ZoneEntity.toDomain(): Zone = Zone(
        id = id,
        name = name,
        description = description,
        totalCapacity = totalCapacity,
        occupiedSpots = occupiedSpots
    )
}

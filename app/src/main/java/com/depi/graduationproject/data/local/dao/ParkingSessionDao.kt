package com.depi.graduationproject.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.depi.graduationproject.domain.model.SessionStatus
import com.depi.graduationproject.domain.model.SyncStatus
import com.depi.graduationproject.data.local.entity.ParkingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ParkingSessionEntity)

    @Update
    suspend fun update(session: ParkingSessionEntity)

    @Delete
    suspend fun delete(session: ParkingSessionEntity)

    @Query("DELETE FROM parking_sessions")
    suspend fun deleteAll()

    @Query("SELECT * FROM parking_sessions WHERE status = :activeStatus ORDER BY entryTime DESC")
    fun getActiveSessionsFlow(activeStatus: SessionStatus = SessionStatus.ACTIVE): Flow<List<ParkingSessionEntity>>

    @Query("SELECT COUNT(*) FROM parking_sessions WHERE status = :activeStatus")
    fun getActiveCountFlow(activeStatus: SessionStatus = SessionStatus.ACTIVE): Flow<Int>

    @Query("SELECT * FROM parking_sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ParkingSessionEntity?

    @Query("SELECT * FROM parking_sessions WHERE licensePlate = :licensePlate AND status = :status LIMIT 1")
    suspend fun getActiveSessionByPlate(
        licensePlate: String, status: SessionStatus = SessionStatus.ACTIVE
    ): ParkingSessionEntity?

    @Query("SELECT * FROM parking_sessions WHERE entryTime BETWEEN :startTime AND :endTime ORDER BY entryTime DESC")
    suspend fun getByDateRange(startTime: Long, endTime: Long): List<ParkingSessionEntity>

    @Query("SELECT SUM(totalFee) FROM parking_sessions WHERE exitTime BETWEEN :startOfDay AND :endOfDay AND status = :completedStatus")
    suspend fun getTodayRevenue(
        startOfDay: Long, endOfDay: Long, completedStatus: SessionStatus = SessionStatus.COMPLETED
    ): Double?

    @Query("SELECT * FROM parking_sessions WHERE syncStatus = :pendingStatus")
    suspend fun getUnsyncedSessions(pendingStatus: SyncStatus = SyncStatus.PENDING): List<ParkingSessionEntity>

    @Query("UPDATE parking_sessions SET syncStatus = :syncedStatus WHERE syncStatus = :pendingStatus")
    suspend fun markAllSynced(
        syncedStatus: SyncStatus = SyncStatus.SYNCED, pendingStatus: SyncStatus = SyncStatus.PENDING
    )

    @Query("SELECT * FROM parking_sessions WHERE zoneId = :zoneId AND entryTime BETWEEN :startTime AND :endTime")
    suspend fun getPeakHoursDataForZone(
        zoneId: String, startTime: Long, endTime: Long
    ): List<ParkingSessionEntity>

    @Query("UPDATE parking_sessions SET syncStatus = 'SYNCED' WHERE id = :sessionId")
    suspend fun markSynced(sessionId: String)

    @Query("SELECT * FROM parking_sessions WHERE licensePlate LIKE '%' || :plate || '%' ORDER BY entryTime DESC")
    suspend fun getByLicensePlate(plate: String): List<ParkingSessionEntity>

    @Query("SELECT * FROM parking_sessions WHERE exitTime BETWEEN :startOfDay AND :endOfDay AND status = 'COMPLETED'")
    suspend fun getCompletedToday(startOfDay: Long, endOfDay: Long): List<ParkingSessionEntity>

    @Query("SELECT * FROM parking_sessions WHERE status = 'ACTIVE' ORDER BY entryTime DESC")
    suspend fun getActiveSessions(): List<ParkingSessionEntity>

    @Query("SELECT * FROM parking_sessions WHERE entryTime BETWEEN :startTime AND :endTime")
    suspend fun getPeakHoursData(startTime: Long, endTime: Long): List<ParkingSessionEntity>
}
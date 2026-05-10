package com.depi.graduationproject.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.depi.graduationproject.data.local.entity.ZoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ZoneDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(zone: ZoneEntity)

    @Update
    suspend fun update(zone: ZoneEntity)

    @Query("SELECT * FROM zones")
    fun getAll(): Flow<List<ZoneEntity>>

    @Query("SELECT * FROM zones WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ZoneEntity?

    @Query("SELECT * FROM zones WHERE occupiedSpots < totalCapacity")
    fun getAvailableZones(): Flow<List<ZoneEntity>>

    @Query("UPDATE zones SET occupiedSpots = occupiedSpots + 1 WHERE id = :zoneId")
    suspend fun incrementOccupied(zoneId: String)

    @Query("UPDATE zones SET occupiedSpots = occupiedSpots - 1 WHERE id = :zoneId AND occupiedSpots > 0")
    suspend fun decrementOccupied(zoneId: String)
}
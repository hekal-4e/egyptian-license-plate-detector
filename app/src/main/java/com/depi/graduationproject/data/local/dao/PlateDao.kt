package com.depi.graduationproject.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.depi.graduationproject.data.local.entity.PlateRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PlateDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlate(plate: PlateRecord)

    @Query("SELECT * From plates_table ORDER BY timestamp DESC")
    fun getAllPlates(): Flow<List<PlateRecord>>

    @Query("DELETE FROM plates_table")
    suspend fun deleteAll()

    @Query("DELETE FROM plates_table WHERE id = :plateId")
    suspend fun deletePlateById(plateId: Int)
}

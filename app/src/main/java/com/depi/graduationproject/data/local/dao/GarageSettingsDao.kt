package com.depi.graduationproject.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.depi.graduationproject.data.local.entity.GarageSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GarageSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: GarageSettingsEntity)

    @Update
    suspend fun update(settings: GarageSettingsEntity)

    @Query("SELECT * FROM garage_settings WHERE id = 1")
    fun getSettings(): Flow<GarageSettingsEntity?>

    @Upsert
    suspend fun upsert(settings: GarageSettingsEntity)

}
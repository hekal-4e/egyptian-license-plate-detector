package com.depi.graduationproject.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.depi.graduationproject.core.utils.PlateUtils
import com.depi.graduationproject.data.local.converter.Converters
import com.depi.graduationproject.data.local.dao.GarageSettingsDao
import com.depi.graduationproject.data.local.dao.ParkingSessionDao
import com.depi.graduationproject.data.local.dao.ZoneDao
import com.depi.graduationproject.data.local.entity.GarageSettingsEntity
import com.depi.graduationproject.data.local.entity.ParkingSessionEntity
import com.depi.graduationproject.data.local.entity.ZoneEntity

/**
 * Main database for the LPR-Edge Parking System.
 * Follows Phase 4c (T046) requirements.
 */
@Database(
    entities = [
        ParkingSessionEntity::class,
        ZoneEntity::class,
        GarageSettingsEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class GarageDatabase : RoomDatabase() {

    abstract fun parkingSessionDao(): ParkingSessionDao
    abstract fun zoneDao(): ZoneDao
    abstract fun garageSettingsDao(): GarageSettingsDao

    companion object {
        const val DATABASE_NAME = "garage_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val cursor = db.query("SELECT id, licensePlate FROM parking_sessions")
                val update = db.compileStatement(
                    "UPDATE parking_sessions SET licensePlate = ? WHERE id = ?"
                )
                cursor.use {
                    while (cursor.moveToNext()) {
                        val id = cursor.getString(0) ?: continue
                        val rawPlate = cursor.getString(1) ?: ""
                        val normalized = PlateUtils.normalizeForStorage(rawPlate)
                        if (normalized.isNotBlank() && normalized != rawPlate) {
                            update.bindString(1, normalized)
                            update.bindString(2, id)
                            update.executeUpdateDelete()
                            update.clearBindings()
                        }
                    }
                }
            }
        }

        /**
         * Room Database Callback for pre-population.
         * Inserts default zones (A, B, C) and initial settings on first creation.
         */
        fun getCallback(): RoomDatabase.Callback {
            return object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    
                    // Pre-populate Zones (FR-016)
                    db.execSQL(
                        "INSERT INTO zones (id, name, description, totalCapacity, occupiedSpots) " +
                        "VALUES ('A', 'Zone A', 'General Parking Area', 50, 0)"
                    )
                    db.execSQL(
                        "INSERT INTO zones (id, name, description, totalCapacity, occupiedSpots) " +
                        "VALUES ('B', 'Zone B', 'Premium Covered Area', 30, 0)"
                    )
                    db.execSQL(
                        "INSERT INTO zones (id, name, description, totalCapacity, occupiedSpots) " +
                        "VALUES ('C', 'Zone C', 'VIP Reserved Area', 10, 0)"
                    )

                    // Pre-populate default Settings (FR-010)
                    // Defaults: 10 EGP/hr, 90 total spots, auto-gate off, notifications on
                    db.execSQL(
                        "INSERT INTO garage_settings (id, hourlyRateEgp, totalCapacity, autoOpenGate, pushNotifications) " +
                        "VALUES (1, 10.0, 90, 0, 1)"
                    )
                }
            }
        }
    }
}

package com.depi.graduationproject.core.di

import android.content.Context
import androidx.room.Room
import com.depi.graduationproject.data.local.GarageDatabase
import com.depi.graduationproject.data.local.dao.GarageSettingsDao
import com.depi.graduationproject.data.local.dao.ParkingSessionDao
import com.depi.graduationproject.data.local.dao.ZoneDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing Database and DAO dependencies.
 * Follows Hilt Guide and Phase 1 (T020) requirements.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGarageDatabase(
        @ApplicationContext context: Context
    ): GarageDatabase {
        return Room.databaseBuilder(
            context,
            GarageDatabase::class.java,
            GarageDatabase.DATABASE_NAME
        )
        .addCallback(GarageDatabase.getCallback())
        .fallbackToDestructiveMigration() // Useful for development, remove for production if needed
        .build()
    }

    @Provides
    @Singleton
    fun provideParkingSessionDao(db: GarageDatabase): ParkingSessionDao {
        return db.parkingSessionDao()
    }

    @Provides
    @Singleton
    fun provideZoneDao(db: GarageDatabase): ZoneDao {
        return db.zoneDao()
    }

    @Provides
    @Singleton
    fun provideGarageSettingsDao(db: GarageDatabase): GarageSettingsDao {
        return db.garageSettingsDao()
    }
}

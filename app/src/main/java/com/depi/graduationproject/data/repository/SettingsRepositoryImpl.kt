package com.depi.graduationproject.data.repository

import com.depi.graduationproject.data.local.dao.GarageSettingsDao
import com.depi.graduationproject.data.local.entity.GarageSettingsEntity
import com.depi.graduationproject.domain.model.GarageSettings
import com.depi.graduationproject.domain.repository.ISettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: GarageSettingsDao
) : ISettingsRepository {

    override fun getSettingsFlow(): Flow<GarageSettings> =
        settingsDao.getSettings()
            .filterNotNull()
            .map { it.toDomain() }

    override suspend fun getSettings(): GarageSettings = withContext(Dispatchers.IO) {
        val entity = settingsDao.getSettings().firstOrNull() 
            ?: return@withContext GarageSettings()
        entity.toDomain()
    }

    override suspend fun updateSettings(settings: GarageSettings) = withContext(Dispatchers.IO) {
        settingsDao.upsert(settings.toEntity())
    }

    // --- Mappers ---

    private fun GarageSettingsEntity.toDomain(): GarageSettings = GarageSettings(
        hourlyRateEgp = hourlyRateEgp,
        totalCapacity = totalCapacity,
        autoOpenGate = autoOpenGate,
        pushNotifications = pushNotifications
    )

    private fun GarageSettings.toEntity(): GarageSettingsEntity = GarageSettingsEntity(
        id = 1, // Singleton ID
        hourlyRateEgp = hourlyRateEgp,
        totalCapacity = totalCapacity,
        autoOpenGate = autoOpenGate,
        pushNotifications = pushNotifications
    )
}

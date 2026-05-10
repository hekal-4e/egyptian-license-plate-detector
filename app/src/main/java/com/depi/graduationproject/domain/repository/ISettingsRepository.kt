package com.depi.graduationproject.domain.repository

import com.depi.graduationproject.domain.model.GarageSettings
import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    fun getSettingsFlow(): Flow<GarageSettings>
    suspend fun getSettings(): GarageSettings
    suspend fun updateSettings(settings: GarageSettings)
}

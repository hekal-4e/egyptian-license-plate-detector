package com.depi.graduationproject.domain.usecase.settings

import com.depi.graduationproject.domain.model.GarageSettings
import com.depi.graduationproject.domain.repository.ISettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateSettingsUseCase @Inject constructor(
    private val settingsRepository: ISettingsRepository
) {
    suspend operator fun invoke(settings: GarageSettings): Result<Unit> = withContext(Dispatchers.Default) {
        // Validation
        if (settings.hourlyRateEgp <= 0) {
            return@withContext Result.failure(Exception("Hourly rate must be greater than zero"))
        }
        if (settings.totalCapacity <= 0) {
            return@withContext Result.failure(Exception("Total capacity must be greater than zero"))
        }
        
        settingsRepository.updateSettings(settings)
        return@withContext Result.success(Unit)
    }
}
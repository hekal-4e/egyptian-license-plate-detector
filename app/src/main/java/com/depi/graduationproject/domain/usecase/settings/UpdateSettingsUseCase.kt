package com.depi.graduationproject.domain.usecase.settings

import com.depi.graduationproject.domain.model.GarageSettings
import com.depi.graduationproject.domain.repository.ISettingsRepository
import javax.inject.Inject

/**
 * Validates and persists garage configuration.
 * Follows Phase 5c (T058).
 */
class UpdateSettingsUseCase @Inject constructor(
    private val settingsRepository: ISettingsRepository
) {
    suspend operator fun invoke(settings: GarageSettings): Result<Unit> {
        // Validation
        if (settings.hourlyRateEgp < 0) {
            return Result.failure(Exception("Hourly rate cannot be negative"))
        }
        if (settings.totalCapacity <= 0) {
            return Result.failure(Exception("Total capacity must be greater than zero"))
        }
        
        settingsRepository.updateSettings(settings)
        return Result.success(Unit)
    }
}

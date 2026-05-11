package com.depi.graduationproject.domain.usecase.checkout

import com.depi.graduationproject.domain.model.ParkingSession
import com.depi.graduationproject.domain.repository.IParkingRepository
import com.depi.graduationproject.domain.repository.ISettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.ceil

/**
 * Calculates duration and fee, then completes the parking session.
 * Follows Phase 5c (T056).
 */
class CheckoutUseCase @Inject constructor(
    private val parkingRepository: IParkingRepository,
    private val settingsRepository: ISettingsRepository
) {
    suspend operator fun invoke(sessionId: String): Result<CheckoutSummary> = withContext(Dispatchers.Default) {
        val session = parkingRepository.getSessionById(sessionId) 
            ?: return@withContext Result.failure(IllegalArgumentException("Session not found"))
            
        val settings = settingsRepository.getSettings()
        
        val currentTime = System.currentTimeMillis()
        val durationMillis = currentTime - session.entryTime
        
        // Calculate hours (ceil per FR-005)
        val durationHours = ceil(durationMillis / 3_600_000.0).toInt().coerceAtLeast(1)
        val totalFee = durationHours * settings.hourlyRateEgp
        
        parkingRepository.checkout(
            sessionId = sessionId,
            zoneId = session.zoneId,
            totalFee = totalFee,
            durationHours = durationHours
        )
        
        return@withContext Result.success(CheckoutSummary(
            session = session,
            durationHours = durationHours,
            totalFee = totalFee
        ))
    }

    data class CheckoutSummary(
        val session: ParkingSession,
        val durationHours: Int,
        val totalFee: Double
    )
}

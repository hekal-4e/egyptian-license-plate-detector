package com.depi.graduationproject.data.remote.supabase

import com.depi.graduationproject.domain.model.ParkingSession
import com.depi.graduationproject.domain.repository.ICloudRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ICloudRepository {
    override suspend fun uploadSessions(sessions: List<ParkingSession>): Result<Unit> {
        if (sessions.isEmpty()) return Result.success(Unit)

        return try {
            val dtos = sessions.map { it.toDto() }

            // Upload to "parking_sessions" table
            supabaseClient.postgrest["parking_sessions"].upsert(dtos)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ParkingSession.toDto(): ParkingSessionDto {
        return ParkingSessionDto(
            id = id,
            license_plate = licensePlate,
            zone_id = zoneId,
            spot_id = spotId,
            entry_time = entryTime,
            exit_time = exitTime,
            duration_hours = durationHours,
            hourly_rate_applied = hourlyRateApplied,
            total_fee = totalFee,
            status = status.name,
            sync_status = syncStatus.name,
            created_at = createdAt
        )
    }
}

@Serializable
data class ParkingSessionDto(
    val id: String,
    val license_plate: String,
    val zone_id: String,
    val spot_id: String?,
    val entry_time: Long,
    val exit_time: Long?,
    val duration_hours: Int?,
    val hourly_rate_applied: Double,
    val total_fee: Double?,
    val status: String,
    val sync_status: String,
    val created_at: Long
)
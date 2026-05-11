package com.depi.graduationproject.domain.repository

import com.depi.graduationproject.domain.model.ParkingSession

interface ICloudRepository {
    suspend fun uploadSessions(sessions: List<ParkingSession>): Result<Unit>
}

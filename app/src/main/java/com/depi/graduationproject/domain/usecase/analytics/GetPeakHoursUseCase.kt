package com.depi.graduationproject.domain.usecase.analytics

import com.depi.graduationproject.domain.repository.IParkingRepository
import java.util.Calendar
import javax.inject.Inject

class GetPeakHoursUseCase @Inject constructor(
    private val parkingRepository: IParkingRepository
) {
    suspend operator fun invoke(startTime: Long, endTime: Long): Map<Int, Int> {
        val sessions = parkingRepository.getByDateRange(startTime, endTime)
        
        val hourDistribution = mutableMapOf<Int, Int>()
        // Initialize all 24 hours
        for (i in 0..23) hourDistribution[i] = 0
        
        val calendar = Calendar.getInstance()
        
        sessions.forEach { session ->
            calendar.timeInMillis = session.entryTime
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            hourDistribution[hour] = (hourDistribution[hour] ?: 0) + 1
        }
        
        return hourDistribution
    }
}

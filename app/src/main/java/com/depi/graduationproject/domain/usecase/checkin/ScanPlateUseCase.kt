package com.depi.graduationproject.domain.usecase.checkin

import com.depi.graduationproject.domain.analyzer.IPlateAnalyzer
import com.depi.graduationproject.domain.model.ParkingSession
import com.depi.graduationproject.domain.model.PlateAnalysisResult
import com.depi.graduationproject.domain.repository.IParkingRepository
import javax.inject.Inject

class ScanPlateUseCase @Inject constructor(
    private val parkingRepository: IParkingRepository,
    private val plateAnalyzer: IPlateAnalyzer
) {
    suspend operator fun invoke(imageData: ByteArray): ScanResult {
        val analysis = plateAnalyzer.analyze(imageData)
        
        if (analysis.text.isEmpty()) {
            return ScanResult.NoPlateFound
        }

        // Check for duplicate active session (FR-012)
        val existingSession = parkingRepository.getActiveSessionByPlate(analysis.text)
        
        return if (existingSession != null) {
            ScanResult.AlreadyActive(existingSession, analysis)
        } else {
            ScanResult.Success(analysis)
        }
    }

    sealed class ScanResult {
        data class Success(val analysis: PlateAnalysisResult) : ScanResult()
        data class AlreadyActive(
            val session: ParkingSession,
            val analysis: PlateAnalysisResult
        ) : ScanResult()
        object NoPlateFound : ScanResult()
    }
}

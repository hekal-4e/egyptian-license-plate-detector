package com.depi.graduationproject.domain.usecase.checkin

import com.depi.graduationproject.core.utils.PlateUtils
import com.depi.graduationproject.domain.analyzer.IPlateAnalyzer
import com.depi.graduationproject.domain.model.ImageFrame
import com.depi.graduationproject.domain.model.ParkingSession
import com.depi.graduationproject.domain.model.PlateAnalysisResult
import com.depi.graduationproject.domain.repository.IParkingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ScanPlateUseCase @Inject constructor(
    private val parkingRepository: IParkingRepository,
    private val plateAnalyzer: IPlateAnalyzer
) {
    suspend operator fun invoke(imageFrame: ImageFrame): ScanResult = withContext(Dispatchers.Default) {
        val analysis = plateAnalyzer.analyze(imageFrame)

        if (analysis.text.isBlank() || !analysis.isSuccess) {
            return@withContext ScanResult.NoPlateFound
        }

        val normalizedPlate = PlateUtils.normalizeForStorage(analysis.text)
        if (
            normalizedPlate.isBlank() ||
            !PlateUtils.isValidV4Plate(normalizedPlate) ||
            analysis.confidence < MIN_DOMAIN_CONFIDENCE
        ) {
            return@withContext ScanResult.NoPlateFound
        }

        val normalizedAnalysis = if (normalizedPlate == analysis.text) {
            analysis
        } else {
            analysis.copy(text = normalizedPlate)
        }

        // Check for duplicate active session (FR-012)
        val existingSession = parkingRepository.getActiveSessionByPlate(normalizedPlate)

        if (existingSession != null) {
            ScanResult.AlreadyActive(existingSession, normalizedAnalysis)
        } else {
            ScanResult.Success(normalizedAnalysis)
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

    private companion object {
        private const val MIN_DOMAIN_CONFIDENCE = 0.60f
    }
}
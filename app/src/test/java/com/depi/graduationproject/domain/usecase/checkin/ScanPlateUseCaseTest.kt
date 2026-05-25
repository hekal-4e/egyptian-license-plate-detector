package com.depi.graduationproject.domain.usecase.checkin

import com.depi.graduationproject.domain.analyzer.IPlateAnalyzer
import com.depi.graduationproject.domain.model.ImageFrame
import com.depi.graduationproject.domain.model.PlateAnalysisResult
import com.depi.graduationproject.domain.repository.IParkingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScanPlateUseCaseTest {

    @Test
    fun invoke_lowConfidenceValidText_returnsNoPlateFound() = runTest {
        val repo = mockk<IParkingRepository>(relaxed = true)
        val analyzer = mockk<IPlateAnalyzer>()
        val analysis = PlateAnalysisResult(
            isSuccess = true,
            text = "123أب",
            imageBytes = null,
            message = "ok",
            confidence = 0.50f
        )
        coEvery { analyzer.analyze(any()) } returns analysis
        val useCase = ScanPlateUseCase(repo, analyzer)

        val result = useCase(ImageFrame(ByteArray(0), width = 1, height = 1))

        assertTrue(result is ScanPlateUseCase.ScanResult.NoPlateFound)
        coVerify(exactly = 0) { repo.getActiveSessionByPlate(any()) }
    }
}

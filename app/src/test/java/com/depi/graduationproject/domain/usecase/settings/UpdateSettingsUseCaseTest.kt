package com.depi.graduationproject.domain.usecase.settings

import com.depi.graduationproject.domain.model.GarageSettings
import com.depi.graduationproject.domain.repository.ISettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UpdateSettingsUseCaseTest {

    @Test
    fun invoke_hourlyRateZero_returnsFailure() = runTest {
        val repo = mockk<ISettingsRepository>(relaxed = true)
        val useCase = UpdateSettingsUseCase(repo)

        val result = useCase(GarageSettings(hourlyRateEgp = 0.0, totalCapacity = 10))

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repo.updateSettings(any()) }
    }

    @Test
    fun invoke_validSettings_updatesRepository() = runTest {
        val repo = mockk<ISettingsRepository>()
        coEvery { repo.updateSettings(any()) } just runs
        val useCase = UpdateSettingsUseCase(repo)
        val settings = GarageSettings(hourlyRateEgp = 12.0, totalCapacity = 80)

        val result = useCase(settings)

        assertFalse(result.isFailure)
        coVerify { repo.updateSettings(settings) }
    }
}

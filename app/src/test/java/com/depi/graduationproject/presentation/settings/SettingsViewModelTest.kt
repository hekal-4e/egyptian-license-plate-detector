package com.depi.graduationproject.presentation.settings

import com.depi.graduationproject.domain.model.GarageSettings
import com.depi.graduationproject.domain.model.Zone
import com.depi.graduationproject.domain.repository.IParkingRepository
import com.depi.graduationproject.domain.repository.ISettingsRepository
import com.depi.graduationproject.domain.usecase.settings.UpdateSettingsUseCase
import com.depi.graduationproject.testutils.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension(dispatcher)

    @Test
    fun updateHourlyRate_validInput_updatesSettingsAfterDebounce() = runTest(dispatcher) {
        val settingsFlow = MutableStateFlow(GarageSettings(hourlyRateEgp = 10.0, totalCapacity = 90))
        val settingsRepository = mockk<ISettingsRepository>()
        val parkingRepository = mockk<IParkingRepository>()
        val updateSettingsUseCase = mockk<UpdateSettingsUseCase>()

        every { settingsRepository.getSettingsFlow() } returns settingsFlow
        coEvery { settingsRepository.getSettings() } returns settingsFlow.value
        every { parkingRepository.getAllZonesFlow() } returns flowOf(emptyList<Zone>())
        coEvery { updateSettingsUseCase.invoke(any()) } returns Result.success(Unit)

        val viewModel = SettingsViewModel(settingsRepository, parkingRepository, updateSettingsUseCase)

        viewModel.updateHourlyRate("15")
        advanceTimeBy(300)
        advanceUntilIdle()

        coVerify { updateSettingsUseCase.invoke(settingsFlow.value.copy(hourlyRateEgp = 15.0)) }
    }

    @Test
    fun updateTotalCapacity_blankInput_doesNotSave() = runTest(dispatcher) {
        val settingsFlow = MutableStateFlow(GarageSettings(hourlyRateEgp = 10.0, totalCapacity = 90))
        val settingsRepository = mockk<ISettingsRepository>()
        val parkingRepository = mockk<IParkingRepository>()
        val updateSettingsUseCase = mockk<UpdateSettingsUseCase>()

        every { settingsRepository.getSettingsFlow() } returns settingsFlow
        coEvery { settingsRepository.getSettings() } returns settingsFlow.value
        every { parkingRepository.getAllZonesFlow() } returns flowOf(emptyList<Zone>())
        coEvery { updateSettingsUseCase.invoke(any()) } returns Result.success(Unit)

        val viewModel = SettingsViewModel(settingsRepository, parkingRepository, updateSettingsUseCase)

        viewModel.updateTotalCapacity("")
        advanceTimeBy(400)
        advanceUntilIdle()

        coVerify(exactly = 0) { updateSettingsUseCase.invoke(any()) }
    }
}

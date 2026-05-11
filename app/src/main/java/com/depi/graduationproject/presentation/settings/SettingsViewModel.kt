package com.depi.graduationproject.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.graduationproject.domain.model.GarageSettings
import com.depi.graduationproject.domain.model.Zone
import com.depi.graduationproject.domain.usecase.settings.UpdateSettingsUseCase
import com.depi.graduationproject.domain.repository.ISettingsRepository
import com.depi.graduationproject.domain.repository.IParkingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: ISettingsRepository,
    parkingRepository: IParkingRepository,
    private val updateSettingsUseCase: UpdateSettingsUseCase
) : ViewModel() {

    val settings: StateFlow<GarageSettings> = settingsRepository.getSettingsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GarageSettings()
        )

    val zones: StateFlow<List<Zone>> = parkingRepository.getAllZonesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val hourlyRateInput = MutableStateFlow<String?>(null)
    private val totalCapacityInput = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            hourlyRateInput
                .filterNotNull()
                .debounce(300)
                .collect { rate ->
                    val rateDouble = rate.toDoubleOrNull() ?: return@collect
                    updateFromRepository { current -> current.copy(hourlyRateEgp = rateDouble) }
                }
        }

        viewModelScope.launch {
            totalCapacityInput
                .filterNotNull()
                .debounce(300)
                .collect { capacity ->
                    val capacityInt = capacity.toIntOrNull() ?: return@collect
                    updateFromRepository { current -> current.copy(totalCapacity = capacityInt) }
                }
        }
    }

    fun updateHourlyRate(rate: String) {
        if (rate.isBlank()) return
        hourlyRateInput.value = rate
    }

    fun updateTotalCapacity(capacity: String) {
        if (capacity.isBlank()) return
        totalCapacityInput.value = capacity
    }

    fun toggleAutoGate(enabled: Boolean) {
        updateFromRepository { current -> current.copy(autoOpenGate = enabled) }
    }

    fun togglePushNotifications(enabled: Boolean) {
        updateFromRepository { current -> current.copy(pushNotifications = enabled) }
    }

    private fun updateFromRepository(transform: (GarageSettings) -> GarageSettings) {
        viewModelScope.launch {
            try {
                val current = settingsRepository.getSettings()
                val updated = transform(current)
                val result = updateSettingsUseCase(updated)
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class SettingsUiState(
    val error: String? = null
)
package com.depi.graduationproject.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.graduationproject.domain.repository.IParkingRepository
import com.depi.graduationproject.domain.repository.ISettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val parkingRepository: IParkingRepository,
    private val settingsRepository: ISettingsRepository
) : ViewModel() {

    // Combine active sessions and settings to determine if garage is full (T069)
    val uiState: StateFlow<DashboardUiState> = combine(
        parkingRepository.getActiveCountFlow(),
        settingsRepository.getSettingsFlow()
    ) { activeCount, settings ->
        DashboardUiState(
            activeSpots = activeCount,
            totalCapacity = settings.totalCapacity,
            isGarageFull = activeCount >= settings.totalCapacity
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}

data class DashboardUiState(
    val activeSpots: Int = 0,
    val totalCapacity: Int = 0,
    val isGarageFull: Boolean = false
)
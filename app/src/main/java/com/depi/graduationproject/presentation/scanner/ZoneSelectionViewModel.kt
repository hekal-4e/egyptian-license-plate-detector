package com.depi.graduationproject.presentation.scanner

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.graduationproject.core.utils.PlateUtils
import com.depi.graduationproject.domain.model.ParkingSession
import com.depi.graduationproject.domain.model.Zone
import com.depi.graduationproject.domain.repository.IParkingRepository
import com.depi.graduationproject.domain.usecase.checkin.ValidatePlateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ZoneSelectionViewModel @Inject constructor(
    private val parkingRepository: IParkingRepository,
    private val validatePlateUseCase: ValidatePlateUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val plateText: String = Uri.decode(savedStateHandle.get<String>("plateText") ?: "")

    val availableZones: StateFlow<List<Zone>> = parkingRepository.getAvailableZonesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(ZoneSelectionUiState())
    val uiState: StateFlow<ZoneSelectionUiState> = _uiState.asStateFlow()

    fun selectZoneAndCheckIn(zone: Zone, onSuccess: (String) -> Unit) {
        if (_uiState.value.isProcessing) return
        _uiState.value = _uiState.value.copy(isProcessing = true, error = null)

        viewModelScope.launch {
            try {
                val isValid = validatePlateUseCase(plateText)
                if (!isValid) {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        error = "Invalid plate text. Please go back and correct it."
                    )
                    return@launch
                }

                val normalizedPlate = PlateUtils.normalizeForStorage(plateText)

                // Generate a unique session ID
                val sessionId = UUID.randomUUID().toString()
                
                // Construct the session
                val session = ParkingSession(
                    id = sessionId,
                    licensePlate = normalizedPlate,
                    zoneId = zone.id,
                    spotId = null, // Auto-assigned by Edge if needed
                    entryTime = System.currentTimeMillis(),
                    hourlyRateApplied = 15.0 // Handled by settings in a real scenario
                )

                // Persist
                parkingRepository.checkIn(session)

                _uiState.value = _uiState.value.copy(isProcessing = false)
                onSuccess(sessionId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "Failed to check in."
                )
            }
        }
    }
}

data class ZoneSelectionUiState(
    val isProcessing: Boolean = false,
    val error: String? = null
)
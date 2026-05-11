package com.depi.graduationproject.presentation.scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.graduationproject.domain.model.ParkingSession
import com.depi.graduationproject.domain.repository.IParkingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val parkingRepository: IParkingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: String = savedStateHandle.get<String>("sessionId") ?: ""

    private val _uiState = MutableStateFlow(TicketUiState())
    val uiState: StateFlow<TicketUiState> = _uiState.asStateFlow()

    init {
        loadSession()
    }

    private fun loadSession() {
        if (sessionId.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Invalid session ID")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val session = parkingRepository.getSessionById(sessionId)
            if (session != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    session = session
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Session not found."
                )
            }
        }
    }
}

data class TicketUiState(
    val isLoading: Boolean = true,
    val session: ParkingSession? = null,
    val error: String? = null
)

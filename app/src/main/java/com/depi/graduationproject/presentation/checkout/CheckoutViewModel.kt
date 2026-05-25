package com.depi.graduationproject.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.graduationproject.core.utils.PlateUtils
import com.depi.graduationproject.domain.model.ParkingSession
import com.depi.graduationproject.domain.repository.IParkingRepository
import com.depi.graduationproject.domain.usecase.checkout.CheckoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val parkingRepository: IParkingRepository,
    private val checkoutUseCase: CheckoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        searchJob?.cancel()
        val normalizedQuery = PlateUtils.normalizeForStorage(query)
        if (normalizedQuery.length < 2) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            _uiState.value = _uiState.value.copy(isLoading = true)
            val results = parkingRepository.searchActiveByPlate(normalizedQuery)
            _uiState.value = _uiState.value.copy(
                searchResults = results,
                isLoading = false
            )
        }
    }

    fun onScanQrCode(sessionId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val session = parkingRepository.getSessionById(sessionId)
            if (session != null && session.status == com.depi.graduationproject.domain.model.SessionStatus.ACTIVE) {
                _uiState.value = _uiState.value.copy(
                    selectedSession = session,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Session not found or already completed."
                )
            }
        }
    }

    fun selectSession(session: ParkingSession) {
        _uiState.value = _uiState.value.copy(selectedSession = session)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedSession = null, checkoutSummary = null)
    }

    fun processPayment() {
        val session = _uiState.value.selectedSession ?: return
        _uiState.value = _uiState.value.copy(isProcessingPayment = true, error = null)
        
        viewModelScope.launch {
            val result = checkoutUseCase(session.id)
            result.fold(
                onSuccess = { summary ->
                    _uiState.value = _uiState.value.copy(
                        isProcessingPayment = false,
                        checkoutSummary = summary,
                        paymentSuccessful = true
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isProcessingPayment = false,
                        error = e.message ?: "Payment failed."
                    )
                }
            )
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class CheckoutUiState(
    val searchQuery: String = "",
    val searchResults: List<ParkingSession> = emptyList(),
    val selectedSession: ParkingSession? = null,
    val isLoading: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val paymentSuccessful: Boolean = false,
    val checkoutSummary: CheckoutUseCase.CheckoutSummary? = null,
    val error: String? = null
)

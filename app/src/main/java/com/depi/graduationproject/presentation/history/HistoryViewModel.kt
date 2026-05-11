package com.depi.graduationproject.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.graduationproject.domain.model.ParkingSession
import com.depi.graduationproject.domain.model.SessionStatus
import com.depi.graduationproject.domain.repository.IParkingRepository
import com.depi.graduationproject.domain.usecase.analytics.GetPeakHoursUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val parkingRepository: IParkingRepository,
    private val getPeakHoursUseCase: GetPeakHoursUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadDataForToday()
    }

    private fun loadDataForToday() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endOfDay = cal.timeInMillis

        loadData(startOfDay, endOfDay, "Today")
    }

    fun loadData(startTime: Long, endTime: Long, dateLabel: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, dateLabel = dateLabel)

        viewModelScope.launch {
            try {
                val sessions = parkingRepository.getByDateRange(startTime, endTime)
                val peakHours = getPeakHoursUseCase(startTime, endTime)

                val currentTime = System.currentTimeMillis()
                val sessionItems = sessions.map { session ->
                    val isOverstay = session.status == SessionStatus.ACTIVE &&
                            (currentTime - session.entryTime) > 24 * 60 * 60 * 1000L
                    HistorySessionItem(session, isOverstay)
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    sessions = sessionItems,
                    peakHoursMap = peakHours
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}

data class HistorySessionItem(
    val session: ParkingSession,
    val isOverstay: Boolean
)

data class HistoryUiState(
    val isLoading: Boolean = false,
    val dateLabel: String = "Today",
    val sessions: List<HistorySessionItem> = emptyList(),
    val peakHoursMap: Map<Int, Int> = emptyMap(),
    val error: String? = null
)

package com.depi.graduationproject.presentation.dashboard

import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.graduationproject.domain.repository.IParkingRepository
import com.depi.graduationproject.domain.repository.ISettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import com.depi.graduationproject.domain.model.ParkingSession
import javax.inject.Inject

import java.util.Calendar

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val parkingRepository: IParkingRepository,
    private val settingsRepository: ISettingsRepository
) : ViewModel() {

    private val _isEntryMode = MutableStateFlow(true)
    private val storageWarningThresholdBytes = 500L * 1024 * 1024
    private val todayRange: Pair<Long, Long> = run {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        start to cal.timeInMillis
    }

    private val metricsFlow = combine(
        parkingRepository.getActiveCountFlow(),
        settingsRepository.getSettingsFlow(),
        parkingRepository.getTodayRevenueFlow(todayRange.first, todayRange.second),
        parkingRepository.getActiveSessionsFlow()
    ) { activeCount, settings, revenue, activeSessions ->
        DashboardMetrics(
            activeCount = activeCount,
            totalCapacity = settings.totalCapacity,
            revenue = revenue,
            recentSessions = activeSessions.take(10)
        )
    }

    private val storageWarningFlow = flow {
        emit(isStorageLow())
        while (true) {
            delay(60_000)
            emit(isStorageLow())
        }
    }.distinctUntilChanged()

    val uiState: StateFlow<DashboardUiState> = combine(
        metricsFlow,
        _isEntryMode,
        storageWarningFlow
    ) { metrics, isEntryMode, isStorageLow ->
        DashboardUiState(
            activeSpots = metrics.activeCount,
            totalCapacity = metrics.totalCapacity,
            isGarageFull = metrics.activeCount >= metrics.totalCapacity,
            todaysRevenue = metrics.revenue,
            recentSessions = metrics.recentSessions,
            isEntryMode = isEntryMode,
            isStorageWarning = isStorageLow
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun toggleMode(isEntry: Boolean) {
        _isEntryMode.value = isEntry
    }

    private fun isStorageLow(): Boolean {
        val statFs = StatFs(Environment.getDataDirectory().absolutePath)
        return statFs.availableBytes < storageWarningThresholdBytes
    }
}

data class DashboardUiState(
    val activeSpots: Int = 0,
    val totalCapacity: Int = 0,
    val isGarageFull: Boolean = false,
    val todaysRevenue: Double = 0.0,
    val recentSessions: List<ParkingSession> = emptyList(),
    val isEntryMode: Boolean = true,
    val isStorageWarning: Boolean = false
)

private data class DashboardMetrics(
    val activeCount: Int,
    val totalCapacity: Int,
    val revenue: Double,
    val recentSessions: List<ParkingSession>
)
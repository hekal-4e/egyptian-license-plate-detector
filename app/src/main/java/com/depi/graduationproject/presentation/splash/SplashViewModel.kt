package com.depi.graduationproject.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.graduationproject.data.mlkit.TFLiteModelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val modelManager: TFLiteModelManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        initialize()
    }

    fun initialize() {
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            // Keep a short delay to smooth the transition without slowing startup.
            delay(300)

            modelManager.initializeModels()

            val ready = modelManager.isReady.first { it || modelManager.error.value != null }
            if (ready) {
                _uiState.value = _uiState.value.copy(isLoading = false, isReady = true, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, isReady = false, error = modelManager.error.value)
            }
        }
    }
}

data class SplashUiState(
    val isLoading: Boolean = false,
    val isReady: Boolean = false,
    val error: String? = null
)
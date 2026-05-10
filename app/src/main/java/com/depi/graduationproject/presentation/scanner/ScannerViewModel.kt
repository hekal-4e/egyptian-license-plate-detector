package com.depi.graduationproject.presentation.scanner

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.graduationproject.domain.analyzer.IPlateAnalyzer
import com.depi.graduationproject.domain.model.PlateAnalysisResult
import com.depi.graduationproject.domain.usecase.checkin.ScanPlateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scanPlateUseCase: ScanPlateUseCase,
    private val plateAnalyzer: IPlateAnalyzer // Just to check initialization state
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    init {
        // Initialize analyzer if needed off main thread (H3 fix)
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            if (!plateAnalyzer.isInitialized()) {
                plateAnalyzer.initialize()
            }
        }
    }

    fun processImage(bitmap: Bitmap) {
        if (_uiState.value.isProcessing || _uiState.value.showVerificationSheet) return

        _uiState.value = _uiState.value.copy(isProcessing = true)

        viewModelScope.launch {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val byteArray = stream.toByteArray()

                val result = scanPlateUseCase(byteArray)

                when (result) {
                    is ScanPlateUseCase.ScanResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            showVerificationSheet = true,
                            currentAnalysis = result.analysis,
                            duplicateSessionError = null
                        )
                    }
                    is ScanPlateUseCase.ScanResult.AlreadyActive -> {
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            showVerificationSheet = true, // We can still show it but warn
                            currentAnalysis = result.analysis,
                            duplicateSessionError = "Plate already active in zone ${result.session.zoneId}"
                        )
                    }
                    is ScanPlateUseCase.ScanResult.NoPlateFound -> {
                        _uiState.value = _uiState.value.copy(isProcessing = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    duplicateSessionError = "Error: ${e.message}"
                )
            }
        }
    }

    fun dismissVerification() {
        _uiState.value = _uiState.value.copy(
            showVerificationSheet = false,
            currentAnalysis = null,
            duplicateSessionError = null
        )
    }

    fun toggleFlashlight() {
        _uiState.value = _uiState.value.copy(isFlashlightOn = !_uiState.value.isFlashlightOn)
    }
}

data class ScannerUiState(
    val isProcessing: Boolean = false,
    val showVerificationSheet: Boolean = false,
    val currentAnalysis: PlateAnalysisResult? = null,
    val isFlashlightOn: Boolean = false,
    val duplicateSessionError: String? = null
)
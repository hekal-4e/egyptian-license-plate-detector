package com.depi.graduationproject.data.mlkit

import com.depi.graduationproject.domain.analyzer.IPlateAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TFLiteModelManager @Inject constructor(
    private val plateAnalyzer: IPlateAnalyzer
) {
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun initializeModels() = withContext(Dispatchers.Default) {
        if (plateAnalyzer.isInitialized()) {
            _isReady.value = true
            return@withContext
        }
        
        try {
            _error.value = null
            plateAnalyzer.initialize()
            if (plateAnalyzer.isInitialized()) {
                _isReady.value = true
            } else {
                _error.value = "Failed to initialize models."
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "Unknown error initializing models"
            _isReady.value = false
        }
    }
}

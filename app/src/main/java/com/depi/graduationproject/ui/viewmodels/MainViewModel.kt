package com.depi.graduationproject.ui.viewmodels

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.graduationproject.data.mlkit.IPlateAnalyzer
import com.depi.graduationproject.data.model.PlateAnalysisResult
import com.depi.graduationproject.repository.PlateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: PlateRepository,
    private val analyzer: IPlateAnalyzer
) : ViewModel() {

    val platesState = repository.getAllPlates()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _analysisResult = MutableStateFlow<PlateAnalysisResult?>(null)
    val analysisResult: StateFlow<PlateAnalysisResult?> = _analysisResult.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            analyzer.initialize()
            if (analyzer.isInitialized()) {
                _eventFlow.emit("Active OCR Model: ${analyzer.ocrModelName}")
            }
        }
    }

    fun processImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = analyzer.analyze(bitmap)

            _analysisResult.value = result
            _isLoading.value = false
            _showDialog.value = true
        }
    }

    fun saveCurrentPlate() {
        val result = _analysisResult.value
        Log.d("ViewModel", "Save button clicked. Current Result: $result")

        // Check if result exists and text is not empty
        if (result != null && result.text.isNotBlank()) {
            Log.d("ViewModel", "Saving text: ${result.text}")
            onPlateDetected(result.text)
            _showDialog.value = false
        } else {
            Log.e("ViewModel", "Save failed: Result is null or text is empty")
        }
    }

    fun dismissDialog() {
        _showDialog.value = false
    }

    fun onPlateDetected(plateNumber: String) {
        viewModelScope.launch {
            try {
                repository.insertPlate(plateNumber)
                Log.d("ViewModel", "Repository insert called successfully")
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to save to repository", e)
            }
        }
    }

    fun onPlateConfirmed(plateNumber: String) {
        viewModelScope.launch {
            Log.d("ParkingLogic", "User saved plate: $plateNumber")

            _showDialog.value = false
        }
    }

    fun deleteRecord(id: Int) {
        viewModelScope.launch { repository.deletePlate(id) }
    }

    fun clearAllHistory() {
        viewModelScope.launch { repository.clearAll() }
    }

    override fun onCleared() {
        analyzer.close()
        super.onCleared()
    }
}
package com.depi.graduationproject.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.depi.graduationproject.data.mlkit.LicensePlateAnalyzer
import com.depi.graduationproject.repository.PlateRepository

class MainViewModelFactory(
    private val repository: PlateRepository,
    private val analyzer: LicensePlateAnalyzer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, analyzer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
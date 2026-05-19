package com.depi.graduationproject.presentation.manualentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.depi.graduationproject.domain.usecase.checkin.ValidatePlateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ManualEntryViewModel @Inject constructor(
    private val validatePlateUseCase: ValidatePlateUseCase
) : ViewModel() {

    private val _numbers = MutableStateFlow("")
    val numbers: StateFlow<String> = _numbers.asStateFlow()

    private val _letters = MutableStateFlow("")
    val letters: StateFlow<String> = _letters.asStateFlow()

    val isValid: StateFlow<Boolean> = combine(_numbers, _letters) { nums, lets ->
        "$nums $lets".trim()
    }.map { combined ->
        validatePlateUseCase(combined)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun onNumbersChange(newNumbers: String) {
        _numbers.value = newNumbers
            .filter { it.isDigit() || it in '\u0660'..'\u0669' }
            .take(4)
    }

    fun onLettersChange(newLetters: String) {
        _letters.value = newLetters
            .filterNot { it.isDigit() || it in '\u0660'..'\u0669' }
            .replace("\\s+".toRegex(), " ")
            .trimStart()
            .take(7)
    }

    fun getCombinedPlate(): String {
        return "${_numbers.value} ${_letters.value}".trim()
    }
}
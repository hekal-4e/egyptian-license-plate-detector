package com.depi.graduationproject.domain.usecase.checkin

import javax.inject.Inject

class ValidatePlateUseCase @Inject constructor() {
    
    /**
     * Returns true if the plate text matches basic Egyptian requirements.
     * v1: Ensure it has at least one digit and one non-digit (Arabic letter).
     */
    operator fun invoke(plateText: String): Boolean {
        val cleanText = plateText.trim()
        if (cleanText.isEmpty()) return false
        
        val hasDigits = cleanText.any { it.isDigit() }
        val hasLetters = cleanText.any { !it.isDigit() && !it.isWhitespace() }
        
        // Simple validation for v1
        return hasDigits && hasLetters && cleanText.length >= 3
    }
}

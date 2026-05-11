package com.depi.graduationproject.domain.usecase.checkin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ValidatePlateUseCase @Inject constructor() {
    
    /**
     * Returns true if the plate text matches basic Egyptian requirements.
     * v1: Ensure it has at least one digit and one non-digit (Arabic letter).
     */
    suspend operator fun invoke(plateText: String): Boolean = withContext(Dispatchers.Default) {
        val cleanText = plateText.trim()
        if (cleanText.isEmpty()) return@withContext false

        val hasDigits = cleanText.any { it.isDigit() }
        val hasLetters = cleanText.any { !it.isDigit() && !it.isWhitespace() }

        // Simple validation for v1
        return@withContext hasDigits && hasLetters && cleanText.length >= 3
    }
}
package com.depi.graduationproject.core.utils

object PlateUtils {
    private val easternDigitRange = '\u0660'..'\u0669'

    fun splitPlateText(text: String): Pair<String, String> {
        val numbers = text.filter { it in easternDigitRange || it.isDigit() }
        val letters = text.filter { it !in easternDigitRange && !it.isDigit() && !it.isWhitespace() }
            .toCharArray()
            .joinToString(" ")
        return Pair(numbers, letters)
    }
}
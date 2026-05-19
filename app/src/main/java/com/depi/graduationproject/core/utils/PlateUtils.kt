package com.depi.graduationproject.core.utils

object PlateUtils {
    private val easternDigitRange = '\u0660'..'\u0669'

    fun splitPlateText(text: String): Pair<String, String> {
        val normalized = text.trim().replace("\\s+".toRegex(), " ")
        if (normalized.isEmpty()) return "" to ""

        val tokens = normalized.split(" ").filter { it.isNotBlank() }
        if (tokens.size >= 2) {
            val numberTokens = tokens.filter { token ->
                token.all { it in easternDigitRange || it.isDigit() }
            }
            val letterTokens = tokens.filterNot { token ->
                token.all { it in easternDigitRange || it.isDigit() }
            }

            val numbers = numberTokens.joinToString("")
            val letters = letterTokens.joinToString(" ").trim()
            if (numbers.isNotBlank() || letters.isNotBlank()) {
                return numbers to letters
            }
        }

        val numbers = normalized.filter { it in easternDigitRange || it.isDigit() }
        val letters = normalized
            .replace("[\\d\\u0660-\\u0669]+".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
        return Pair(numbers, letters)
    }
}
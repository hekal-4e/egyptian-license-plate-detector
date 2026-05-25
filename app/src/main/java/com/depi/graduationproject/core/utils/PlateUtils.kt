package com.depi.graduationproject.core.utils

object PlateUtils {
    internal val ARABIC_LETTERS = listOf(
        'أ', 'ب', 'ج', 'د', 'ر', 'س', 'ص', 'ط', 'ع', 'ف',
        'ق', 'ك', 'ل', 'م', 'ن', 'ه', 'و', 'ي'
    )

    private val arabicLetterSet = ARABIC_LETTERS.toSet()
    private val easternDigitRange = '\u0660'..'\u0669'
    private val asciiDigitRange = '0'..'9'
    private val separators = setOf('-', '_', '|')

    fun splitPlateText(text: String): Pair<String, String> {
        val normalized = normalizeForStorage(text)
        if (normalized.isEmpty()) return "" to ""

        val numbers = normalized.takeWhile { it in asciiDigitRange }
        val letters = normalized.drop(numbers.length)
        return numbers to letters
    }

    fun normalizeForStorage(text: String): String {
        if (text.isBlank()) return ""

        val digits = StringBuilder()
        val letters = StringBuilder()

        text.forEach { char ->
            when {
                char in easternDigitRange -> digits.append(toAsciiDigit(char))
                char in asciiDigitRange -> digits.append(char)
                arabicLetterSet.contains(char) -> letters.append(char)
                char.isWhitespace() || separators.contains(char) -> Unit
            }
        }
        return digits.append(letters).toString()
    }

    fun isValidV4Plate(text: String): Boolean {
        if (text.isBlank()) return false

        var digitCount = 0
        var letterCount = 0

        text.forEach { char ->
            when {
                char in easternDigitRange || char in asciiDigitRange -> digitCount++
                arabicLetterSet.contains(char) -> letterCount++
                char.isWhitespace() || separators.contains(char) -> Unit
                else -> return false
            }
        }

        return digitCount in 3..4 && letterCount in 2..3
    }

    private fun toAsciiDigit(char: Char): Char {
        val offset = char.code - easternDigitRange.first.code
        return (asciiDigitRange.first.code + offset).toChar()
    }
}
package com.depi.graduationproject.data.mlkit

import com.depi.graduationproject.core.utils.PlateUtils
import kotlin.math.exp

internal class PlateSlotDecoder(
    private val arabicLetters: List<Char> = PlateUtils.ARABIC_LETTERS,
    private val digits: List<Char> = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'),
    private val digitSlots: Int = DEFAULT_DIGIT_SLOTS,
    private val letterSlots: Int = DEFAULT_LETTER_SLOTS
) {

    data class DecodeResult(
        val text: String,
        val confidence: Float,
        val digitLength: Int,
        val letterLength: Int,
        val isValid: Boolean
    )

    fun decode(
        digitLogits: Array<FloatArray>,
        letterLogits: Array<FloatArray>,
        digitLenLogits: FloatArray,
        letterLenLogits: FloatArray
    ): DecodeResult {
        val digitLen = argmax(digitLenLogits) + DIGIT_LEN_OFFSET
        val letterLen = argmax(letterLenLogits) + LETTER_LEN_OFFSET

        val boundedDigitLen = digitLen.coerceAtMost(digitSlots).coerceAtMost(digitLogits.size)
        val boundedLetterLen = letterLen.coerceAtMost(letterSlots).coerceAtMost(letterLogits.size)
        val digitPadClass = digits.size
        val letterPadClass = arabicLetters.size

        val digitText = StringBuilder()
        val letterText = StringBuilder()
        var confidenceSum = 0f
        var confidenceCount = 0
        var hasPad = false

        for (slotIndex in 0 until boundedDigitLen) {
            val slot = digitLogits[slotIndex]
            val maxIndex = argmax(slot)
            if (maxIndex == digitPadClass || maxIndex !in digits.indices) {
                hasPad = true
                continue
            }
            digitText.append(digits[maxIndex])
            confidenceSum += maxSoftmaxProbability(slot)
            confidenceCount++
        }

        for (slotIndex in 0 until boundedLetterLen) {
            val slot = letterLogits[slotIndex]
            val maxIndex = argmax(slot)
            if (maxIndex == letterPadClass || maxIndex !in arabicLetters.indices) {
                hasPad = true
                continue
            }
            letterText.append(arabicLetters[maxIndex])
            confidenceSum += maxSoftmaxProbability(slot)
            confidenceCount++
        }

        val confidence = if (confidenceCount > 0) confidenceSum / confidenceCount else 0f
        val text = digitText.toString() + letterText.toString()
        val isValid = !hasPad &&
            digitLen in DIGIT_LEN_RANGE &&
            letterLen in LETTER_LEN_RANGE &&
            digitText.length == digitLen &&
            letterText.length == letterLen

        return DecodeResult(
            text = text,
            confidence = confidence,
            digitLength = digitLen,
            letterLength = letterLen,
            isValid = isValid
        )
    }

    private fun argmax(values: FloatArray): Int =
        values.indices.maxByOrNull { values[it] } ?: 0

    private fun maxSoftmaxProbability(values: FloatArray): Float {
        val maxVal = values.maxOrNull() ?: return 0f
        var sum = 0.0
        var maxProb = 0.0
        for (value in values) {
            val expValue = exp((value - maxVal).toDouble())
            sum += expValue
            if (expValue > maxProb) {
                maxProb = expValue
            }
        }
        return if (sum == 0.0) 0f else (maxProb / sum).toFloat()
    }

    private companion object {
        private const val DIGIT_LEN_OFFSET = 3
        private const val LETTER_LEN_OFFSET = 2
        private val DIGIT_LEN_RANGE = 3..4
        private val LETTER_LEN_RANGE = 2..3
        private const val DEFAULT_DIGIT_SLOTS = 4
        private const val DEFAULT_LETTER_SLOTS = 3
    }
}

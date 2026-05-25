package com.depi.graduationproject.data.mlkit

import com.depi.graduationproject.core.utils.PlateUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlateSlotDecoderTest {

    private val decoder = PlateSlotDecoder()

    @Test
    fun decode_validFourDigitThreeLetterLogits_returnsCanonicalPlate() {
        val letterClasses = PlateUtils.ARABIC_LETTERS.size + 1
        val digitLogits = arrayOf(
            logits(11, 3),
            logits(11, 8),
            logits(11, 4),
            logits(11, 2)
        )
        val letterLogits = arrayOf(
            logits(letterClasses, 0),
            logits(letterClasses, 1),
            logits(letterClasses, 2)
        )
        val digitLenLogits = floatArrayOf(0f, 5f)
        val letterLenLogits = floatArrayOf(0f, 5f)

        val result = decoder.decode(digitLogits, letterLogits, digitLenLogits, letterLenLogits)

        assertTrue(result.isValid)
        assertEquals("3842أبج", result.text)
        assertEquals(4, result.digitLength)
        assertEquals(3, result.letterLength)
    }

    @Test
    fun decode_selectedSlotPredictsPad_marksInvalid() {
        val letterClasses = PlateUtils.ARABIC_LETTERS.size + 1
        val digitLogits = arrayOf(
            logits(11, 10),
            logits(11, 1),
            logits(11, 2),
            logits(11, 3)
        )
        val letterLogits = arrayOf(
            logits(letterClasses, 0),
            logits(letterClasses, 1),
            logits(letterClasses, 2)
        )
        val digitLenLogits = floatArrayOf(5f, 0f)
        val letterLenLogits = floatArrayOf(5f, 0f)

        val result = decoder.decode(digitLogits, letterLogits, digitLenLogits, letterLenLogits)

        assertFalse(result.isValid)
    }

    @Test
    fun decode_flatLogits_returnsLowConfidenceDefaultText() {
        val letterClasses = PlateUtils.ARABIC_LETTERS.size + 1
        val digitLogits = Array(4) { FloatArray(11) { 0f } }
        val letterLogits = Array(3) { FloatArray(letterClasses) { 0f } }
        val digitLenLogits = floatArrayOf(0f, 0f)
        val letterLenLogits = floatArrayOf(0f, 0f)

        val result = decoder.decode(digitLogits, letterLogits, digitLenLogits, letterLenLogits)

        assertEquals("000أأ", result.text)
        assertTrue(result.confidence < 0.10f)
    }

    private fun logits(size: Int, maxIndex: Int): FloatArray =
        FloatArray(size) { index -> if (index == maxIndex) 5f else 0f }
}

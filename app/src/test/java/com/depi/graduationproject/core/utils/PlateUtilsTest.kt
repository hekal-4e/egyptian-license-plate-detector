package com.depi.graduationproject.core.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlateUtilsTest {

    @Test
    fun normalizeForStorage_displayStyleArabicDigits_returnsCanonicalAsciiDigits() {
        val input = "س م ل ٣٨٤٢"

        val result = PlateUtils.normalizeForStorage(input)

        assertEquals("3842سمل", result)
    }

    @Test
    fun isValidV4Plate_twoDigitsAndThreeLetters_returnsFalse() {
        val result = PlateUtils.isValidV4Plate("12أبج")

        assertFalse(result)
    }

    @Test
    fun isValidV4Plate_threeDigitsThreeLetters_returnsTrue() {
        val result = PlateUtils.isValidV4Plate("384سمل")

        assertTrue(result)
    }
}

package com.depi.graduationproject.presentation.scanner

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScannerConfirmLogicTest {

    @Test
    fun isScannerConfirmEnabled_invalidPlate_returnsFalse() {
        val result = isScannerConfirmEnabled(
            isVerified = false,
            isEdited = false,
            numbers = "1",
            letters = ""
        )

        assertFalse(result)
    }

    @Test
    fun isScannerConfirmEnabled_validPlate_returnsTrue() {
        val result = isScannerConfirmEnabled(
            isVerified = false,
            isEdited = true,
            numbers = "1234",
            letters = "أبج"
        )

        assertTrue(result)
    }

    @Test
    fun isScannerConfirmEnabled_uneditedValidNeedsReview_returnsTrue() {
        val result = isScannerConfirmEnabled(
            isVerified = false,
            isEdited = false,
            numbers = "123",
            letters = "أب"
        )

        assertTrue(result)
    }
}

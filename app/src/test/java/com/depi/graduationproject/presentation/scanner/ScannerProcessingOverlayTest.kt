package com.depi.graduationproject.presentation.scanner

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScannerProcessingOverlayTest {

    @Test
    fun shouldShowScannerProcessingOverlay_processingWithoutSheet_returnsTrue() {
        val result = shouldShowScannerProcessingOverlay(
            isProcessing = true,
            showProcessingOverlay = true,
            showVerificationSheet = false
        )

        assertTrue(result)
    }

    @Test
    fun shouldShowScannerProcessingOverlay_processingWithSheet_returnsFalse() {
        val result = shouldShowScannerProcessingOverlay(
            isProcessing = true,
            showProcessingOverlay = true,
            showVerificationSheet = true
        )

        assertFalse(result)
    }
}

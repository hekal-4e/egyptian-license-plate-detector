package com.depi.graduationproject.presentation.scanner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.core.utils.PlateUtils
import com.depi.graduationproject.presentation.components.CameraPermissionWrapper
import com.depi.graduationproject.presentation.components.CameraPreview
import com.depi.graduationproject.presentation.components.PlateVerificationSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onNavigateToZoneSelection: (String) -> Unit,
    onNavigateToCheckout: (String) -> Unit,
    onNavigateToManualEntry: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPermissionWrapper(
            onPermissionDenied = { requestPermission ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Camera permission is required to scan plates.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = requestPermission) {
                        Text("GRANT PERMISSION")
                    }
                }
            }
        ) {
            CameraPreview(
                onImageCaptured = { bitmap ->
                    if (!uiState.isProcessing) {
                        viewModel.processImage(bitmap)
                    }
                },
                isFlashlightOn = uiState.isFlashlightOn,
                modifier = Modifier.fillMaxSize()
            )
        }

        ScannerScreenContent(
            isFlashlightOn = uiState.isFlashlightOn,
            isProcessing = uiState.isProcessing,
            onClose = onClose,
            onFlashlightToggle = { viewModel.toggleFlashlight() },
            onCapture = { },
            onManualEntry = onNavigateToManualEntry,
            modifier = Modifier.fillMaxSize()
        )
    }

    if (uiState.showVerificationSheet && uiState.currentAnalysis != null) {
        val (detectedNumbers, detectedLetters) = PlateUtils.splitPlateText(uiState.currentAnalysis!!.text)
        val numbers = uiState.correctedPlateNumbers.ifEmpty { detectedNumbers }
        val letters = uiState.correctedPlateLetters.ifEmpty { detectedLetters }
        val bottomSheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissVerification() },
            sheetState = bottomSheetState,
            containerColor = com.depi.graduationproject.core.theme.PanelSurface
        ) {
            PlateVerificationSheet(
                plateNumbers = numbers,
                plateLetters = letters,
                croppedPlateImageBytes = uiState.currentAnalysis?.imageBytes,
                isVerified = uiState.duplicateSessionError == null,
                duplicateError = uiState.duplicateSessionError,
                onNumbersChanged = { viewModel.updateCorrectedPlateNumbers(it) },
                onLettersChanged = { viewModel.updateCorrectedPlateLetters(it) },
                onRetake = { viewModel.dismissVerification() },
                onConfirm = {
                    val correctedText = viewModel.getCorrectedPlateText()
                    viewModel.dismissVerification()
                    if (uiState.duplicateSessionError != null) {
                        onNavigateToCheckout(correctedText)
                    } else {
                        onNavigateToZoneSelection(correctedText)
                    }
                }
            )
        }
    }
}

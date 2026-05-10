package com.depi.graduationproject.presentation.scanner

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.core.theme.EmeraldGreen
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.presentation.components.GradientButton
import com.depi.graduationproject.presentation.components.PlateDisplay
import com.depi.graduationproject.presentation.components.SecondaryButton
import com.depi.graduationproject.presentation.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = hiltViewModel(),
    onNavigateToZoneSelection: (String) -> Unit,
    onNavigateToCheckout: (String) -> Unit // For duplicate plates
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        CameraPreview(
            onImageCaptured = { bitmap ->
                if (!uiState.isProcessing) {
                    viewModel.processImage(bitmap)
                }
            },
            isFlashlightOn = uiState.isFlashlightOn,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Align license plate within the frame",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )

            // Center brackets would go here (omitted for brevity, assume drawn in CameraPreview)
            Spacer(modifier = Modifier.weight(1f))

            if (uiState.isProcessing) {
                CircularProgressIndicator(color = Color(0xFFFF2A7A))
            }

            Button(
                onClick = { viewModel.toggleFlashlight() },
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(if (uiState.isFlashlightOn) "Turn Flash Off" else "Turn Flash On")
            }
        }
    }

    // Verification Bottom Sheet (T063)
    if (uiState.showVerificationSheet && uiState.currentAnalysis != null) {
        val plateText = uiState.currentAnalysis!!.text
        val plateParts = remember(plateText) { splitPlateText(plateText) }
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissVerification() },
            sheetState = sheetState,
            containerColor = Color(0xFF1A1D24)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatusBadge(
                    text = if (uiState.duplicateSessionError != null) "ALREADY ACTIVE" else "AI VERIFIED",
                    color = if (uiState.duplicateSessionError != null) NeonPink else EmeraldGreen
                )

                PlateDisplay(
                    numbers = plateParts.numbers,
                    letters = plateParts.letters,
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.duplicateSessionError != null) {
                    Text(
                        text = uiState.duplicateSessionError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    GradientButton(
                        text = "GO TO CHECKOUT",
                        onClick = {
                            viewModel.dismissVerification()
                            onNavigateToCheckout(uiState.currentAnalysis!!.text)
                        }
                    )
                } else {
                    GradientButton(
                        text = "CONFIRM & SELECT ZONE",
                        onClick = {
                            viewModel.dismissVerification()
                            onNavigateToZoneSelection(uiState.currentAnalysis!!.text)
                        }
                    )
                }

                SecondaryButton(
                    text = "RETAKE",
                    onClick = { viewModel.dismissVerification() }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private data class PlateParts(
    val numbers: String,
    val letters: String
)

private fun splitPlateText(text: String): PlateParts {
    val numbers = text.filter { it.isDigit() }
    val letters = text.filter { !it.isDigit() && !it.isWhitespace() }.toCharArray().joinToString(" ")
    return PlateParts(numbers = numbers, letters = letters)
}

@Composable
private fun CameraPreview(
    onImageCaptured: (Bitmap) -> Unit,
    isFlashlightOn: Boolean,
    modifier: Modifier = Modifier
) {
    // Placeholder composable until CameraX preview is wired in presentation components.
    Box(modifier = modifier)
}
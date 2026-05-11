package com.depi.graduationproject.presentation.scanner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.core.theme.EmeraldGreen
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.utils.PlateUtils
import com.depi.graduationproject.presentation.components.GradientButton
import com.depi.graduationproject.presentation.components.PlateDisplay
import com.depi.graduationproject.presentation.components.SecondaryButton
import com.depi.graduationproject.presentation.components.StatusBadge
import com.depi.graduationproject.core.utils.HapticType
import com.depi.graduationproject.core.utils.rememberHapticFeedback
import com.depi.graduationproject.presentation.components.CameraPreview
import com.depi.graduationproject.presentation.components.CameraPermissionWrapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = hiltViewModel(),
    onNavigateToZoneSelection: (String) -> Unit,
    onNavigateToCheckout: (String) -> Unit, // For duplicate plates
    onNavigateToManualEntry: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val haptic = rememberHapticFeedback()

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        CameraPermissionWrapper(
            onPermissionDenied = { requestPermission ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Camera permission is required to scan plates.")
                    Spacer(modifier = Modifier.height(12.dp))
                    SecondaryButton(
                        text = "GRANT PERMISSION",
                        onClick = requestPermission
                    )
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

            TextButton(
                onClick = onNavigateToManualEntry,
                modifier = Modifier.semantics { contentDescription = "Navigate to manual plate entry" }
            ) {
                Text("Can't scan? Enter manually")
            }

            Button(
                onClick = {
                    haptic(HapticType.TOGGLE)
                    viewModel.toggleFlashlight()
                },
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .semantics {
                        contentDescription = if (uiState.isFlashlightOn) {
                            "Turn flashlight off"
                        } else {
                            "Turn flashlight on"
                        }
                    }
            ) {
                Text(if (uiState.isFlashlightOn) "Turn Flash Off" else "Turn Flash On")
            }
        }
    }

    // Verification Bottom Sheet (T063)
    if (uiState.showVerificationSheet && uiState.currentAnalysis != null) {
        val (numbers, letters) = PlateUtils.splitPlateText(uiState.currentAnalysis!!.text)
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
                    numbers = numbers,
                    letters = letters,
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
                        },
                        modifier = Modifier.semantics { contentDescription = "Plate already active. Go to checkout." }
                    )
                } else {
                    GradientButton(
                        text = "CONFIRM & SELECT ZONE",
                        onClick = {
                            viewModel.dismissVerification()
                            onNavigateToZoneSelection(uiState.currentAnalysis!!.text)
                        },
                        modifier = Modifier.semantics { contentDescription = "Confirm plate and proceed to zone selection" }
                    )
                }

                SecondaryButton(
                    text = "RETAKE",
                    onClick = { viewModel.dismissVerification() },
                    modifier = Modifier.semantics { contentDescription = "Reject result and retake photo" }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
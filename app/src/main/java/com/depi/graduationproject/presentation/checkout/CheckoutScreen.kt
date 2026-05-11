package com.depi.graduationproject.presentation.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.core.theme.CardSurface
import com.depi.graduationproject.core.theme.GraduationProjectTheme
import com.depi.graduationproject.domain.model.ParkingSession
import com.depi.graduationproject.presentation.components.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.depi.graduationproject.core.utils.PlateUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.RGBLuminanceSource

@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    scannedSessionId: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var showQrScanner by remember { mutableStateOf(false) }

    LaunchedEffect(scannedSessionId) {
        if (scannedSessionId != null) {
            viewModel.onScanQrCode(scannedSessionId)
        }
    }

    CheckoutScreenContent(
        uiState = uiState,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSessionSelected = viewModel::selectSession,
        onProcessPayment = viewModel::processPayment,
        onClearSelection = viewModel::clearSelection,
        onDismissError = viewModel::dismissError,
        onDone = onNavigateBack,
        onScanQrClick = { showQrScanner = true }
    )

    if (showQrScanner) {
        QrScannerBottomSheet(
            onDismiss = { showQrScanner = false },
            onQrScanned = { sessionId ->
                showQrScanner = false
                viewModel.onScanQrCode(sessionId)
            }
        )
    }
}

@Composable
fun CheckoutScreenContent(
    uiState: CheckoutUiState,
    onSearchQueryChanged: (String) -> Unit,
    onSessionSelected: (ParkingSession) -> Unit,
    onProcessPayment: () -> Unit,
    onClearSelection: () -> Unit,
    onDismissError: () -> Unit,
    onDone: () -> Unit,
    onScanQrClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Checkout",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val summary = uiState.checkoutSummary
        if (uiState.paymentSuccessful && summary != null) {
            PaymentSuccessView(
                summary = summary,
                onDone = onDone
            )
            return
        }

        if (uiState.selectedSession == null) {
            // Search State
            SecondaryButton(
                text = "SCAN QR TICKET",
                onClick = onScanQrClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .semantics { contentDescription = "Scan QR ticket to select session" }
            )
            AppSearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChanged,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(3) {
                        ShimmerPlaceholder(height = 72.dp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.searchResults) { session ->
                        ZoneCard(
                            zoneLetter = session.zoneId.take(1).uppercase(),
                            zoneName = "Zone ${session.zoneId}",
                            subtitle = session.licensePlate,
                            spotsLeft = 1, // Doesn't matter here
                            contentDescriptionOverride = "Session ${session.licensePlate} in Zone ${session.zoneId}",
                            onClick = { onSessionSelected(session) }
                        )
                    }
                }
            }
        } else {
            // Billing Summary State
            SecondaryButton(
                text = "BACK TO SEARCH",
                onClick = onClearSelection,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            BillingSummaryView(
                session = uiState.selectedSession,
                isProcessing = uiState.isProcessingPayment,
                onProcessPayment = onProcessPayment
            )
        }

        if (uiState.error != null) {
            AlertDialog(
                onDismissRequest = onDismissError,
                title = { Text("Error") },
                text = { Text(uiState.error) },
                confirmButton = {
                    TextButton(onClick = onDismissError) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun BillingSummaryView(
    session: ParkingSession,
    isProcessing: Boolean,
    onProcessPayment: () -> Unit
) {
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    val entryTimeStr = format.format(Date(session.entryTime))

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        val plateParts = PlateUtils.splitPlateText(session.licensePlate)
        PlateDisplay(
            numbers = plateParts.first,
            letters = plateParts.second,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardSurface, MaterialTheme.shapes.large)
                .padding(16.dp)
        ) {
            Column {
                BillingRow(
                    icon = Icons.Default.LocationOn,
                    label = "Parking Zone",
                    value = "Zone ${session.zoneId}"
                )
                BillingRow(
                    icon = Icons.Default.AccessTime,
                    label = "Entry Time",
                    value = entryTimeStr
                )
                BillingRow(
                    icon = Icons.Default.Payment,
                    label = "Status",
                    value = "Pending Payment"
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        GradientButton(
            text = "PAY & OPEN GATE",
            onClick = onProcessPayment,
            isLoading = isProcessing,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Confirm payment and open gate" }
        )
    }
}

@Composable
fun PaymentSuccessView(
    summary: com.depi.graduationproject.domain.usecase.checkout.CheckoutUseCase.CheckoutSummary,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Payment Successful!",
            style = MaterialTheme.typography.headlineMedium,
            color = com.depi.graduationproject.core.theme.EmeraldGreen,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Total Fee: EGP ${summary.totalFee}",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        GradientButton(
            text = "DONE",
            onClick = onDone,
            showArrow = false,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun QrScannerBottomSheet(
    onDismiss: () -> Unit,
    onQrScanned: (String) -> Unit
) {
    var lastScanAt by remember { mutableStateOf(0L) }
    var isScanComplete by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Scan Ticket QR",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            CameraPermissionWrapper(
                onPermissionDenied = { requestPermission ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera permission is required to scan QR codes.")
                        Spacer(modifier = Modifier.height(12.dp))
                        SecondaryButton(
                            text = "GRANT PERMISSION",
                            onClick = requestPermission
                        )
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .background(Color.Black, MaterialTheme.shapes.large)
                ) {
                    CameraPreview(
                        onImageCaptured = { bitmap ->
                            if (isScanComplete) return@CameraPreview
                            val now = System.currentTimeMillis()
                            if (now - lastScanAt < 600) return@CameraPreview
                            lastScanAt = now

                            val text = decodeQrFromBitmap(bitmap)
                            if (text != null) {
                                isScanComplete = true
                                onQrScanned(text)
                            }
                        },
                        isFlashlightOn = false,
                        modifier = Modifier.fillMaxSize(),
                        analysisIntervalMs = 500L
                    )
                }
            }

            Text(
                text = "Align the QR code inside the frame.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

private fun decodeQrFromBitmap(bitmap: android.graphics.Bitmap): String? {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    val source = RGBLuminanceSource(width, height, pixels)
    val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
    val reader = MultiFormatReader().apply {
        setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)))
    }

    return try {
        reader.decode(binaryBitmap).text
    } catch (_: NotFoundException) {
        null
    } finally {
        reader.reset()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun CheckoutScreenPreview() {
    GraduationProjectTheme {
        CheckoutScreenContent(
            uiState = CheckoutUiState(searchQuery = "123"),
            onSearchQueryChanged = {},
            onSessionSelected = {},
            onProcessPayment = {},
            onClearSelection = {},
            onDismissError = {},
            onDone = {},
            onScanQrClick = {}
        )
    }
}
package com.depi.graduationproject.presentation.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.core.theme.*
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
        onScanQrClick = { showQrScanner = true },
        onBackClick = onNavigateBack
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
    onScanQrClick: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(LprDimens.ScreenPadding)
            .verticalScroll(rememberScrollState())
    ) {
        BackHeader(
            title = "Checkout",
            onBackClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
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
            Spacer(modifier = Modifier.height(8.dp))

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
                placeholder = "Search License Plate / Lost Ticket",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                        val zoneLetter = session.zoneId.take(1).uppercase()
                        val (numbers, letters) = PlateUtils.splitPlateText(session.licensePlate)
                        ZoneOptionCard(
                            zoneName = "Zone ${session.zoneId}",
                            zoneDescription = session.licensePlate,
                            spotsLeft = 1,
                            isSelected = false,
                            onClick = { onSessionSelected(session) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            val session = uiState.selectedSession
            val (numbers, letters) = PlateUtils.splitPlateText(session.licensePlate)

            val exitTimeNow = System.currentTimeMillis()
            val durationHours = calculateHours(session.entryTime, exitTimeNow)
            val hourlyRate = session.hourlyRateApplied
            val totalFee = durationHours * hourlyRate

            Spacer(modifier = Modifier.height(16.dp))

            ScanResultHeaderCard(
                plateNumbers = numbers,
                plateLetters = letters,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CarLocationCard(
                zoneName = "Zone ${session.zoneId}",
                spotId = session.spotId ?: "A-12",
                rowNumber = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(LprDimens.CardRadius))
                    .background(PanelSurface)
                    .padding(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Billing Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )

                    BillingRow(
                        icon = Icons.Default.AccessTime,
                        label = "Entry Time",
                        value = formatTime(session.entryTime),
                        dateText = formatDate(session.entryTime)
                    )
                    BillingRow(
                        icon = Icons.Default.AccessTime,
                        label = "Exit Time",
                        value = formatTime(exitTimeNow),
                        dateText = formatDate(exitTimeNow)
                    )
                    BillingRow(
                        icon = Icons.Default.AccessTime,
                        label = "Duration",
                        value = String.format(Locale.US, "%.1f hours", durationHours),
                        dateText = null
                    )
                    BillingRow(
                        icon = Icons.Default.Payment,
                        label = "Hourly Rate",
                        value = String.format(Locale.US, "%.2f EGP", hourlyRate),
                        dateText = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GradientFeeText(
                fee = String.format(Locale.US, "%.2f EGP", totalFee),
                label = "TOTAL FEE",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            GradientButton(
                text = "PAY & OPEN GATE",
                onClick = onProcessPayment,
                isLoading = uiState.isProcessingPayment,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
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

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
}

private fun calculateHours(start: Long, end: Long): Double {
    val diffMs = end - start
    return (diffMs / (1000.0 * 60.0 * 60.0)).coerceAtLeast(0.5)
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
            color = EmeraldGreen,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Total Fee: EGP ${summary.totalFee}",
            style = MaterialTheme.typography.headlineLarge,
            color = PrimaryText,
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
                color = PrimaryText
            )

            CameraPermissionWrapper(
                onPermissionDenied = { requestPermission ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera permission is required to scan QR codes.", color = PrimaryText)
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
                color = PrimaryText
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
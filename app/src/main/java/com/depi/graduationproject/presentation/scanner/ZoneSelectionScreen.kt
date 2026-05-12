package com.depi.graduationproject.presentation.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.core.theme.*
import com.depi.graduationproject.domain.model.Zone
import com.depi.graduationproject.presentation.components.*

@Composable
fun ZoneSelectionScreen(
    viewModel: ZoneSelectionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToTicket: (String) -> Unit
) {
    val availableZones by viewModel.availableZones.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var selectedZoneId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(LprDimens.ScreenPadding)
    ) {
        BackHeader(
            title = "Select Zone",
            onBackClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LprDimens.CardRadius))
                .background(PanelSurface)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AI Verified",
                            style = TinyCaps,
                            color = EmeraldGreen
                        )
                    }
                    Text(
                        text = "Detected in 0.3s",
                        style = TinyCaps,
                        color = MutedText
                    )
                }

                val (numbers, letters) = com.depi.graduationproject.core.utils.PlateUtils.splitPlateText(viewModel.plateText)
                PlateDisplay(
                    numbers = numbers,
                    letters = letters,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(
            title = "Select Parking Zone",
            icon = Icons.Default.LocationOn,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isProcessing) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonPink)
            }
        } else if (availableZones.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No zones available currently.", color = SecondaryText)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(availableZones) { zone ->
                    val zoneLetter = zone.name.lastOrNull()?.uppercase()?.toString() ?: "A"
                    ZoneOptionCard(
                        zoneName = zone.name,
                        zoneDescription = zone.description,
                        spotsLeft = zone.spotsAvailable,
                        isSelected = selectedZoneId == zone.id,
                        onClick = { selectedZoneId = zone.id },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        GradientButton(
            text = "CONFIRM CHECK-IN",
            onClick = {
                selectedZoneId?.let { zoneId ->
                    availableZones.find { it.id == zoneId }?.let { zone ->
                        viewModel.selectZoneAndCheckIn(zone) { sessionId ->
                            onNavigateToTicket(sessionId)
                        }
                    }
                }
            },
            enabled = selectedZoneId != null,
            isLoading = uiState.isProcessing,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
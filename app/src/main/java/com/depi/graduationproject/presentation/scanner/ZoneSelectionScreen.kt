package com.depi.graduationproject.presentation.scanner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.domain.model.Zone
import com.depi.graduationproject.presentation.components.ZoneCard

@Composable
fun ZoneSelectionScreen(
    viewModel: ZoneSelectionViewModel = hiltViewModel(),
    onNavigateToTicket: (String) -> Unit
) {
    val availableZones by viewModel.availableZones.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Select Parking Zone",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Vehicle: ${viewModel.plateText}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (uiState.isProcessing) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (availableZones.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No zones available currently.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(availableZones) { zone ->
                    ZoneCard(
                        zoneLetter = zoneLetter(zone),
                        zoneName = zone.name,
                        subtitle = zone.description,
                        spotsLeft = zone.spotsAvailable,
                        onClick = {
                            viewModel.selectZoneAndCheckIn(zone) { sessionId ->
                                onNavigateToTicket(sessionId)
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun zoneLetter(zone: Zone): String {
    val fromName = zone.name.firstOrNull()?.uppercase()
    return fromName ?: zone.id.take(1).uppercase()
}
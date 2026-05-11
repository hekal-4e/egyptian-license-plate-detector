package com.depi.graduationproject.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.core.theme.CardSurface
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.presentation.components.BottomNavLayout
import com.depi.graduationproject.presentation.components.NavItem
import com.depi.graduationproject.core.utils.HapticType
import com.depi.graduationproject.core.utils.rememberHapticFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToManualEntry: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val zones by viewModel.zones.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val haptic = rememberHapticFeedback()

    var hourlyRateInput by remember { mutableStateOf("") }
    var totalCapacityInput by remember { mutableStateOf("") }

    LaunchedEffect(settings.hourlyRateEgp) {
        hourlyRateInput = if (settings.hourlyRateEgp > 0) settings.hourlyRateEgp.toString() else ""
    }

    LaunchedEffect(settings.totalCapacity) {
        totalCapacityInput = if (settings.totalCapacity > 0) settings.totalCapacity.toString() else ""
    }

    Scaffold(
        bottomBar = {
            BottomNavLayout(
                selectedItem = NavItem.SETTINGS,
                onItemSelected = { item ->
                    when (item) {
                        NavItem.HOME -> onNavigateToHome()
                        NavItem.HISTORY -> onNavigateToHistory()
                        NavItem.ADD -> onNavigateToManualEntry()
                        NavItem.SETTINGS -> {} // Current
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Pricing Configuration
            Text(
                text = "Pricing Configuration",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = hourlyRateInput,
                        onValueChange = {
                            hourlyRateInput = it
                            viewModel.updateHourlyRate(it)
                        },
                        label = { Text("Hourly Rate") },
                        suffix = { Text("EGP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CardSurface,
                            unfocusedContainerColor = CardSurface,
                            focusedIndicatorColor = NeonPink
                        )
                    )

                    OutlinedTextField(
                        value = totalCapacityInput,
                        onValueChange = {
                            totalCapacityInput = it
                            viewModel.updateTotalCapacity(it)
                        },
                        label = { Text("Total Capacity") },
                        suffix = { Text("SPOTS") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CardSurface,
                            unfocusedContainerColor = CardSurface,
                            focusedIndicatorColor = NeonPink
                        )
                    )
                }
            }

            // Automation Settings
            Text(
                text = "Automation Settings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto-Open Gate", color = MaterialTheme.colorScheme.onBackground)
                            Text("Automatically open gate on successful checkout", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = settings.autoOpenGate,
                            onCheckedChange = {
                                haptic(HapticType.TOGGLE)
                                viewModel.toggleAutoGate(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = NeonPink
                            ),
                            modifier = Modifier.semantics { contentDescription = "Toggle Auto-Open Gate" }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Push Notifications", color = MaterialTheme.colorScheme.onBackground)
                            Text("Alerts for VIP arrivals and overstays", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = settings.pushNotifications,
                            onCheckedChange = {
                                haptic(HapticType.TOGGLE)
                                viewModel.togglePushNotifications(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = NeonPink
                            ),
                            modifier = Modifier.semantics { contentDescription = "Toggle Push Notifications" }
                        )
                    }
                }
            }

            // Zone Distribution
            Text(
                text = "Zone Distribution",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (zones.isEmpty()) {
                        Text(
                            text = "No zones configured yet.",
                            color = SecondaryText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        zones.sortedBy { it.id }.forEach { zone ->
                            Text(
                                text = "• Zone ${zone.id}: ${zone.name} (${zone.occupiedSpots}/${zone.totalCapacity})",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}
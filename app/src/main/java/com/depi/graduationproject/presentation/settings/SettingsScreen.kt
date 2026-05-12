package com.depi.graduationproject.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.core.theme.*
import com.depi.graduationproject.presentation.components.*
import com.depi.graduationproject.core.utils.HapticType
import com.depi.graduationproject.core.utils.rememberHapticFeedback

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
                        NavItem.SETTINGS -> {}
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(LprDimens.ScreenPadding)
        ) {
            BackHeader(
                title = "Settings",
                onBackClick = onNavigateToHome,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = ErrorRed,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            SectionHeader(
                title = "Pricing Configuration",
                icon = Icons.Default.AttachMoney,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsNumberCard(
                label = "Hourly Rate",
                value = hourlyRateInput,
                suffix = "EGP",
                helper = "Cost per hour of parking"
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsNumberCard(
                label = "Capacity",
                value = totalCapacityInput,
                suffix = "spots",
                helper = "Total parking spots available"
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Automation Settings",
                icon = Icons.Default.DirectionsCar,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsSwitchRow(
                title = "Auto Gate Open",
                subtitle = "Automatically open gate after payment",
                isChecked = settings.autoOpenGate,
                onCheckedChange = {
                    haptic(HapticType.TOGGLE)
                    viewModel.toggleAutoGate(it)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsSwitchRow(
                title = "Push Notifications",
                subtitle = "Receive alerts for overstay and sync",
                isChecked = settings.pushNotifications,
                onCheckedChange = {
                    haptic(HapticType.TOGGLE)
                    viewModel.togglePushNotifications(it)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Zone Distribution",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (zones.isEmpty()) {
                Text(
                    text = "No zones configured yet.",
                    color = MutedText,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                zones.sortedBy { it.id }.forEach { zone ->
                    ZoneDistributionRow(
                        zone = zone.name,
                        description = zone.description,
                        spots = "${zone.totalCapacity} spots"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
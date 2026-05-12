package com.depi.graduationproject.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.core.theme.EmeraldGreen
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.domain.model.ParkingSession
import com.depi.graduationproject.presentation.components.BottomNavLayout
import com.depi.graduationproject.presentation.components.DashboardHeader
import com.depi.graduationproject.presentation.components.DashboardMetricCard
import com.depi.graduationproject.presentation.components.GradientButton
import com.depi.graduationproject.presentation.components.NavItem
import com.depi.graduationproject.presentation.components.RecentParkedCard
import com.depi.graduationproject.presentation.components.ScannerFab
import com.depi.graduationproject.presentation.components.SegmentedEntryExitToggle
import com.depi.graduationproject.presentation.components.EntryExitMode

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToScanner: () -> Unit,
    onNavigateToManualEntry: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavLayout(
                selectedItem = NavItem.HOME,
                onItemSelected = { item ->
                    when (item) {
                        NavItem.HOME -> {}
                        NavItem.HISTORY -> onNavigateToHistory()
                        NavItem.ADD -> onNavigateToManualEntry()
                        NavItem.SETTINGS -> onNavigateToSettings()
                    }
                }
            )
        },
        floatingActionButton = {
            ScannerFab(
                onCameraClick = {
                    if (!uiState.isGarageFull) onNavigateToScanner()
                },
                onKeyboardClick = {
                    if (!uiState.isGarageFull) onNavigateToManualEntry()
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        DashboardContent(
            uiState = uiState,
            onToggleMode = { viewModel.toggleMode(it) },
            onCheckoutClick = onNavigateToCheckout,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onToggleMode: (Boolean) -> Unit,
    onCheckoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        DashboardHeader(
            userName = "Mahmoud",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        SegmentedEntryExitToggle(
            selectedMode = if (uiState.isEntryMode) EntryExitMode.Entry else EntryExitMode.Exit,
            onModeSelected = { mode ->
                onToggleMode(mode == EntryExitMode.Entry)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardMetricCard(
                label = "ACTIVE",
                value = "${uiState.activeSpots} /${uiState.totalCapacity}",
                subtitle = "Spots Filled",
                subtitleColor = EmeraldGreen,
                modifier = Modifier.weight(1f)
            )

            DashboardMetricCard(
                label = "REVENUE",
                value = uiState.todaysRevenue.toInt().toString(),
                subtitle = "EGP Today",
                subtitleColor = SecondaryText,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (uiState.isEntryMode) {
            Text(
                text = "Recently Parked",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (uiState.recentSessions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No active sessions right now.", color = SecondaryText)
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.recentSessions) { session ->
                        val (numbers, letters) = session.licensePlate.split(" ", limit = 2).let {
                            if (it.size >= 2) Pair(it[0], it[1]) else Pair(it[0], "")
                        }
                        val zoneLetter = session.zoneId.lastOrNull()?.uppercase()?.toString() ?: "A"
                        RecentParkedCard(
                            zoneName = "Zone ${session.zoneId.uppercase()}",
                            zoneLetter = zoneLetter,
                            time = "10:30 AM",
                            numbers = numbers,
                            letters = letters,
                            onDetailsClick = {},
                            onCheckOutClick = {}
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Scan QR Ticket or manually search license plate to process checkout.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                GradientButton(
                    text = "PROCESS CHECKOUT",
                    onClick = onCheckoutClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
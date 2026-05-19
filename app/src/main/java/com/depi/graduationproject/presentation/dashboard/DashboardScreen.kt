package com.depi.graduationproject.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.depi.graduationproject.core.utils.PlateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToScanner: () -> Unit,
    onNavigateToManualEntry: () -> Unit,
    onNavigateToCheckout: (String?) -> Unit,
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
    onCheckoutClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        item {
            DashboardHeader(
                userName = "Mahmoud",
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            SegmentedEntryExitToggle(
                selectedMode = if (uiState.isEntryMode) EntryExitMode.Entry else EntryExitMode.Exit,
                onModeSelected = { mode ->
                    onToggleMode(mode == EntryExitMode.Entry)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
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
        }

        if (uiState.isEntryMode) {
            item {
                Text(
                    text = "Recently Parked",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (uiState.recentSessions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active sessions right now.", color = SecondaryText)
                    }
                }
            } else {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.recentSessions, key = { it.id }) { session ->
                            val (numbers, letters) = PlateUtils.splitPlateText(session.licensePlate)
                            val zoneLetter = session.zoneId.lastOrNull()?.uppercase()?.toString() ?: "A"
                            RecentParkedCard(
                                zoneName = "Zone ${session.zoneId.uppercase()}",
                                zoneLetter = zoneLetter,
                                time = timeFormat.format(Date(session.entryTime)),
                                numbers = numbers,
                                letters = letters,
                                onDetailsClick = { onCheckoutClick(session.id) },
                                onCheckOutClick = { onCheckoutClick(session.id) }
                            )
                        }
                    }
                }
            }
        } else {
            item {
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
                        onClick = { onCheckoutClick(null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
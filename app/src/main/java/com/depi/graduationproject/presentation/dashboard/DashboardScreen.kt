package com.depi.graduationproject.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.core.theme.AppGradient
import com.depi.graduationproject.core.theme.EmeraldGreen
import com.depi.graduationproject.core.theme.GraduationProjectTheme
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.domain.model.ParkingSession
import com.depi.graduationproject.presentation.components.BottomNavLayout
import com.depi.graduationproject.presentation.components.GradientButton
import com.depi.graduationproject.presentation.components.MetricCard
import com.depi.graduationproject.presentation.components.ScannerFab
import com.depi.graduationproject.presentation.components.StatusBadge
import com.depi.graduationproject.presentation.components.PlateDisplay
import com.depi.graduationproject.presentation.components.NavItem
import com.depi.graduationproject.core.utils.PlateUtils
import com.depi.graduationproject.core.utils.HapticType
import com.depi.graduationproject.core.utils.rememberHapticFeedback

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
            onToggleMode = viewModel::toggleMode,
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
    val haptic = rememberHapticFeedback()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Greeting Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome Back,",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecondaryText
                )
                Text(
                    text = "Operator",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (uiState.isGarageFull) {
                StatusBadge(text = "GARAGE FULL", color = NeonPink)
            } else if (uiState.isStorageWarning) {
                StatusBadge(text = "SYNC NEEDED", color = Color(0xFFFFC107)) // Amber warning
            } else {
                StatusBadge(text = "ACTIVE", color = EmeraldGreen)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Entry / Exit Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF1A1D24))
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .then(if (uiState.isEntryMode) Modifier.background(AppGradient) else Modifier.background(Color.Transparent))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = {
                        haptic(HapticType.TOGGLE)
                        onToggleMode(true)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                    modifier = Modifier.semantics { contentDescription = "Switch to Entry Mode" }
                ) {
                    Text(
                        "ENTRY MODE",
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isEntryMode) Color.White else SecondaryText
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .then(if (!uiState.isEntryMode) Modifier.background(AppGradient) else Modifier.background(Color.Transparent))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = {
                        haptic(HapticType.TOGGLE)
                        onToggleMode(false)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                    modifier = Modifier.semantics { contentDescription = "Switch to Exit Mode" }
                ) {
                    Text(
                        "EXIT MODE",
                        fontWeight = FontWeight.Bold,
                        color = if (!uiState.isEntryMode) Color.White else SecondaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                label = "SPOTS FILLED",
                value = uiState.activeSpots.toString(),
                subtitle = "${uiState.totalCapacity - uiState.activeSpots} SPOTS LEFT",
                subtitleColor = EmeraldGreen,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                label = "EGP TODAY",
                value = uiState.todaysRevenue.toInt().toString(),
                subtitle = "Total Revenue",
                subtitleColor = SecondaryText,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action or Recent
        if (uiState.isEntryMode) {
            Text(
                text = "Recently Parked",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (uiState.recentSessions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No active sessions right now.", color = SecondaryText)
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.recentSessions) { session ->
                        RecentSessionCard(session)
                    }
                }
            }
        } else {
            // EXIT MODE
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
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
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Process car checkout" }
                )
            }
        }
    }
}

@Composable
fun RecentSessionCard(session: ParkingSession) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D24)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(200.dp)
            .semantics {
                contentDescription = "Recent session in Zone ${session.zoneId}, plate ${session.licensePlate}"
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val fromName = session.zoneId.firstOrNull()?.uppercase() ?: "A"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.DarkGray.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = fromName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Zone ${session.zoneId}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            val (numbers, letters) = PlateUtils.splitPlateText(session.licensePlate)
            PlateDisplay(
                numbers = numbers,
                letters = letters,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun DashboardPreview() {
    GraduationProjectTheme {
        DashboardContent(
            uiState = DashboardUiState(
                activeSpots = 45,
                totalCapacity = 90,
                todaysRevenue = 150.0,
                isEntryMode = true,
                isGarageFull = false,
                recentSessions = emptyList() // or dummy
            ),
            onToggleMode = {},
            onCheckoutClick = {}
        )
    }
}
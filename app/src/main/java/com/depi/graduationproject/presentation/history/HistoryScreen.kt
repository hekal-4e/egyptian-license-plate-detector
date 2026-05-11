package com.depi.graduationproject.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.core.theme.CardSurface
import com.depi.graduationproject.core.theme.EmeraldGreen
import com.depi.graduationproject.core.theme.ErrorRed
import com.depi.graduationproject.core.theme.GraduationProjectTheme
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.domain.model.SessionStatus
import com.depi.graduationproject.presentation.components.PlateDisplay
import com.depi.graduationproject.presentation.components.StatusBadge
import com.depi.graduationproject.core.utils.PlateUtils
import com.depi.graduationproject.presentation.components.ShimmerPlaceholder
import com.depi.graduationproject.core.utils.HapticType
import com.depi.graduationproject.core.utils.rememberHapticFeedback
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.depi.graduationproject.presentation.components.BottomNavLayout
import com.depi.graduationproject.presentation.components.NavItem

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToManualEntry: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavLayout(
                selectedItem = NavItem.HISTORY,
                onItemSelected = { item ->
                    when (item) {
                        NavItem.HOME -> onNavigateToHome()
                        NavItem.HISTORY -> {}
                        NavItem.ADD -> onNavigateToManualEntry()
                        NavItem.SETTINGS -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { innerPadding ->
        HistoryContent(
            uiState = uiState,
            onFilterSelected = { start, end, label -> 
                viewModel.loadData(start, end, label)
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun HistoryContent(
    uiState: HistoryUiState,
    onFilterSelected: (Long, Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var filterExpanded by remember { mutableStateOf(false) }
    val haptic = rememberHapticFeedback()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = "History & Analytics",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Date Range Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Showing data for:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                    Text(
                        text = uiState.dateLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Box {
                    IconButton(
                        onClick = {
                            haptic(HapticType.CLICK)
                            filterExpanded = true
                        },
                        modifier = Modifier.semantics { contentDescription = "Open date filter menu" }
                    ) {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = Color.White)
                    }
                    
                    DropdownMenu(
                        expanded = filterExpanded,
                        onDismissRequest = { filterExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Today") },
                            onClick = { 
                                haptic(HapticType.CLICK)
                                filterExpanded = false
                                val cal = java.util.Calendar.getInstance()
                                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                cal.set(java.util.Calendar.MINUTE, 0)
                                cal.set(java.util.Calendar.SECOND, 0)
                                cal.set(java.util.Calendar.MILLISECOND, 0)
                                val start = cal.timeInMillis
                                cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                                cal.set(java.util.Calendar.MINUTE, 59)
                                cal.set(java.util.Calendar.SECOND, 59)
                                cal.set(java.util.Calendar.MILLISECOND, 999)
                                onFilterSelected(start, cal.timeInMillis, "Today")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("This Week") },
                            onClick = {
                                haptic(HapticType.CLICK)
                                filterExpanded = false
                                val cal = java.util.Calendar.getInstance()
                                cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                                cal.set(java.util.Calendar.MINUTE, 59)
                                cal.set(java.util.Calendar.SECOND, 59)
                                cal.set(java.util.Calendar.MILLISECOND, 999)
                                val end = cal.timeInMillis
                                cal.add(java.util.Calendar.DAY_OF_YEAR, -7)
                                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                cal.set(java.util.Calendar.MINUTE, 0)
                                cal.set(java.util.Calendar.SECOND, 0)
                                cal.set(java.util.Calendar.MILLISECOND, 0)
                                onFilterSelected(cal.timeInMillis, end, "This Week")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("All Time") },
                            onClick = {
                                haptic(HapticType.CLICK)
                                filterExpanded = false
                                onFilterSelected(0L, Long.MAX_VALUE, "All Time")
                            }
                        )
                    }
                }
            }
        }

        // Chart
        Text(
            text = "Peak Hours",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(200.dp).padding(bottom = 24.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when {
                    uiState.isLoading -> {
                        ShimmerPlaceholder(height = 168.dp, modifier = Modifier.fillMaxWidth())
                    }
                    uiState.peakHoursMap.isNotEmpty() -> {
                        // Convert map to Vico entries
                        val entries = (0..23).map { hour ->
                            uiState.peakHoursMap[hour]?.toFloat() ?: 0f
                        }.toTypedArray()
                        
                        val chartEntryModel = entryModelOf(*entries)

                        Chart(
                            chart = columnChart(),
                            model = chartEntryModel,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis()
                        )
                    }
                    else -> {
                        Text(
                            "No data available",
                            modifier = Modifier.align(Alignment.Center),
                            color = SecondaryText
                        )
                    }
                }
            }
        }

        Text(
            text = "Transactions",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(3) {
                    ShimmerPlaceholder(height = 96.dp)
                }
            }
        } else if (uiState.sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No transactions found.", color = SecondaryText)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.sessions) { item ->
                    TransactionItem(item)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(item: HistorySessionItem) {
    val session = item.session
    val statusLabel = when {
        item.isOverstay -> "Overstay"
        session.status == SessionStatus.ACTIVE -> "Active"
        else -> "Completed"
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Transaction for Zone ${session.zoneId}, plate ${session.licensePlate}, status $statusLabel"
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
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

                // Status Badge logic (T088 overstay)
                if (item.isOverstay) {
                    StatusBadge(text = "OVERSTAY", color = ErrorRed)
                } else if (session.status == SessionStatus.ACTIVE) {
                    StatusBadge(text = "ACTIVE", color = NeonPink)
                } else {
                    StatusBadge(text = "COMPLETED", color = EmeraldGreen)
                }
            }

            val (numbers, letters) = PlateUtils.splitPlateText(session.licensePlate)
            
            PlateDisplay(
                numbers = numbers,
                letters = letters,
                modifier = Modifier.fillMaxWidth()
            )
            
            if (session.status == SessionStatus.COMPLETED) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Fee",
                        color = SecondaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${session.totalFee} EGP",
                        color = NeonPink,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun HistoryPreview() {
    GraduationProjectTheme {
        val dummyMap = mapOf(10 to 5, 11 to 8, 12 to 15, 13 to 22, 14 to 12)
        HistoryContent(
            uiState = HistoryUiState(
                isLoading = false,
                peakHoursMap = dummyMap,
                sessions = listOf()
            ),
            onFilterSelected = { _, _, _ -> }
        )
    }
}
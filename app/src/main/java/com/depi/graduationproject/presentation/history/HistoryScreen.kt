package com.depi.graduationproject.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.core.theme.*
import com.depi.graduationproject.domain.model.SessionStatus
import com.depi.graduationproject.presentation.components.*
import com.depi.graduationproject.core.utils.PlateUtils
import com.depi.graduationproject.core.utils.HapticType
import com.depi.graduationproject.core.utils.rememberHapticFeedback
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCheckout: (String) -> Unit,
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
            onNavigateToCheckout = onNavigateToCheckout,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun HistoryContent(
    uiState: HistoryUiState,
    onFilterSelected: (Long, Long, String) -> Unit,
    onNavigateToCheckout: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = LprDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            HistoryHeader()
        }

        item {
            HistoryDateFilterCard(
                dateRange = uiState.dateLabel,
                onChangeClick = {
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
        }

        item {
            val peakData = if (uiState.peakHoursMap.isNotEmpty()) {
                uiState.peakHoursMap.map { (hour, count) ->
                    val label = when {
                        hour < 12 -> "${hour}am"
                        hour == 12 -> "12pm"
                        else -> "${hour - 12}pm"
                    }
                    PeakHourData(label, count.toFloat())
                }
            } else {
                listOf(
                    PeakHourData("6am", 15f),
                    PeakHourData("9am", 45f),
                    PeakHourData("12pm", 35f),
                    PeakHourData("3pm", 25f),
                    PeakHourData("6pm", 40f),
                    PeakHourData("9pm", 10f)
                )
            }
            GradientBarChart(
                peakHoursData = peakData,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )
        }

        if (uiState.isLoading) {
            items(3) {
                ShimmerPlaceholder(height = 72.dp, modifier = Modifier.fillMaxWidth())
            }
        } else if (uiState.sessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions found.", color = MutedText)
                }
            }
        } else {
            items(uiState.sessions, key = { it.session.id }) { item ->
                HistoryTransactionRow(
                    item = item,
                    onClick = {
                        if (item.session.status == SessionStatus.ACTIVE) {
                            onNavigateToCheckout(item.session.id)
                        }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun HistoryTransactionRow(
    item: HistorySessionItem,
    onClick: () -> Unit = {}
) {
    val session = item.session
    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val timeStr = dateFormat.format(Date(session.entryTime))
    val zoneLetter = session.zoneId.take(1).uppercase()
    val (numbers, letters) = PlateUtils.splitPlateText(session.licensePlate)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LprDimens.CardRadius))
            .background(PanelSurface)
            .clickable(onClick = onClick)
            .padding(12.dp)
            .semantics {
                contentDescription = "Transaction for Zone ${session.zoneId}, plate $numbers $letters"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(listOf(NeonPink, DeepPurple))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = zoneLetter,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Zone ${session.zoneId}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(GreenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "$numbers $letters",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
                Text(
                    text = timeStr,
                    style = TinyCaps,
                    color = MutedText
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            val fee = session.totalFee ?: 0.0
            Text(
                text = "${fee.toInt()}.00 EGP",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Text(
                text = if (item.isOverstay) "OVERSTAY" else if (session.status == SessionStatus.ACTIVE) "ACTIVE" else "COMPLETED",
                style = TinyCaps,
                color = if (item.isOverstay) ErrorRed else if (session.status == SessionStatus.ACTIVE) NeonPink else EmeraldGreen
            )
        }
    }
}
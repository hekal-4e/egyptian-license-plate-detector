package com.depi.graduationproject.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.ui.components.StatCard

@Composable
fun DashboardScreen(
    occupancy: Int = 45,
    totalCapacity: Int = 60,
    onStartCamera: () -> Unit,
    onViewHistory: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartCamera,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("SCAN NEW VEHICLE", fontWeight = FontWeight.Bold)
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                "Smart Garage Hub",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(24.dp))

            // Occupancy Section
            Text("Live Garage Capacity", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = occupancy.toFloat() / totalCapacity,
                modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp)),
                color = if (occupancy > totalCapacity * 0.8) Color.Red else Color.Green
            )
            Text("$occupancy / $totalCapacity Spots Occupied", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(32.dp))

            // Quick Stats Cards
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "Today's Revenue",
                    value = "1,250 EGP",
                    icon = Icons.Default.MonetizationOn,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Vehicles In",
                    value = occupancy.toString(),
                    icon = Icons.Default.DirectionsCar,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // History Navigation
            OutlinedCard(
                onClick = onViewHistory,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                ListItem(
                    headlineContent = { Text("View All Records") },
                    supportingContent = { Text("Search and manage historical logs") },
                    leadingContent = { Icon(
                        Icons.Default.Analytics, tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                        modifier = Modifier
                    ) },
                    trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                )
            }
        }
    }
}
package com.depi.graduationproject.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BillingSummaryScreen(
    plateNumber: String,
    duration: String = "2h 15m",
    totalFee: String = "25.00",
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Payment Overview", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BillingItem("Plate Number", plateNumber)
                BillingItem("Parking Duration", duration)
                Divider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("TOTAL FEE", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                    Text("$totalFee EGP", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("PROCESS PAYMENT & EXIT", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BillingItem(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
        Text(value, fontWeight = FontWeight.Bold)
    }
}
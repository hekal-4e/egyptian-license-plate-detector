package com.depi.graduationproject.presentation.scanner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.presentation.components.GradientButton
import com.depi.graduationproject.presentation.components.TicketCard

@Composable
fun TicketScreen(
    plateNumbers: String,
    plateLetters: String,
    zone: String,
    entryTime: String,
    date: String,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Check-In Successful!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        TicketCard(
            plateNumbers = plateNumbers,
            plateLetters = plateLetters,
            zone = zone,
            entryTime = entryTime,
            date = date
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        GradientButton(
            text = "RETURN TO DASHBOARD",
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
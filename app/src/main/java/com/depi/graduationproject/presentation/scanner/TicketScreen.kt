package com.depi.graduationproject.presentation.scanner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.depi.graduationproject.core.utils.PlateUtils
import com.depi.graduationproject.presentation.components.GradientButton
import com.depi.graduationproject.presentation.components.TicketCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TicketScreen(
    viewModel: TicketViewModel = hiltViewModel(),
    onDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.session != null) {
            val session = uiState.session!!
            Text(
                text = "Check-In Successful!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            val (numbers, letters) = PlateUtils.splitPlateText(session.licensePlate)
            val timeFormat = SimpleDateFormat("HH:mm a", Locale.getDefault())
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

            TicketCard(
                plateNumbers = numbers,
                plateLetters = letters,
                zone = "Zone ${session.zoneId}",
                entryTime = timeFormat.format(Date(session.entryTime)),
                date = dateFormat.format(Date(session.entryTime)),
                qrContent = session.id
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            GradientButton(
                text = "RETURN TO DASHBOARD",
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text("Error loading ticket.")
            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(
                text = "BACK",
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
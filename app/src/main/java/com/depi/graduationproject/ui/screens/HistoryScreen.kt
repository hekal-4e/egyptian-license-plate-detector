package com.depi.graduationproject.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.ui.components.PlateItemCard
import com.depi.graduationproject.ui.components.SwipeToDeleteContainer
import com.depi.graduationproject.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val plates by viewModel.platesState.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Garage Records",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                actions = {
                    if (plates.isNotEmpty()) {
                        IconButton(onClick = { /* PDF Logic will be added here */ }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = Color.Red)
                        }
                        IconButton(onClick = { viewModel.clearAllHistory() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (plates.isEmpty()) {
                EmptyHistoryView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = plates, key = { it.id }) { plate ->
                        SwipeToDeleteContainer(
                            item = plate,
                            onDelete = { viewModel.deleteRecord(plate.id) }
                        ) {
                            PlateItemCard(plate = plate)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No records found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
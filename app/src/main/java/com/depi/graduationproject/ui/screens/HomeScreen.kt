package com.depi.graduationproject.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onStartCamera: () -> Unit,
    onViewHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ANPR Dashboard",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(40.dp))

        LargeNavigationButton(
            text = "Scan License Plate",
            icon = Icons.Rounded.Face,
            onClick = onStartCamera,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )

        Spacer(modifier = Modifier.height(16.dp))

        LargeNavigationButton(
            text = "View Records History",
            icon = Icons.Default.List,
            onClick = onViewHistory,
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}

@Composable
fun LargeNavigationButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}
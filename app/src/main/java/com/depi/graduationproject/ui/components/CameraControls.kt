package com.depi.graduationproject.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CameraControls(
    onCaptureClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            // زر السجل
            IconButton(
                onClick = onHistoryClick,
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "History",
                    tint = Color.White
                )
            }

            // زر التصوير
            IconButton(
                onClick = onCaptureClick,
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White, CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                // شكل جمالي
            }

            Spacer(modifier = Modifier.size(50.dp))
        }
    }
}
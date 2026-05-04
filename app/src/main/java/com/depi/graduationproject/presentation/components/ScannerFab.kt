package com.depi.graduationproject.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.core.theme.AppGradient
import com.depi.graduationproject.core.theme.CardSurfaceVariant
import com.depi.graduationproject.core.theme.GraduationProjectTheme
import com.depi.graduationproject.core.theme.PrimaryText

@Composable
fun ScannerFab(
    onCameraClick: () -> Unit,
    onKeyboardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Secondary FAB
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(CardSurfaceVariant)
                .clickable(onClick = onKeyboardClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Keyboard,
                contentDescription = "Manual Entry",
                tint = PrimaryText,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Primary FAB
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(AppGradient)
                .clickable(onClick = onCameraClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Scan License Plate",
                tint = PrimaryText,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun ScannerFabPreview() {
    GraduationProjectTheme {
        Box(modifier = Modifier.padding(32.dp)) {
            ScannerFab(
                onCameraClick = {},
                onKeyboardClick = {}
            )
        }
    }
}

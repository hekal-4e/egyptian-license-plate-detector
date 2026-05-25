package com.depi.graduationproject.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.core.theme.AppBackground
import com.depi.graduationproject.core.theme.GraduationProjectTheme
import com.depi.graduationproject.core.theme.MutedText
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.PanelSurface
import com.depi.graduationproject.core.theme.PrimaryText

@Composable
fun ScannerProcessingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(PanelSurface.copy(alpha = 0.94f))
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            CircularProgressIndicator(
                color = NeonPink,
                strokeWidth = 3.dp
            )
            Text(
                text = "Analyzing plate...",
                color = PrimaryText,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Hold steady",
                color = MutedText,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun ScannerProcessingOverlayPreview() {
    GraduationProjectTheme {
        ScannerProcessingOverlay()
    }
}

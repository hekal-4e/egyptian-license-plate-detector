package com.depi.graduationproject.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.core.theme.EmeraldGreen
import com.depi.graduationproject.core.theme.GraduationProjectTheme
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.PrimaryText

@Composable
fun StatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun StatusBadgePreview() {
    GraduationProjectTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            StatusBadge(text = "AI Verified Match", color = EmeraldGreen)
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge(text = "ACTIVE", color = NeonPink)
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge(text = "COMPLETED", color = EmeraldGreen)
        }
    }
}

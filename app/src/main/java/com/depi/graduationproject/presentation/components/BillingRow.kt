package com.depi.graduationproject.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.core.theme.CardSurface
import com.depi.graduationproject.core.theme.CardSurfaceVariant
import com.depi.graduationproject.core.theme.GraduationProjectTheme
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.PrimaryText
import com.depi.graduationproject.core.theme.SecondaryText

@Composable
fun BillingRow(
    icon: ImageVector,
    label: String,
    value: String,
    dateText: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Circle Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CardSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonPink,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Label and Date (if any)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label.uppercase(),
                color = SecondaryText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            if (dateText != null) {
                Text(
                    text = dateText,
                    color = SecondaryText,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        // Value text
        Text(
            text = value,
            color = PrimaryText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun BillingRowPreview() {
    GraduationProjectTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(CardSurface)
                .padding(16.dp)
        ) {
            BillingRow(
                icon = Icons.Default.LocationOn,
                label = "Parking Zone",
                value = "Zone A"
            )
            BillingRow(
                icon = Icons.Default.AccessTime,
                label = "Duration",
                value = "2h 15m",
                dateText = "Today, 14:30 - 16:45"
            )
            BillingRow(
                icon = Icons.Default.Payment,
                label = "Hourly Rate",
                value = "EGP 20/hr"
            )
        }
    }
}

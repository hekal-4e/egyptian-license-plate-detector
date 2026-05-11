package com.depi.graduationproject.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.core.theme.CardSurface
import com.depi.graduationproject.core.theme.EmeraldGreen
import com.depi.graduationproject.core.theme.ErrorRed
import com.depi.graduationproject.core.theme.GraduationProjectTheme
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.PrimaryText
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.core.utils.HapticType
import com.depi.graduationproject.core.utils.rememberHapticFeedback

@Composable
fun ZoneCard(
    zoneLetter: String,
    zoneName: String,
    subtitle: String,
    spotsLeft: Int,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    contentDescriptionOverride: String? = null
) {
    val isFull = spotsLeft <= 0
    val alpha = if (isFull) 0.5f else 1f
    val spotsText = if (isFull) "FULL" else "$spotsLeft SPOTS LEFT"
    val spotsColor = if (isFull) ErrorRed else EmeraldGreen
    val haptic = rememberHapticFeedback()
    val accessibilityLabel = if (isFull) {
        "$zoneName full"
    } else {
        "$zoneName, $spotsLeft spots left"
    }
    val resolvedDescription = contentDescriptionOverride ?: accessibilityLabel

    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .then(
                if (isSelected) Modifier.border(2.dp, NeonPink, MaterialTheme.shapes.large)
                else Modifier
            )
            .clip(MaterialTheme.shapes.large) // 16dp
            .background(CardSurface)
            .semantics {
                contentDescription = resolvedDescription
                role = Role.Button
            }
            .clickable(enabled = !isFull, onClick = {
                haptic(HapticType.CLICK)
                onClick()
            })
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Zone Letter Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = zoneLetter,
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Name and Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = zoneName,
                    color = PrimaryText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = SecondaryText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Spots Left
            Text(
                text = spotsText,
                color = spotsColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun ZoneCardPreview() {
    GraduationProjectTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ZoneCard(
                zoneLetter = "A",
                zoneName = "Zone A",
                subtitle = "Premium Covered",
                spotsLeft = 12
            )
            Spacer(modifier = Modifier.padding(8.dp))
            ZoneCard(
                zoneLetter = "B",
                zoneName = "Zone B",
                subtitle = "Standard Open",
                spotsLeft = 45,
                isSelected = true
            )
            Spacer(modifier = Modifier.padding(8.dp))
            ZoneCard(
                zoneLetter = "C",
                zoneName = "Zone C",
                subtitle = "VIP Parking",
                spotsLeft = 0
            )
        }
    }
}
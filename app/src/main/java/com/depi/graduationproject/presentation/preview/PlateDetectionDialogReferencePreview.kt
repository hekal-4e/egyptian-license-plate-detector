package com.depi.graduationproject.presentation.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.core.theme.AppBackground
import com.depi.graduationproject.core.theme.EmeraldGreen
import com.depi.graduationproject.core.theme.GreenBg
import com.depi.graduationproject.core.theme.LprDimens
import com.depi.graduationproject.core.theme.MutedText
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.PanelSurface
import com.depi.graduationproject.core.theme.PanelSurfaceAlt
import com.depi.graduationproject.core.theme.PrimaryHorizontalGradient
import com.depi.graduationproject.core.theme.PrimaryText
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.core.theme.TinyCaps
import com.depi.graduationproject.presentation.components.GradientButton
import com.depi.graduationproject.presentation.components.PlateDisplay
import com.depi.graduationproject.presentation.components.SecondaryButton
import com.depi.graduationproject.presentation.components.StatusBadge
import androidx.compose.ui.tooling.preview.Preview

@Preview(widthDp = 360, heightDp = 800, showBackground = true, backgroundColor = 0xFF08090D)
@Composable
fun PlateDetectionDialogReferencePreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(PanelSurface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MutedText.copy(alpha = 0.4f))
        )

        Text(
            text = "Scan Verification",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
        )

        Text(
            text = "Review and confirm plate detection",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "DETECTED IMAGE",
            style = TinyCaps,
            color = MutedText,
            modifier = Modifier.align(Alignment.Start)
        )

        Box(
            modifier = Modifier
                .size(width = 200.dp, height = 80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppBackground)
                .border(1.dp, MutedText.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cropped plate preview",
                style = MaterialTheme.typography.bodySmall,
                color = MutedText
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(GreenBg)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(EmeraldGreen)
                )
                Text(
                    text = "AI Verified Match",
                    style = TinyCaps,
                    color = EmeraldGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "PLATE NUMBER",
            style = TinyCaps,
            color = MutedText,
            modifier = Modifier.align(Alignment.Start)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppBackground)
                .padding(16.dp)
        ) {
            PlateDisplay(
                numbers = "1234",
                letters = "أ ب ج",
                modifier = Modifier.fillMaxWidth()
            )
        }

        Text(
            text = "Tap to edit if incorrect",
            style = TinyCaps,
            color = MutedText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryButton(
                text = "Retake",
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            GradientButton(
                text = "Confirm & Save",
                onClick = {},
                modifier = Modifier.weight(2f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

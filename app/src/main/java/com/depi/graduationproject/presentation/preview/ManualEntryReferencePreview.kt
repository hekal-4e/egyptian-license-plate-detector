package com.depi.graduationproject.presentation.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Icon
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
import com.depi.graduationproject.core.theme.DeepPurple
import com.depi.graduationproject.core.theme.Hairline
import com.depi.graduationproject.core.theme.InputBlack
import com.depi.graduationproject.core.theme.LprDimens
import com.depi.graduationproject.core.theme.MutedText
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.PanelSurface
import com.depi.graduationproject.core.theme.PrimaryHorizontalGradient
import com.depi.graduationproject.core.theme.PrimaryText
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.core.theme.TinyCaps
import com.depi.graduationproject.presentation.components.BackHeader
import com.depi.graduationproject.presentation.components.GradientButton
import com.depi.graduationproject.presentation.components.SecondaryButton
import androidx.compose.ui.tooling.preview.Preview

@Preview(widthDp = 360, heightDp = 800, showBackground = true, backgroundColor = 0xFF08090D)
@Composable
fun ManualEntryReferencePreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(LprDimens.ScreenPadding)
    ) {
        BackHeader(
            title = "Manual Entry",
            onBackClick = {}
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LprDimens.LargeCardRadius))
                .background(PanelSurface)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Keyboard,
                contentDescription = null,
                tint = NeonPink,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Enter Plate Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )

            Text(
                text = "Egyptian license plate format",
                style = MaterialTheme.typography.bodySmall,
                color = MutedText
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NUMBERS",
                        style = TinyCaps,
                        color = MutedText,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(LprDimens.PlateHeight)
                            .clip(RoundedCornerShape(12.dp))
                            .background(InputBlack)
                            .border(2.dp, Hairline, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1234",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LETTERS",
                        style = TinyCaps,
                        color = MutedText,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(LprDimens.PlateHeight)
                            .clip(RoundedCornerShape(12.dp))
                            .background(InputBlack)
                            .border(2.dp, Hairline, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "أ ب ج",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        GradientButton(
            text = "Confirm Check-In",
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        SecondaryButton(
            text = "Cancel",
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

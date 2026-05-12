package com.depi.graduationproject.presentation.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
import com.depi.graduationproject.core.theme.PinkHot
import com.depi.graduationproject.core.theme.PrimaryText
import com.depi.graduationproject.core.theme.PurpleHot
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.core.theme.TinyCaps
import com.depi.graduationproject.presentation.components.GradientButton
import com.depi.graduationproject.presentation.components.PlateDisplay
import com.depi.graduationproject.presentation.components.AppSearchBar
import androidx.compose.ui.tooling.preview.Preview

@Preview(widthDp = 360, heightDp = 800, showBackground = true, backgroundColor = 0xFF08090D)
@Composable
fun CheckoutReferencePreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(LprDimens.ScreenPadding)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        AppSearchBar(
            query = "",
            onQueryChange = {},
            placeholder = "Search License Plate / Lost Ticket",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LprDimens.CardRadius))
                .background(PanelSurface)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PurpleHot.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = PurpleHot,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "FIND CAR: Located in Zone A",
                        style = TinyCaps,
                        color = MutedText
                    )
                    Text(
                        text = "Spot A-12 - Row 2",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LprDimens.CardRadius))
                .background(PanelSurface)
                .padding(16.dp)
        ) {
            Text(
                text = "LICENSE PLATE",
                style = TinyCaps,
                color = NeonPink
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlateDisplay(
                numbers = "1234",
                letters = "أ ب ج",
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LprDimens.CardRadius))
                .background(PanelSurface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Billing Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )

            BillingRow(label = "Entry Time", value = "10:30 AM", date = "May 11, 2026")
            BillingRow(label = "Exit Time", value = "12:30 PM", date = "May 11, 2026")
            BillingRow(label = "Duration", value = "2.0 hours", date = "")
            BillingRow(label = "Hourly Rate", value = "15.00 EGP", date = "")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LprDimens.CardRadius))
                .background(PanelSurface)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TOTAL FEE",
                    style = TinyCaps,
                    color = MutedText
                )
                Text(
                    text = "30.00 EGP",
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.displayLarge.fontSize,
                        fontWeight = FontWeight.ExtraBold,
                        brush = Brush.horizontalGradient(listOf(PinkHot, PurpleHot))
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenBg)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen)
                        )
                        Text(
                            text = "Auto-calculated - No hidden fees",
                            style = TinyCaps,
                            color = EmeraldGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        GradientButton(
            text = "PAY & OPEN GATE",
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun BillingRow(label: String, value: String, date: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TinyCaps,
            color = MutedText
        )
        Row {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = PrimaryText
            )
            if (date.isNotEmpty()) {
                Text(
                    text = " - $date",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }
        }
    }
}

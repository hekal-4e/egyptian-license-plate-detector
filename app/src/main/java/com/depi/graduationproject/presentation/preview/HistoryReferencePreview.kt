package com.depi.graduationproject.presentation.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.depi.graduationproject.core.theme.PrimaryText
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.core.theme.TinyCaps
import com.depi.graduationproject.presentation.components.GradientBarChart
import com.depi.graduationproject.presentation.components.PeakHourData
import com.depi.graduationproject.presentation.components.PlateDisplay
import androidx.compose.ui.tooling.preview.Preview

@Preview(widthDp = 360, heightDp = 800, showBackground = true, backgroundColor = 0xFF08090D)
@Composable
fun HistoryReferencePreview() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = LprDimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                Text(
                    text = " & Analytics",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = NeonPink
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(LprDimens.CardRadius))
                    .background(PanelSurface)
                    .padding(16.dp)
                    .clickable {}
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "DATE RANGE",
                                style = TinyCaps,
                                color = MutedText
                            )
                            Text(
                                text = "Oct 10 - Oct 15",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = PrimaryText
                            )
                        }
                    }
                    TextButton(onClick = {}) {
                        Text(
                            text = "Change",
                            style = TinyCaps,
                            color = NeonPink
                        )
                    }
                }
            }
        }

        item {
            GradientBarChart(
                peakHoursData = listOf(
                    PeakHourData("6am", 15f),
                    PeakHourData("9am", 45f),
                    PeakHourData("12pm", 35f),
                    PeakHourData("3pm", 25f),
                    PeakHourData("6pm", 40f),
                    PeakHourData("9pm", 10f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )
        }

        items(listOf(
            Triple("Zone A", Triple("1234", "أ ب ج", "A"), Pair("10:30 AM", 30.0)),
            Triple("Zone B", Triple("5678", "د ه و", "B"), Pair("09:15 AM", 45.0)),
            Triple("Zone A", Triple("9012", "ز ح ط", "A"), Pair("08:00 AM", 15.0))
        )) { (zone, plate, timeFee) ->
            HistoryTransactionRow(
                zone = zone,
                numbers = plate.first,
                letters = plate.second,
                zoneLetter = plate.third,
                time = timeFee.first,
                fee = timeFee.second
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun HistoryTransactionRow(
    zone: String,
    numbers: String,
    letters: String,
    zoneLetter: String,
    time: String,
    fee: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LprDimens.CardRadius))
            .background(PanelSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(listOf(NeonPink, com.depi.graduationproject.core.theme.DeepPurple))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = zone.last().toString(),
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = zone,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(GreenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "$numbers $letters",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
                Text(
                    text = "$time",
                    style = TinyCaps,
                    color = MutedText
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${fee.toInt()}.00 EGP",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Text(
                text = "COMPLETED",
                style = TinyCaps,
                color = EmeraldGreen
            )
        }
    }
}

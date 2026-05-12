package com.depi.graduationproject.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.depi.graduationproject.core.theme.ChartBarGradient
import com.depi.graduationproject.core.theme.EmeraldGreen
import com.depi.graduationproject.core.theme.Hairline
import com.depi.graduationproject.core.theme.LprDimens
import com.depi.graduationproject.core.theme.MutedText
import com.depi.graduationproject.core.theme.PanelSurface
import com.depi.graduationproject.core.theme.PrimaryHorizontalGradient
import com.depi.graduationproject.core.theme.PrimaryText
import com.depi.graduationproject.core.theme.SectionTitle
import com.depi.graduationproject.core.theme.TinyCaps

data class PeakHourData(
    val label: String,
    val value: Float
)

@Composable
fun GradientBarChart(
    peakHoursData: List<PeakHourData>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val maxValue = (peakHoursData.maxOfOrNull { it.value } ?: 60f).coerceAtLeast(1f)
    val chartHeight = 120.dp
    val barSpacing = 8.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LprDimens.CardRadius))
            .background(PanelSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PEAK HOURS",
                    style = SectionTitle,
                    color = PrimaryText
                )
                Text(
                    text = "TODAY'S TRAFFIC",
                    style = TinyCaps,
                    color = MutedText
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0D372B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            val totalWidth = size.width
            val totalHeight = size.height
            val barCount = peakHoursData.size
            val spacing = barSpacing.toPx()
            val totalSpacing = spacing * (barCount - 1)
            val barWidth = (totalWidth - totalSpacing) / barCount

            val gridColor = Hairline.copy(alpha = 0.4f)
            val gridCount = 4
            for (i in 0..gridCount) {
                val y = totalHeight * i / gridCount
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(totalWidth, y),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )
            }

            peakHoursData.forEachIndexed { index, data ->
                val barHeight = (data.value / maxValue) * totalHeight * 0.85f
                val x = index * (barWidth + spacing)
                val y = totalHeight - barHeight

                drawRoundRect(
                    brush = ChartBarGradient,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            peakHoursData.forEach { data ->
                Text(
                    text = data.label,
                    style = TinyCaps,
                    color = MutedText
                )
            }
        }
    }
}

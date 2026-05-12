package com.depi.graduationproject.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.core.theme.EmeraldGreen
import com.depi.graduationproject.core.theme.Hairline
import com.depi.graduationproject.core.theme.LprDimens
import com.depi.graduationproject.core.theme.MetricNumber
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.PanelSurface
import com.depi.graduationproject.core.theme.PanelSurfaceAlt
import com.depi.graduationproject.core.theme.PrimaryHorizontalGradient
import com.depi.graduationproject.core.theme.PrimaryText
import com.depi.graduationproject.core.theme.PurpleHot
import com.depi.graduationproject.core.theme.TinyCaps

@Composable
fun ZoneOptionCard(
    zoneName: String,
    zoneDescription: String,
    spotsLeft: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) PanelSurfaceAlt else PanelSurface
    val borderModifier = if (isSelected) {
        Modifier.border(2.dp, PrimaryHorizontalGradient, RoundedCornerShape(LprDimens.CardRadius))
    } else {
        Modifier.border(1.dp, Hairline, RoundedCornerShape(LprDimens.CardRadius))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(75.dp)
            .clip(RoundedCornerShape(LprDimens.CardRadius))
            .background(backgroundColor)
            .then(borderModifier)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Zone $zoneName, $spotsLeft spots left" }
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(LprDimens.ZoneBadgeSize)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) PrimaryHorizontalGradient
                            else Brush.verticalGradient(listOf(PurpleHot, NeonPink))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = zoneName.takeLast(1),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = zoneName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText
                    )
                    Text(
                        text = zoneDescription,
                        style = TinyCaps,
                        color = PrimaryText.copy(alpha = 0.6f)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = spotsLeft.toString(),
                    style = MetricNumber,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen
                )
                Text(
                    text = "SPOTS LEFT",
                    style = TinyCaps,
                    color = PrimaryText.copy(alpha = 0.5f)
                )
            }
        }
    }
}

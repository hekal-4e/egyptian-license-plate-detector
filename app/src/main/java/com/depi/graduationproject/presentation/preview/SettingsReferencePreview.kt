package com.depi.graduationproject.presentation.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.depi.graduationproject.core.theme.AppBackground
import com.depi.graduationproject.core.theme.EmeraldGreen
import com.depi.graduationproject.core.theme.GreenBg
import com.depi.graduationproject.core.theme.InputBlack
import com.depi.graduationproject.core.theme.LprDimens
import com.depi.graduationproject.core.theme.MutedText
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.PanelSurface
import com.depi.graduationproject.core.theme.PinkHot
import com.depi.graduationproject.core.theme.PrimaryText
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.core.theme.TinyCaps
import com.depi.graduationproject.presentation.components.BackHeader
import com.depi.graduationproject.core.theme.Hairline
import com.depi.graduationproject.presentation.components.SectionHeader
import androidx.compose.ui.tooling.preview.Preview

@Preview(widthDp = 360, heightDp = 800, showBackground = true, backgroundColor = 0xFF08090D)
@Composable
fun SettingsReferencePreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(LprDimens.ScreenPadding)
            .verticalScroll(rememberScrollState())
    ) {
        BackHeader(
            title = "Settings",
            onBackClick = {}
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader(
            title = "Pricing Configuration",
            icon = Icons.Default.AttachMoney,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsNumberCard(
            label = "Hourly Rate",
            value = "15.00",
            suffix = "EGP",
            helper = "Cost per hour of parking"
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsNumberCard(
            label = "Capacity",
            value = "75",
            suffix = "spots",
            helper = "Total parking spots available"
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(
            title = "Automation Settings",
            icon = Icons.Default.DirectionsCar,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsSwitchRow(
            title = "Auto Gate Open",
            subtitle = "Automatically open gate after payment",
            isChecked = true,
            onCheckedChange = {}
        )

        Spacer(modifier = Modifier.height(12.dp))

        SettingsSwitchRow(
            title = "Push Notifications",
            subtitle = "Receive alerts for overstay and sync",
            isChecked = true,
            onCheckedChange = {}
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(
            title = "Zone Distribution",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        listOf(
            Triple("Zone A", "Premium Covered", "20 spots"),
            Triple("Zone B", "Standard Open", "30 spots"),
            Triple("Zone C", "Economy Parking", "25 spots")
        ).forEach { (zone, desc, spots) ->
            ZoneDistributionRow(
                zone = zone,
                description = desc,
                spots = spots
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsNumberCard(
    label: String,
    value: String,
    suffix: String,
    helper: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LprDimens.CardRadius))
            .background(PanelSurface)
            .padding(16.dp)
    ) {
        Text(
            text = label,
            style = TinyCaps,
            color = MutedText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(InputBlack)
                    .border(1.dp, Hairline, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    ),
                    cursorBrush = SolidColor(NeonPink),
                    value = value,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                text = suffix,
                style = MaterialTheme.typography.bodyLarge,
                color = SecondaryText
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = helper,
            style = MaterialTheme.typography.bodySmall,
            color = MutedText
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LprDimens.CardRadius))
            .background(PanelSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MutedText
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryText,
                checkedTrackColor = NeonPink,
                uncheckedThumbColor = MutedText,
                uncheckedTrackColor = PanelSurface,
                uncheckedBorderColor = Hairline
            )
        )
    }
}

@Composable
private fun ZoneDistributionRow(
    zone: String,
    description: String,
    spots: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = zone,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MutedText
            )
        }
        Text(
            text = spots,
            style = MaterialTheme.typography.bodyMedium,
            color = EmeraldGreen
        )
    }
}

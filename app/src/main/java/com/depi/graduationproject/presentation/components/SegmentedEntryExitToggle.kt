package com.depi.graduationproject.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.core.theme.LprDimens
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.DeepPurple
import com.depi.graduationproject.core.theme.PanelSurfaceAlt
import com.depi.graduationproject.core.theme.PrimaryText

enum class EntryExitMode {
    Entry,
    Exit
}

@Composable
fun SegmentedEntryExitToggle(
    selectedMode: EntryExitMode,
    onModeSelected: (EntryExitMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = NeonPink
    val inactiveColor = DeepPurple

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(LprDimens.ToggleHeight)
            .clip(RoundedCornerShape(LprDimens.ToggleHeight / 2))
            .background(PanelSurfaceAlt)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        EntryExitMode.entries.forEach { mode ->
            val isSelected = mode == selectedMode
            val backgroundColor = if (isSelected) activeColor else Color.Transparent
            val textColor by animateColorAsState(
                targetValue = if (isSelected) PrimaryText else PrimaryText.copy(alpha = 0.6f),
                label = "toggle_text"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(21.dp))
                    .background(backgroundColor)
                    .clickable { onModeSelected(mode) }
                    .semantics { contentDescription = if (isSelected) "${mode.name} selected" else mode.name },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.name,
                    color = textColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

package com.depi.graduationproject.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.depi.graduationproject.core.theme.CardSurface
import com.depi.graduationproject.core.theme.GraduationProjectTheme
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.core.utils.HapticType
import com.depi.graduationproject.core.utils.rememberHapticFeedback

enum class NavItem {
    HOME, HISTORY, ADD, SETTINGS
}

@Composable
fun BottomNavLayout(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(CardSurface)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = Icons.Default.Home,
            label = "Home",
            isSelected = selectedItem == NavItem.HOME,
            onClick = { onItemSelected(NavItem.HOME) },
            modifier = Modifier.weight(1f)
        )
        BottomNavItem(
            icon = Icons.Default.History,
            label = "History",
            isSelected = selectedItem == NavItem.HISTORY,
            onClick = { onItemSelected(NavItem.HISTORY) },
            modifier = Modifier.weight(1f)
        )
        // Empty space for FABs in the middle, or just regular items
        BottomNavItem(
            icon = Icons.Default.Add,
            label = "Manual",
            isSelected = selectedItem == NavItem.ADD,
            onClick = { onItemSelected(NavItem.ADD) },
            modifier = Modifier.weight(1f)
        )
        BottomNavItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            isSelected = selectedItem == NavItem.SETTINGS,
            onClick = { onItemSelected(NavItem.SETTINGS) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (isSelected) NeonPink else SecondaryText
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = rememberHapticFeedback()

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .semantics {
                contentDescription = label
                role = Role.Button
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic(HapticType.CLICK)
                    onClick()
                }
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun BottomNavLayoutPreview() {
    GraduationProjectTheme {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            BottomNavLayout(
                selectedItem = NavItem.HOME,
                onItemSelected = {}
            )
        }
    }
}
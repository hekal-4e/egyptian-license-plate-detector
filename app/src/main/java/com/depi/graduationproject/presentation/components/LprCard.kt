package com.depi.graduationproject.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.core.theme.AppBackground
import com.depi.graduationproject.core.theme.CardSurface
import com.depi.graduationproject.core.theme.EmeraldGreen
import com.depi.graduationproject.core.theme.GreenBg
import com.depi.graduationproject.core.theme.Hairline
import com.depi.graduationproject.core.theme.InputBlack
import com.depi.graduationproject.core.theme.LprDimens
import com.depi.graduationproject.core.theme.PanelSurface
import com.depi.graduationproject.core.theme.PanelSurfaceAlt
import com.depi.graduationproject.core.theme.PinkHot
import com.depi.graduationproject.core.theme.PrimaryHorizontalGradient
import com.depi.graduationproject.core.theme.PrimaryText
import com.depi.graduationproject.core.theme.PurpleHot

enum class LprCardVariant {
    Default,
    Elevated,
    Input,
    Selected,
    Success
}

@Composable
fun LprCard(
    modifier: Modifier = Modifier,
    variant: LprCardVariant = LprCardVariant.Default,
    shape: Shape = RoundedCornerShape(LprDimens.CardRadius),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val backgroundColor = when (variant) {
        LprCardVariant.Default -> PanelSurface
        LprCardVariant.Elevated -> PanelSurfaceAlt
        LprCardVariant.Input -> InputBlack
        LprCardVariant.Selected -> PanelSurface
        LprCardVariant.Success -> GreenBg
    }

    val borderModifier = when (variant) {
        LprCardVariant.Elevated -> Modifier.border(1.dp, Hairline, shape)
        LprCardVariant.Selected -> Modifier.border(2.dp, PrimaryHorizontalGradient, shape)
        else -> Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .then(borderModifier)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun LprCardOutlined(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    LprCard(
        modifier = modifier,
        variant = LprCardVariant.Input,
        shape = RoundedCornerShape(LprDimens.CardRadius)
    ) {
        content()
    }
}

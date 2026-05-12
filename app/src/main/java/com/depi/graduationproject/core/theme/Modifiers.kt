package com.depi.graduationproject.core.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.neonGlow(
    color: Color = NeonPink,
    radius: Dp = 16.dp,
    alpha: Float = 0.6f,
    offsetY: Dp = 0.dp
): Modifier = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            asFrameworkPaint().apply {
                this.color = color.copy(alpha = alpha).toArgb()
                this.setShadowLayer(
                    radius.toPx(),
                    0f,
                    offsetY.toPx(),
                    color.copy(alpha = alpha).toArgb()
                )
            }
        }
        canvas.drawRect(0f, 0f, size.width, size.height, paint)
    }
}

fun Modifier.softCardBorder(
    color: Color = Hairline,
    width: Dp = 1.dp
): Modifier = this.border(
    width = width,
    color = color,
    shape = RoundedCornerShape(LprDimens.CardRadius)
)

fun Modifier.gradientBorder(
    gradient: Brush = PrimaryHorizontalGradient,
    width: Dp = 2.dp,
    shape: Shape
): Modifier = this.border(
    width = width,
    brush = gradient,
    shape = shape
)

fun Modifier.screenHorizontalPadding(): Modifier =
    this.padding(horizontal = LprDimens.ScreenPadding)

fun Modifier.cardPadding(): Modifier =
    this.padding(LprDimens.CardRadius)

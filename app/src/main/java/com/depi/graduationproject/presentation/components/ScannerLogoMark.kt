package com.depi.graduationproject.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.core.theme.DeepPurple
import com.depi.graduationproject.core.theme.NeonPink

@Composable
fun ScannerLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    cornerLength: Dp = 12.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cl = cornerLength.toPx()
        val sw = 3.dp.toPx()
        val halfStroke = sw / 2

        val plateW = w * 0.7f
        val plateH = h * 0.3f
        val plateLeft = (w - plateW) / 2
        val plateTop = (h - plateH) / 2

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(plateLeft, plateTop),
            size = androidx.compose.ui.geometry.Size(plateW, plateH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )

        val corners = listOf(
            Triple(plateLeft, plateTop, Offset(1f, 1f)),
            Triple(plateLeft + plateW, plateTop, Offset(-1f, 1f)),
            Triple(plateLeft, plateTop + plateH, Offset(1f, -1f)),
            Triple(plateLeft + plateW, plateTop + plateH, Offset(-1f, -1f))
        )

        corners.forEach { (cx, cy, dir) ->
            val (dx, dy) = dir
            drawLine(
                color = NeonPink,
                start = Offset(cx, cy),
                end = Offset(cx + cl * dx, cy),
                strokeWidth = sw,
                cap = StrokeCap.Round
            )
            drawLine(
                color = NeonPink,
                start = Offset(cx, cy),
                end = Offset(cx, cy + cl * dy),
                strokeWidth = sw,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun ScannerLogoMarkWithGlow(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val center = Offset(size.toPx() / 2, size.toPx() / 2)
        val glowRadius = size.toPx() / 2

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonPink.copy(alpha = 0.4f),
                    DeepPurple.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = center,
                radius = glowRadius
            ),
            radius = glowRadius
        )
    }
}

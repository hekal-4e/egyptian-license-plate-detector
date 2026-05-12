package com.depi.graduationproject.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.core.theme.PinkHot

@Composable
fun PlateTargetFrame(
    modifier: Modifier = Modifier
) {
    val frameWidth = 280.dp
    val frameHeight = 100.dp
    val cornerLength = 24.dp
    val strokeWidth = 4f

    Canvas(modifier = modifier.size(frameWidth, frameHeight)) {
        val w = size.width
        val h = size.height
        val cl = cornerLength.value * density
        val sw = strokeWidth

        val halfStroke = sw / 2

        drawRoundRect(
            color = Color.Transparent,
            cornerRadius = CornerRadius(8.dp.value * density),
            size = size,
            style = Stroke(width = sw)
        )

        val tl = listOf(
            Offset(halfStroke, h / 2) to Offset(cl, h / 2),
            Offset(halfStroke, halfStroke) to Offset(cl, halfStroke),
            Offset(halfStroke, halfStroke) to Offset(halfStroke, cl)
        )
        tl.forEach { (start, end) ->
            drawLine(PinkHot, start, end, sw)
        }

        val tr = listOf(
            Offset(w - cl, h / 2) to Offset(w - halfStroke, h / 2),
            Offset(w - cl, halfStroke) to Offset(w - halfStroke, halfStroke),
            Offset(w - halfStroke, halfStroke) to Offset(w - halfStroke, cl)
        )
        tr.forEach { (start, end) ->
            drawLine(PinkHot, start, end, sw)
        }

        val bl = listOf(
            Offset(halfStroke, h / 2) to Offset(cl, h / 2),
            Offset(halfStroke, h - cl) to Offset(halfStroke, h - halfStroke),
            Offset(halfStroke, h - halfStroke) to Offset(cl, h - halfStroke)
        )
        bl.forEach { (start, end) ->
            drawLine(PinkHot, start, end, sw)
        }

        val br = listOf(
            Offset(w - cl, h / 2) to Offset(w - halfStroke, h / 2),
            Offset(w - cl, h - cl) to Offset(w - halfStroke, h - halfStroke),
            Offset(w - halfStroke, h - cl) to Offset(w - halfStroke, h - halfStroke)
        )
        br.forEach { (start, end) ->
            drawLine(PinkHot, start, end, sw)
        }

        drawLine(
            color = PinkHot.copy(alpha = 0.7f),
            start = Offset(halfStroke, h / 2),
            end = Offset(w - halfStroke, h / 2),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}

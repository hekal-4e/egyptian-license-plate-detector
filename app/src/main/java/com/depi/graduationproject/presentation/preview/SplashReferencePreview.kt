package com.depi.graduationproject.presentation.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.depi.graduationproject.core.theme.AppBackground
import com.depi.graduationproject.core.theme.DeepPurple
import com.depi.graduationproject.core.theme.NeonPink
import com.depi.graduationproject.core.theme.PinkHot
import com.depi.graduationproject.core.theme.PrimaryText
import com.depi.graduationproject.core.theme.PurpleHot
import com.depi.graduationproject.core.theme.SecondaryText
import com.depi.graduationproject.presentation.components.GradientButton
import androidx.compose.ui.tooling.preview.Preview

@Preview(widthDp = 360, heightDp = 800, showBackground = true, backgroundColor = 0xFF08090D)
@Composable
fun SplashReferencePreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PinkHot.copy(alpha = 0.4f),
                            PurpleHot.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.width / 2
                    ),
                    radius = size.width / 2
                )
            }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
                    .background(
                        Brush.linearGradient(listOf(NeonPink, DeepPurple)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(60.dp)) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val w = size.width * 0.7f
                    val h = size.height * 0.3f

                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(cx - w / 2, cy - h / 2),
                        size = androidx.compose.ui.geometry.Size(w, h),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )

                    val cornerLen = 12.dp.toPx()
                    val strokeW = 3.dp.toPx()

                    listOf(
                        Offset(cx - w / 2, cy) to Offset(cx - w / 2 + cornerLen, cy),
                        Offset(cx - w / 2, cy) to Offset(cx - w / 2, cy - cornerLen),
                        Offset(cx + w / 2, cy) to Offset(cx + w / 2 - cornerLen, cy),
                        Offset(cx + w / 2, cy) to Offset(cx + w / 2, cy - cornerLen),
                        Offset(cx - w / 2, cy) to Offset(cx - w / 2 + cornerLen, cy),
                        Offset(cx - w / 2, cy) to Offset(cx - w / 2, cy + cornerLen),
                        Offset(cx + w / 2, cy) to Offset(cx + w / 2 - cornerLen, cy),
                        Offset(cx + w / 2, cy) to Offset(cx + w / 2, cy + cornerLen),
                    ).forEach { (start, end) ->
                        drawLine(
                            color = NeonPink,
                            start = start,
                            end = end,
                            strokeWidth = strokeW
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "LPR-Edge",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Automating Garage Management\nwith Edge-AI",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.weight(1f))

        GradientButton(
            text = "Get Started",
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

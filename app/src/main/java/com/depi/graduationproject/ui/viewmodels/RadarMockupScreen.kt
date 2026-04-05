package com.example.radarapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// --- Colors Palette ---
val DeepSpaceBlue = Color(0xFF0A0E17)
val NeonCyan = Color(0xFF00F0FF)
val NeonPurple = Color(0xFFBD00FF)
val GlassWhite = Color(0x1AFFFFFF)

@Composable
fun CreativeRadarScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFF1A233A), DeepSpaceBlue))),
        contentAlignment = Alignment.Center
    ) {
        // 1. شبكة الخلفية المتحركة
        MovingGridBackground()

        // 2. الرادار الرئيسي
        RadarScannerMain()

        // 3. واجهة البيانات (HUD)
        HudOverlay()

        // 4. بطاقة مستخدم تم اكتشافه (محاكاة)
        DetectedUserCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }
}

@Composable
fun RadarScannerMain() {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarMain")

    // دوران الماسح
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing)), label = "Spin"
    )

    Box(modifier = Modifier.size(380.dp), contentAlignment = Alignment.Center) {
        // الحلقات الخارجية المتوهجة
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = center
            val radius = size.minDimension / 2

            // رسم حلقات متعددة
            for (i in 1..4) {
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.1f * i),
                    radius = radius * (i / 4f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // رسم خطوط التقاطع
            drawLine(
                color = NeonCyan.copy(alpha = 0.2f),
                start = Offset(center.x, 0f), end = Offset(center.x, size.height),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = NeonCyan.copy(alpha = 0.2f),
                start = Offset(0f, center.y), end = Offset(size.width, center.y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // الماسح الضوئي (الشعاع)
        Canvas(modifier = Modifier.fillMaxSize()) {
            rotate(rotation) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        0.8f to Color.Transparent,
                        0.95f to NeonCyan.copy(alpha = 0.1f),
                        1.0f to NeonCyan.copy(alpha = 0.8f)
                    )
                )
                // خط حاد في مقدمة الماسح
                drawLine(
                    color = NeonCyan,
                    start = center,
                    end = Offset(center.x + size.minDimension / 2, center.y),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // نقاط الأشخاص (Blips)
        // نضع 3 أشخاص في أماكن عشوائية
        RadarBlipItem(angle = 45f, distance = 0.4f, delay = 0)
        RadarBlipItem(angle = 190f, distance = 0.7f, delay = 1000)
        RadarBlipItem(angle = 320f, distance = 0.5f, delay = 2000)

        // أنا (المركز)
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(NeonPurple, CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}

@Composable
fun RadarBlipItem(angle: Float, distance: Float, delay: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "BlipPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = delay), RepeatMode.Restart), label = "Scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = delay), RepeatMode.Restart), label = "Alpha"
    )

    // حساب الموقع بناء على الزاوية
    // (هذا مجرد Hack سريع للعرض، في التطبيق الحقيقي نستخدم Sin/Cos)
    val rad = Math.toRadians(angle.toDouble())
    val alignX = (distance * cos(rad)).toFloat()
    val alignY = (distance * sin(rad)).toFloat()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (180 * alignX).dp, y = (180 * alignY).dp) // 180 is approx half radar size
        ) {
            // موجة التموج
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .scale(pulseScale)
                    .alpha(pulseAlpha)
                    .background(NeonCyan, CircleShape)
            )
            // النقطة الثابتة
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, NeonCyan, CircleShape)
            )
        }
    }
}

@Composable
fun HudOverlay() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp, start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SCANNING AREA",
            color = NeonCyan,
            style = MaterialTheme.typography.labelLarge,
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // محاكاة إحداثيات تتغير
        var coords by remember { mutableStateOf("LAT: 30.0444 | LNG: 31.2357") }
        LaunchedEffect(Unit) {
            while (true) {
                coords = "LAT: 30.${Random.nextInt(1000, 9999)} | LNG: 31.${Random.nextInt(1000, 9999)}"
                delay(200)
            }
        }

        Text(
            text = coords,
            color = NeonCyan.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun DetectedUserCard(modifier: Modifier = Modifier) {
    // أنيميشن ظهور البطاقة
    val infiniteTransition = rememberInfiniteTransition(label = "CardPulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse), label = "Border"
    )

    Row(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(20.dp))
            .background(GlassWhite) // Glass effect background
            .border(1.dp, NeonCyan.copy(alpha = borderAlpha), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // أيقونة المستخدم (مجهول)
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(NeonCyan.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("?", color = NeonCyan, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "New Connection Nearby!",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "~15 Meters away",
                color = NeonCyan,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // زر الاتصال
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text("Chat")
        }
    }
}

@Composable
fun MovingGridBackground() {
    // خلفية بسيطة تتحرك ببطء لتعطي عمق
    // (يمكن تركها فارغة للتبسيط، لكن وجودها يعطي احترافية)
    Canvas(modifier = Modifier.fillMaxSize().alpha(0.05f)) {
        val step = 40.dp.toPx()
        for (i in 0..(size.width / step).toInt()) {
            drawLine(Color.White, Offset(i * step, 0f), Offset(i * step, size.height))
        }
        for (i in 0..(size.height / step).toInt()) {
            drawLine(Color.White, Offset(0f, i * step), Offset(size.width, i * step))
        }
    }
}

@Preview(showBackground = true, name = "Cyberpunk Radar")
@Composable
fun PreviewCreativeRadar() {
    CreativeRadarScreen()
}
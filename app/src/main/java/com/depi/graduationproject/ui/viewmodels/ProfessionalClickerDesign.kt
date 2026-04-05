package com.depi.graduationproject.ui.viewmodels

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. الدالة الرئيسية للتصميم
@Composable
fun ProfessionalClickerDesign() {
    // حالة العداد (للعرض فقط في البريفيو)
    var count by remember { mutableStateOf(0) }

    // خلفية متدرجة (Night Mode Gradient)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1A2E), // Dark Blue
            Color(0xFF16213E),
            Color(0xFF0F3460)  // Lighter Blue
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            // عداد النقرات
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TOTAL CLICKS",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 18.sp,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = String.format("%02d", count),
                    color = Color.White,
                    fontSize = 110.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.displayLarge,
                    // التعديل هنا: حذفنا blurRadius واكتفينا بالظل العادي
                    modifier = Modifier.shadow(elevation = 10.dp)
                )
            }

            // الزر التفاعلي
            NeonButton(onClick = { count++ })

            // نص سفلي
            Text(
                text = "Tap to Win!",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 14.sp
            )
        }
    }
}

// 2. مكون الزر التفاعلي (مفصول لسهولة القراءة)
@Composable
fun NeonButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // أنيميشن الضغط
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(durationMillis = 80), label = "ButtonScale"
    )

    // ألوان الزر (Cyberpunk Pink/Purple)
    val buttonGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF0080), Color(0xFF7928CA))
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .scale(scale)
            .size(240.dp)
            .shadow(
                elevation = if (isPressed) 10.dp else 40.dp,
                shape = CircleShape,
                spotColor = Color(0xFFFF0080),
                ambientColor = Color(0xFF7928CA)
            )
            .clip(CircleShape)
            .background(buttonGradient)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "PRESS",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "ME",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 24.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

// 3. إعدادات الـ Preview (عشان تظهر كشاشة كاملة)
@Preview(
    showBackground = true,
    showSystemUi = true, // يظهر شريط الحالة ليبدو كأنه سكرين شوت حقيقي من هاتف
    device = "id:pixel_6" // حجم هاتف قياسي
)
@Composable
fun PreviewClickerScreen() {
    ProfessionalClickerDesign()
}
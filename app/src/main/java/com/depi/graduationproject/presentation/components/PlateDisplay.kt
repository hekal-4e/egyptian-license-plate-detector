package com.depi.graduationproject.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.depi.graduationproject.core.theme.GraduationProjectTheme

@Composable
fun PlateDisplay(
    numbers: String,
    letters: String,
    modifier: Modifier = Modifier
) {
    val containerShape = MaterialTheme.shapes.medium // 8dp
    val textStyle = TextStyle(
        color = Color.Black,
        fontFamily = com.depi.graduationproject.core.theme.PlateMono,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(containerShape)
            .background(Color.White)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Numbers (Left, LTR)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = numbers,
                    style = textStyle,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Letters (Right, RTL)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = letters,
                    style = textStyle,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun PlateDisplayPreview() {
    GraduationProjectTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PlateDisplay(
                numbers = "1234",
                letters = "أ ب ج"
            )
        }
    }
}

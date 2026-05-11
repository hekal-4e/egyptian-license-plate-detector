package com.depi.graduationproject.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.depi.graduationproject.core.theme.AppGradient
import com.depi.graduationproject.core.theme.GraduationProjectTheme
import com.depi.graduationproject.core.theme.SecondaryText

@Composable
fun LicensePlateTextField(
    numbers: String,
    onNumbersChange: (String) -> Unit,
    letters: String,
    onLettersChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val containerShape = MaterialTheme.shapes.medium // 8dp
    val textStyle = TextStyle(
        color = Color.Black,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(containerShape)
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(bottom = 4.dp), // space for gradient underline
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Numbers (Left, LTR)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = numbers,
                        onValueChange = { if (it.length <= 4) onNumbersChange(it) },
                        textStyle = textStyle,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        cursorBrush = SolidColor(Color.Black),
                        modifier = Modifier.semantics { contentDescription = "License plate numbers" },
                        decorationBox = { innerTextField ->
                            if (numbers.isEmpty()) {
                                Text(
                                    text = "1234",
                                    style = textStyle.copy(color = SecondaryText.copy(alpha = 0.5f))
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            // Divider
            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .width(1.dp),
                color = Color.LightGray
            )

            // Letters (Right, RTL)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = letters,
                        onValueChange = { if (it.length <= 3) onLettersChange(it) },
                        textStyle = textStyle,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        cursorBrush = SolidColor(Color.Black),
                        modifier = Modifier.semantics { contentDescription = "License plate letters" },
                        decorationBox = { innerTextField ->
                            if (letters.isEmpty()) {
                                Text(
                                    text = "أ ب ج",
                                    style = textStyle.copy(color = SecondaryText.copy(alpha = 0.5f))
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
        }

        // Gradient Underline
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(4.dp)
                .background(AppGradient)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun LicensePlateTextFieldPreview() {
    GraduationProjectTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            LicensePlateTextField(
                numbers = "123",
                onNumbersChange = {},
                letters = "أ ب",
                onLettersChange = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun LicensePlateTextFieldEmptyPreview() {
    GraduationProjectTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            LicensePlateTextField(
                numbers = "",
                onNumbersChange = {},
                letters = "",
                onLettersChange = {}
            )
        }
    }
}
package com.depi.graduationproject.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// تعريف باليتة الألوان الفاتحة الجذابة
private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryBlue.copy(alpha = 0.1f),
    onPrimaryContainer = PrimaryBlue,

    secondary = SecondaryAmber,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryAmber.copy(alpha = 0.1f),
    onSecondaryContainer = SecondaryAmber,

    tertiary = TertiaryTeal,

    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
)

@Composable
fun GraduationProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // حالياً سنستخدم الفاتح كأساس
    content: @Composable () -> Unit
) {
    // هنجبره يستخدم الـ Light Theme مؤقتاً عشان نشوف الألوان الحلوة
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // نخلي شريط الحالة (Status Bar) لونه أزرق جذاب
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.depi.graduationproject.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Dark-Only Color Scheme ────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = NeonPink,
    onPrimary = PrimaryText,
    primaryContainer = DeepPurple,
    onPrimaryContainer = PrimaryText,

    secondary = DeepPurple,
    onSecondary = PrimaryText,
    secondaryContainer = DeepPurple.copy(alpha = 0.3f),
    onSecondaryContainer = PrimaryText,

    tertiary = EmeraldGreen,
    onTertiary = PrimaryText,

    background = Background,
    onBackground = PrimaryText,
    surface = CardSurface,
    onSurface = PrimaryText,
    surfaceVariant = CardSurfaceVariant,
    onSurfaceVariant = SecondaryText,

    error = ErrorRed,
    onError = PrimaryText,
)

// ── Theme Composable ──────────────────────────────────────────
@Composable
fun GraduationProjectTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LprTypography,
        shapes = LprShapes,
        content = content
    )
}

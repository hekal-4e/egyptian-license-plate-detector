package com.depi.graduationproject.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Screenshot-Accurate Background Layers ─────────────────────
val ScreenBlack = Color(0xFF050607)
val AppBackground = Color(0xFF08090D)
val PanelSurface = Color(0xFF17181D)
val PanelSurfaceAlt = Color(0xFF1D1E24)
val InputBlack = Color(0xFF07080B)
val Hairline = Color(0xFF2A2B31)

// ── Background ──────────────────────────────────────────────
val Background = Color(0xFF0D1117)
val CardSurface = Color(0xFF1A1D24)
val CardSurfaceVariant = Color(0xFF24272E)

// ── Brand Accent ─────────────────────────────────────────────
val NeonPink = Color(0xFFFF2A7A)
val PinkHot = Color(0xFFFF1F6D)
val PurpleHot = Color(0xFF9827FF)
val DeepPurple = Color(0xFF7B2CBF)
val PurpleIconBg = Color(0xFF3B176C)

// ── Semantic ─────────────────────────────────────────────────
val EmeraldGreen = Color(0xFF00C853)
val GreenBg = Color(0xFF0D372B)
val SuccessMint = Color(0xFF11D69B)
val ErrorRed = Color(0xFFFF1744)
val WarningAmber = Color(0xFFFFAB00)

// ── Text ─────────────────────────────────────────────────────
val PrimaryText = Color(0xFFFFFFFF)
val SecondaryText = Color(0xFFA0A0A0)
val MutedText = Color(0xFF8B8D98)
val DimText = Color(0xFF6F717B)
val DisabledText = Color(0xFF5A5A5A)

// ── Gradient Brush (theme-level token) ───────────────────────
val AppGradient: Brush
    get() = Brush.linearGradient(
        colors = listOf(NeonPink, DeepPurple)
    )

val PrimaryHorizontalGradient: Brush
    get() = Brush.horizontalGradient(listOf(PinkHot, PurpleHot))

val PrimaryVerticalGradient: Brush
    get() = Brush.verticalGradient(listOf(PinkHot, PurpleHot))

val ChartBarGradient: Brush
    get() = Brush.verticalGradient(listOf(PinkHot, PurpleHot))

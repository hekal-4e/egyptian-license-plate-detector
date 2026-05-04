package com.depi.graduationproject.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Background ──────────────────────────────────────────────
val Background = Color(0xFF0D1117)
val CardSurface = Color(0xFF1A1D24)
val CardSurfaceVariant = Color(0xFF24272E)

// ── Brand Accent ─────────────────────────────────────────────
val NeonPink = Color(0xFFFF2A7A)
val DeepPurple = Color(0xFF7B2CBF)

// ── Semantic ─────────────────────────────────────────────────
val EmeraldGreen = Color(0xFF00C853)
val ErrorRed = Color(0xFFFF1744)
val WarningAmber = Color(0xFFFFAB00)

// ── Text ─────────────────────────────────────────────────────
val PrimaryText = Color(0xFFFFFFFF)
val SecondaryText = Color(0xFFA0A0A0)
val DisabledText = Color(0xFF5A5A5A)

// ── Gradient Brush (theme-level token) ───────────────────────
val AppGradient: Brush
    get() = Brush.linearGradient(
        colors = listOf(NeonPink, DeepPurple)
    )

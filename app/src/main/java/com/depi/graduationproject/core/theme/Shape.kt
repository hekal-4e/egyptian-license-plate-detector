package com.depi.graduationproject.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ── Shape Tokens (per PRD) ────────────────────────────────────
val LprShapes = Shapes(
    // 16dp rounded corners for cards
    large = RoundedCornerShape(16.dp),
    // 8dp for license plate boxes
    medium = RoundedCornerShape(8.dp),
    // 4dp for bottom sheet drag handle, small elements
    small = RoundedCornerShape(4.dp),
    // Fully-rounded (50%) for pill buttons and toggle segments
    extraLarge = RoundedCornerShape(50),
)

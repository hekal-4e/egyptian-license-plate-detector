package com.depi.graduationproject.core.utils

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

/**
 * Utility for triggering haptic feedback in Compose.
 */
@Composable
fun rememberHapticFeedback(): (Int) -> Unit {
    val view = LocalView.current
    return { type ->
        view.performHapticFeedback(type)
    }
}

object HapticType {
    const val CLICK = HapticFeedbackConstants.VIRTUAL_KEY
    const val LONG_PRESS = HapticFeedbackConstants.LONG_PRESS
    val SUCCESS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        HapticFeedbackConstants.CONFIRM
    } else {
        HapticFeedbackConstants.VIRTUAL_KEY
    }
    val ERROR = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        HapticFeedbackConstants.REJECT
    } else {
        HapticFeedbackConstants.LONG_PRESS
    }
    const val TOGGLE = HapticFeedbackConstants.CLOCK_TICK
}
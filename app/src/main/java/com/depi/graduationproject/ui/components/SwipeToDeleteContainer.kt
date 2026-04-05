package com.depi.graduationproject.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SwipeToDeleteContainer(
    item: T,
    onDelete: (T) -> Unit,
    content: @Composable (T) -> Unit
) {
    var isRemoved by remember { mutableStateOf(false) }

    // 1. التحديث هنا: الاسم الجديد للـ State
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            // 2. التحديث هنا: التأكد من اتجاه السحب (EndToStart)
            if (value == SwipeToDismissBoxValue.EndToStart) {
                isRemoved = true
                true
            } else {
                false
            }
        }
    )

    LaunchedEffect(isRemoved) {
        if (isRemoved) {
            onDelete(item)
        }
    }

    // 3. التحديث هنا: الاسم الجديد للمكون (Box)
    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            val color by animateColorAsState(
                if (state.targetValue == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent,
                label = "Delete Background"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        },
        content = { content(item) },
        // تفعيل السحب من اليمين لليسار فقط
        enableDismissFromEndToStart = true,
        enableDismissFromStartToEnd = false
    )
}
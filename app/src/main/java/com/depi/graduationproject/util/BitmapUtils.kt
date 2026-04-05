package com.depi.graduationproject.util

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.roundToInt

object BitmapUtils {
    fun cropBitmap(source: Bitmap, rect: Rect): Bitmap? {
        if (rect.width() <= 0 || rect.height() <= 0) return null
        val left = rect.left.coerceAtLeast(0)
        val top = rect.top.coerceAtLeast(0)
        val width = rect.width().coerceAtMost(source.width - left)
        val height = rect.height().coerceAtMost(source.height - top)
        if (width <= 0 || height <= 0) return null
        return try {
            Bitmap.createBitmap(source, left, top, width, height)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun cropBitmap(source: Bitmap, rectF: RectF): Bitmap? {
        // Round the RectF to nearest ints for cropping
        val left = rectF.left.roundToInt()
        val top = rectF.top.roundToInt()
        val right = rectF.right.roundToInt()
        val bottom = rectF.bottom.roundToInt()
        val rect = Rect(left, top, right, bottom)
        return cropBitmap(source, rect)
    }
}
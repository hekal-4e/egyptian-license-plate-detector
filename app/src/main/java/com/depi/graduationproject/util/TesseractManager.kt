package com.depi.graduationproject.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class TesseractManager(private val context: Context) {

    private val tessApi = TessBaseAPI()
    private var isInitialized = false
    private var usingLegacy = false

    companion object {
        private const val TAG = "OCR_DEBUG"
        // The ONLY letters that appear on Egyptian license plates
        private const val WHITELIST = "أبجدرسصطعفقكلمنهوي0123456789٠١٢٣٤٥٦٧٨٩"
    }

    fun init(): Boolean {
        if (isInitialized) return true

        try {
            val dataPath = File(context.filesDir, "tesseract").absolutePath
            val tessDataFolder = File(dataPath, "tessdata")
            if (!tessDataFolder.exists()) tessDataFolder.mkdirs()

            copyFileFromAssets("tessdata/ara.traineddata", File(tessDataFolder, "ara.traineddata"))

            // ═════════════════════════════════════════════════════════
            // CRITICAL: Use OEM_TESSERACT_ONLY (legacy engine)
            // because VAR_CHAR_WHITELIST is IGNORED in LSTM mode!
            // With LSTM, Tesseract matches against ALL Arabic characters
            // (hundreds) instead of our restricted 17-letter plate set.
            // Legacy mode + whitelist = only plate characters considered.
            // ═════════════════════════════════════════════════════════
            var success = tessApi.init(dataPath, "ara", TessBaseAPI.OEM_TESSERACT_ONLY)

            if (success) {
                usingLegacy = true
                Log.d(TAG, "✅ Initialized with OEM_TESSERACT_ONLY (legacy)")
            } else {
                // Fallback: if legacy mode fails (traineddata missing legacy data),
                // use OEM_DEFAULT which picks whatever is available
                Log.d(TAG, "⚠️ Legacy mode failed, trying OEM_DEFAULT...")
                success = tessApi.init(dataPath, "ara", TessBaseAPI.OEM_DEFAULT)
                if (success) {
                    usingLegacy = false
                    Log.d(TAG, "✅ Initialized with OEM_DEFAULT")
                }
            }

            if (success) {
                // Whitelist — only valid plate characters
                tessApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, WHITELIST)

                // PSM 7 = single line of text (set as default, may change in recognize)
                tessApi.pageSegMode = TessBaseAPI.PageSegMode.PSM_SINGLE_LINE

                // Tuned variables
                tessApi.setVariable("textord_heavy_nr", "1")
                tessApi.setVariable("textord_min_linesize", "2.5")
                tessApi.setVariable("classify_bln_numeric_mode", "0")

                Log.d(TAG, "✅ Tesseract Ready (legacy=$usingLegacy)")
                isInitialized = true
            } else {
                Log.e(TAG, "❌ Failed all OEM modes")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Init Error", e)
        }
        return isInitialized
    }

    /**
     * Multi-attempt recognition:
     * 1. Try with Otsu binarization + PSM 7
     * 2. If empty, try with mild preprocessing + PSM 7
     * 3. If empty, try with Otsu + PSM 6 (single block)
     * 4. If empty, try grayscale only + PSM 6
     * Returns the first non-empty result.
     */
    fun recognize(bitmap: Bitmap): String {
        if (!isInitialized) return ""

        // Attempt 1: Otsu binarization + PSM 7 (single line)
        val otsuImage = preprocessOtsu(bitmap)
        debugSaveBitmap(otsuImage, "otsu")
        tessApi.pageSegMode = TessBaseAPI.PageSegMode.PSM_SINGLE_LINE
        tessApi.setImage(otsuImage)
        var text = tessApi.utF8Text?.trim() ?: ""
        tessApi.clear()
        Log.d(TAG, "Attempt1 (Otsu+PSM7): '$text'")
        if (text.isNotBlank()) return text

        // Attempt 2: Mild preprocessing + PSM 7
        val mildImage = preprocessMild(bitmap)
        debugSaveBitmap(mildImage, "mild")
        tessApi.pageSegMode = TessBaseAPI.PageSegMode.PSM_SINGLE_LINE
        tessApi.setImage(mildImage)
        text = tessApi.utF8Text?.trim() ?: ""
        tessApi.clear()
        Log.d(TAG, "Attempt2 (Mild+PSM7): '$text'")
        if (text.isNotBlank()) return text

        // Attempt 3: Otsu + PSM 6 (single block — handles multiple elements)
        tessApi.pageSegMode = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK
        tessApi.setImage(otsuImage)
        text = tessApi.utF8Text?.trim() ?: ""
        tessApi.clear()
        Log.d(TAG, "Attempt3 (Otsu+PSM6): '$text'")
        if (text.isNotBlank()) return text

        // Attempt 4: Grayscale only + PSM 6 (let Tesseract do its own binarization)
        val grayImage = preprocessGrayscaleOnly(bitmap)
        debugSaveBitmap(grayImage, "gray")
        tessApi.pageSegMode = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK
        tessApi.setImage(grayImage)
        text = tessApi.utF8Text?.trim() ?: ""
        tessApi.clear()
        Log.d(TAG, "Attempt4 (Gray+PSM6): '$text'")

        return text
    }

    // ═══════════════════════════════════════════════════════
    // Preprocessing Pipeline 1: Otsu's Automatic Binarization
    // ═══════════════════════════════════════════════════════
    private fun preprocessOtsu(bitmap: Bitmap): Bitmap {
        val scaled = Bitmap.createScaledBitmap(bitmap, 2000, 600, true)

        // Step 1: Convert to grayscale with mild contrast
        val gray = applyGrayscaleAndContrast(scaled, 1.8f, -40f)

        // Step 2: Get pixel data
        val width = gray.width
        val height = gray.height
        val pixels = IntArray(width * height)
        gray.getPixels(pixels, 0, width, 0, 0, width, height)

        // Step 3: Convert to gray values
        val grayValues = IntArray(pixels.size)
        for (i in pixels.indices) {
            val r = (pixels[i] shr 16) and 0xFF
            val g = (pixels[i] shr 8) and 0xFF
            val b = pixels[i] and 0xFF
            grayValues[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        }

        // Step 4: Compute Otsu's optimal threshold automatically
        val threshold = computeOtsuThreshold(grayValues)
        Log.d(TAG, "Otsu threshold: $threshold")

        // Step 5: Binarize
        for (i in pixels.indices) {
            pixels[i] = if (grayValues[i] > threshold) {
                0xFFFFFFFF.toInt()  // White
            } else {
                0xFF000000.toInt()  // Black
            }
        }
        gray.setPixels(pixels, 0, width, 0, 0, width, height)

        // Step 6: Auto-invert if needed
        return autoInvert(gray, pixels)
    }

    // ═══════════════════════════════════════════════════════
    // Preprocessing Pipeline 2: Mild (preserve detail)
    // ═══════════════════════════════════════════════════════
    private fun preprocessMild(bitmap: Bitmap): Bitmap {
        val scaled = Bitmap.createScaledBitmap(bitmap, 2000, 600, true)

        // Gentle grayscale + mild contrast
        val gray = applyGrayscaleAndContrast(scaled, 1.5f, -20f)

        // Fixed threshold (gentle)
        val width = gray.width
        val height = gray.height
        val pixels = IntArray(width * height)
        gray.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val r = (pixels[i] shr 16) and 0xFF
            val g = (pixels[i] shr 8) and 0xFF
            val b = pixels[i] and 0xFF
            val grayVal = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            pixels[i] = if (grayVal > 140) {
                0xFFFFFFFF.toInt()
            } else {
                0xFF000000.toInt()
            }
        }
        gray.setPixels(pixels, 0, width, 0, 0, width, height)

        return autoInvert(gray, pixels)
    }

    // ═══════════════════════════════════════════════════════
    // Preprocessing Pipeline 3: Grayscale only (no binarization)
    // Let Tesseract handle its own internal binarization
    // ═══════════════════════════════════════════════════════
    private fun preprocessGrayscaleOnly(bitmap: Bitmap): Bitmap {
        val scaled = Bitmap.createScaledBitmap(bitmap, 2000, 600, true)
        return applyGrayscaleAndContrast(scaled, 1.3f, -10f)
    }

    // ═══════════════════════════════════════════════════════
    // Shared: Grayscale + Contrast
    // ═══════════════════════════════════════════════════════
    private fun applyGrayscaleAndContrast(
        bitmap: Bitmap,
        contrastScale: Float,
        contrastTranslate: Float
    ): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)

        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                contrastScale, 0f, 0f, 0f, contrastTranslate,
                0f, contrastScale, 0f, 0f, contrastTranslate,
                0f, 0f, contrastScale, 0f, contrastTranslate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        colorMatrix.postConcat(contrastMatrix)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return result
    }

    // ═══════════════════════════════════════════════════════
    // Otsu's Method — computes optimal binarization threshold
    // automatically from the image histogram
    // ═══════════════════════════════════════════════════════
    private fun computeOtsuThreshold(grayValues: IntArray): Int {
        // Build histogram (0-255)
        val histogram = IntArray(256)
        for (gray in grayValues) {
            histogram[gray.coerceIn(0, 255)]++
        }

        val total = grayValues.size
        var sumTotal = 0f
        for (i in 0..255) {
            sumTotal += (i * histogram[i]).toFloat()
        }

        var sumBackground = 0f
        var weightBackground = 0
        var maxVariance = 0f
        var bestThreshold = 128

        for (t in 0..255) {
            weightBackground += histogram[t]
            if (weightBackground == 0) continue

            val weightForeground = total - weightBackground
            if (weightForeground == 0) break

            sumBackground += (t * histogram[t]).toFloat()

            val meanBackground = sumBackground / weightBackground
            val meanForeground = (sumTotal - sumBackground) / weightForeground

            val betweenVariance = weightBackground.toFloat() * weightForeground.toFloat() *
                    (meanBackground - meanForeground) * (meanBackground - meanForeground)

            if (betweenVariance > maxVariance) {
                maxVariance = betweenVariance
                bestThreshold = t
            }
        }

        // Clamp to reasonable range for plates (130-180)
        return bestThreshold.coerceIn(130, 180)
    }

    // ═══════════════════════════════════════════════════════
    // Auto-invert: ensure black text on white background
    // ═══════════════════════════════════════════════════════
    private fun autoInvert(bitmap: Bitmap, pixels: IntArray): Bitmap {
        var blackCount = 0
        for (pixel in pixels) {
            if (pixel == 0xFF000000.toInt()) blackCount++
        }
        val blackRatio = blackCount.toFloat() / pixels.size

        if (blackRatio > 0.50f) {
            for (i in pixels.indices) {
                pixels[i] = if (pixels[i] == 0xFF000000.toInt()) {
                    0xFFFFFFFF.toInt()
                } else {
                    0xFF000000.toInt()
                }
            }
            bitmap.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            Log.d(TAG, "🔄 Auto-inverted (blackRatio=${String.format("%.2f", blackRatio)})")
        }
        return bitmap
    }

    private fun debugSaveBitmap(bitmap: Bitmap, tag: String) {
        try {
            val file = File(
                context.cacheDir,
                "debug_ocr_${tag}_${System.currentTimeMillis()}.png"
            )
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Log.d(TAG, "📸 Debug: ${file.name} (${bitmap.width}x${bitmap.height})")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save debug image", e)
        }
    }

    fun stop() {
        tessApi.stop()
        try {
            tessApi.recycle()
        } catch (e: Exception) { /* already recycled */ }
    }

    private fun copyFileFromAssets(assetPath: String, destFile: File) {
        if (destFile.exists()) {
            try {
                val assetSize = context.assets.open(assetPath).use { it.available().toLong() }
                if (destFile.length() == assetSize) return
                Log.d(TAG, "🔄 Traineddata updated: ${destFile.length()} → $assetSize bytes")
                destFile.delete()
            } catch (e: Exception) {
                return
            }
        }
        try {
            context.assets.open(assetPath).use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Log.d(TAG, "✅ Copied: $assetPath (${destFile.length()} bytes)")
        } catch (e: IOException) {
            Log.e(TAG, "❌ Failed copy: $assetPath", e)
        }
    }
}
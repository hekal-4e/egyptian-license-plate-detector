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

    fun init(): Boolean {
        if (isInitialized) return true

        try {
            val dataPath = File(context.filesDir, "tesseract").absolutePath
            val tessDataFolder = File(dataPath, "tessdata")
            if (!tessDataFolder.exists()) tessDataFolder.mkdirs()

            copyFileFromAssets("tessdata/ara.traineddata", File(tessDataFolder, "ara.traineddata"))

            val success = tessApi.init(dataPath, "ara", TessBaseAPI.OEM_LSTM_ONLY)

            if (success) {
                // Allow Arabic digits, English digits, and Arabic plate letters
                val whitelist = "0123456789٠١٢٣٤٥٦٧٨٩أبجدرسصطعفقكلمنهوي "
                
                tessApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, whitelist)

                // Ÿ” ŠŠ PSM „€ SPARSE_TEXT † „„„ˆ
                tessApi.pageSegMode = TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT

                // 🔥 إعدادات إضافية لتحسين الدقة
                tessApi.setVariable("tessedit_do_invert", "1")
                tessApi.setVariable("edges_max_children_per_outline", "40")

                Log.d("OCR_DEBUG", "✅ Tesseract Ready (Arabic Only)")
                isInitialized = true
            }
        } catch (e: Exception) {
            Log.e("OCR_DEBUG", "❌ Init Error", e)
        }
        return isInitialized
    }

    fun recognize(bitmap: Bitmap): String {
        if (!isInitialized) return ""

        // 🔥 تطبيق Preprocessing قبل الـ OCR
        val processedBitmap = preprocessForOCR(bitmap)

        tessApi.setImage(processedBitmap)
        val text = tessApi.utF8Text
        tessApi.clear()

        return text?.trim() ?: ""
    }

    // 🔥 دالة جديدة لتحسين جودة الصورة قبل OCR
    private fun preprocessForOCR(bitmap: Bitmap): Bitmap {
        // 1. تكبير الصورة - Tesseract بيشتغل أحسن على صور أكبر
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 1200, 400, true)

        // 2. تحويل لـ Grayscale + رفع الـ Contrast بشكل قوي
        val result = Bitmap.createBitmap(scaledBitmap.width, scaledBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()

        // Grayscale أولاً
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)

        // رفع الـ Contrast بشكل قوي عشان الحروف تبقى واضحة
        val scale = 2.5f
        val translate = -120f
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        colorMatrix.postConcat(contrastMatrix)

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)

        Log.d("OCR_DEBUG", "🖼️ Image preprocessed: ${result.width}x${result.height}")
        return result
    }

    fun stop() {
        tessApi.stop()
        try {
            tessApi.recycle()
        } catch (e: Exception) {}
    }

    private fun copyFileFromAssets(assetPath: String, destFile: File) {
        if (destFile.exists()) return
        try {
            context.assets.open(assetPath).use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: IOException) {
            Log.e("OCR_DEBUG", "❌ Failed copy: $assetPath (Check Assets Folder!)", e)
        }
    }
}
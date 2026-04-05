package com.depi.graduationproject.data.mlkit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import com.depi.graduationproject.data.model.PlateAnalysisResult
import com.depi.graduationproject.util.TesseractManager
import com.depi.graduationproject.util.YoloDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LicensePlateAnalyzer(private val context: Context) {

    private lateinit var detector: YoloDetector
    private val tesseract = TesseractManager(context)

    fun initialize() {
        try {
            detector = YoloDetector(context, "best.tflite")

            if (tesseract.init()) {
                Log.d("Analyzer", "YOLO + Tesseract Ready ✅")
            } else {
                Log.e("Analyzer", "Tesseract Failed ❌")
            }

        } catch (e: Exception) {
            Log.e("Analyzer", "Error loading models", e)
        }
    }

    fun isInitialized(): Boolean = ::detector.isInitialized

    suspend fun analyze(originalBitmap: Bitmap): PlateAnalysisResult =
        withContext(Dispatchers.Default) {

            if (!isInitialized()) {
                return@withContext PlateAnalysisResult(
                    false,
                    "",
                    null,
                    "Models not ready"
                )
            }

            try {
                // 1️⃣ YOLO Detection
                var results = detector.detect(originalBitmap)
                var workingBitmap = originalBitmap

                // Rotate if needed
                if (results.isEmpty()) {
                    val matrix = Matrix()
                    matrix.postRotate(90f)
                    val rotatedBitmap = Bitmap.createBitmap(
                        originalBitmap, 0, 0,
                        originalBitmap.width, originalBitmap.height,
                        matrix, true
                    )
                    results = detector.detect(rotatedBitmap)
                    if (results.isNotEmpty()) workingBitmap = rotatedBitmap
                }

                if (results.isEmpty()) {
                    return@withContext PlateAnalysisResult(
                        false, "", workingBitmap, "No Plate Found"
                    )
                }

                // 2️⃣ Crop with Padding
                val plateBox = results[0].boundingBox
                val paddedBitmap = cropWithPadding(workingBitmap, plateBox, 20)

                // 3️⃣ Resize ثابت - الـ TesseractManager هيكبره داخلياً
                val resizedBitmap = Bitmap.createScaledBitmap(paddedBitmap, 600, 200, true)

                // 4️⃣ OCR
                val rawText = tesseract.recognize(resizedBitmap)
                Log.d("OCR_TESS", "Raw Output: '$rawText'")

                // 5️⃣ تنظيف النص
                val finalText = filterText(rawText)
                Log.d("OCR_TESS", "Filtered Output: '$finalText'")

                return@withContext if (finalText.isNotBlank()) {
                    PlateAnalysisResult(true, finalText, resizedBitmap, "Read: $finalText")
                } else {
                    PlateAnalysisResult(false, rawText, resizedBitmap, "Text Unclear")
                }

            } catch (e: Exception) {
                Log.e("Analyzer", "Error", e)
                return@withContext PlateAnalysisResult(
                    false, "", null, "Error: ${e.message}"
                )
            }
        }

    private fun cropWithPadding(source: Bitmap, rectF: RectF, padding: Int): Bitmap {
        val left = (rectF.left - padding).toInt().coerceAtLeast(0)
        val top = (rectF.top - padding).toInt().coerceAtLeast(0)
        val right = (rectF.right + padding).toInt().coerceAtMost(source.width)
        val bottom = (rectF.bottom + padding).toInt().coerceAtMost(source.height)

        val width = right - left
        val height = bottom - top

        return Bitmap.createBitmap(source, left, top, width, height)
    }

    // 🔥 filterText محسّنة - تفصل الأرقام عن الحروف بشكل صح
    private fun filterText(text: String): String {
        val digits = StringBuilder()
        val letters = StringBuilder()

        for (char in text) {
            when {
                // أرقام إنجليزية
                char.isDigit() -> digits.append(char)

                // أرقام عربية - تحويل لإنجليزية
                char == '٠' -> digits.append('0')
                char == '١' -> digits.append('1')
                char == '٢' -> digits.append('2')
                char == '٣' -> digits.append('3')
                char == '٤' -> digits.append('4')
                char == '٥' -> digits.append('5')
                char == '٦' -> digits.append('6')
                char == '٧' -> digits.append('7')
                char == '٨' -> digits.append('8')
                char == '٩' -> digits.append('9')

                // حروف اللوحة العربية الصحيحة فقط
                isValidArabicPlateChar(char) -> letters.append(char).append(" ")
            }
        }

        val lettersStr = letters.toString().trim()
        val digitsStr = digits.toString().trim()

        // اللوحة المصرية: حروف على اليمين + أرقام على اليسار
        // مثال: "ص و ن 9432"
        val result = buildString {
            if (lettersStr.isNotEmpty()) append(lettersStr)
            if (lettersStr.isNotEmpty() && digitsStr.isNotEmpty()) append(" ")
            if (digitsStr.isNotEmpty()) append(digitsStr)
        }

        return result.trim().replace("  ", " ")
    }

    private fun isValidArabicPlateChar(c: Char): Boolean {
        return "أبجدرسصطعفقكلمنهوي".contains(c)
    }

    fun close() {
        tesseract.stop()
    }
}
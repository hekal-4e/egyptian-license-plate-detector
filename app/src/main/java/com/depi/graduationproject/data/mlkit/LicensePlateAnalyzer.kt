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

    // ═══════════════════════════════════════════════════════════════
    // Arabic Letter Correction Map
    // Tesseract commonly confuses these visually similar characters
    // ═══════════════════════════════════════════════════════════════
    private val arabicCorrectionMap = mapOf(
        'ا' to 'أ',  // Alef without hamza → with hamza
        'ى' to 'ي',  // Alef maqsura → Ya
        'ة' to 'ه',  // Ta marbuta → Ha
        'ت' to 'ب',  // Ta → Ba (common misread on plates)
        'ث' to 'ب',  // Tha → Ba
        'ذ' to 'د',  // Thal → Dal
        'ز' to 'ر',  // Zay → Ra
        'ش' to 'س',  // Shin → Sin
        'ض' to 'ص',  // Dad → Sad
        'ظ' to 'ط',  // Dha → Ta
        'غ' to 'ع',  // Ghain → Ain
        'ح' to 'ج',  // Ha → Jim
        'خ' to 'ج',  // Kha → Jim
        'ئ' to 'ي',  // Ya with hamza → Ya
        'ؤ' to 'و',  // Waw with hamza → Waw
        'إ' to 'أ',  // Alef with hamza below → with hamza above
        'آ' to 'أ',  // Alef madda → Alef hamza
    )

    private val validPlateLetters = "أبجدرسصطعفقكلمنهوي"

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
                        false, "", workingBitmap, "لم يتم العثور على لوحة (حاول الاقتراب)"
                    )
                }

                // 2️⃣ Crop with Padding
                val plateBox = results[0].boundingBox
                val paddedBitmap = cropWithPadding(workingBitmap, plateBox, 20)

                // Keep a display-sized bitmap for the bottom sheet UI
                val displayBitmap = Bitmap.createScaledBitmap(paddedBitmap, 600, 200, true)

                // 3️⃣ Crop bottom portion only — Egyptian plates have 2 rows:
                //    Top row: "EGYPT مصر" (header — not needed)
                //    Bottom row: "ص و ن ٩٤٣٥" (actual plate text)
                //    PSM 7 needs exactly ONE line, so we isolate the bottom row.
                val bottomCrop = cropBottomPortion(paddedBitmap, 0.45f)
                Log.d("OCR_TESS", "Plate: ${paddedBitmap.width}x${paddedBitmap.height} → Bottom: ${bottomCrop.width}x${bottomCrop.height}")

                // 4️⃣ OCR — send bottom crop to TesseractManager
                val rawText = tesseract.recognize(bottomCrop)
                Log.d("OCR_TESS", "Raw Output (bottom): '$rawText'")

                // 4b️⃣ Fallback: if bottom crop returned nothing, try full plate
                val effectiveRaw = if (rawText.isBlank()) {
                    Log.d("OCR_TESS", "⚠️ Bottom crop empty, trying full plate...")
                    tesseract.recognize(paddedBitmap)
                } else {
                    rawText
                }
                Log.d("OCR_TESS", "Effective Raw: '$effectiveRaw'")

                // 5️⃣ Clean and correct the text
                val finalText = filterText(effectiveRaw)
                Log.d("OCR_TESS", "Filtered Output: '$finalText'")

                return@withContext if (finalText.isNotBlank()) {
                    PlateAnalysisResult(true, finalText, displayBitmap, "Read: $finalText")
                } else {
                    PlateAnalysisResult(false, effectiveRaw, displayBitmap, "Text Unclear")
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

    /**
     * Crops only the bottom portion of the plate image.
     * Egyptian plates have "EGYPT مصر" on top — we skip that header.
     * @param skipTopRatio 0.45 = skip top 45%, keep bottom 55%
     */
    private fun cropBottomPortion(bitmap: Bitmap, skipTopRatio: Float): Bitmap {
        val skipPixels = (bitmap.height * skipTopRatio).toInt().coerceAtMost(bitmap.height - 1)
        val cropHeight = bitmap.height - skipPixels
        return Bitmap.createBitmap(bitmap, 0, skipPixels, bitmap.width, cropHeight)
    }

    private fun correctArabicChar(c: Char): Char {
        // If it's already a valid plate letter, keep it
        if (validPlateLetters.contains(c)) return c
        // Try correction map
        return arabicCorrectionMap[c] ?: c
    }

    /**
     * Improved filterText:
     * 1. Corrects common Arabic OCR mistakes via correction map
     * 2. Converts Arabic digits (٠-٩) → English digits (0-9)
     * 3. Separates letters from digits
     * 4. Produces clean "letters digits" format for Egyptian plates
     */
    private fun filterText(text: String): String {
        val digits = StringBuilder()
        val letters = StringBuilder()

        for (char in text) {
            when {
                // English digits
                char.isDigit() -> digits.append(char)

                // Arabic digits → English
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

                // Arabic letters — apply correction first, then validate
                char.code in 0x0600..0x06FF -> {
                    val corrected = correctArabicChar(char)
                    if (validPlateLetters.contains(corrected)) {
                        letters.append(corrected)
                        letters.append(" ")
                    }
                }
            }
        }

        val lettersStr = letters.toString().trim()
        val digitsStr = digits.toString().trim()

        // Egyptian plate format: letters on right + digits on left
        // Example: "ص و ن 9432"
        return buildString {
            if (lettersStr.isNotEmpty()) append(lettersStr)
            if (lettersStr.isNotEmpty() && digitsStr.isNotEmpty()) append(" ")
            if (digitsStr.isNotEmpty()) append(digitsStr)
        }.trim().replace("  ", " ")
    }

    private fun isValidArabicPlateChar(c: Char): Boolean {
        return validPlateLetters.contains(c)
    }

    fun close() {
        tesseract.stop()
    }
}
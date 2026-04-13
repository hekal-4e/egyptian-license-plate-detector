package com.depi.graduationproject.data.mlkit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import com.depi.graduationproject.data.model.PlateAnalysisResult
import com.depi.graduationproject.util.CharacterDetector
import com.depi.graduationproject.util.YoloDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LicensePlateAnalyzer(private val context: Context) : IPlateAnalyzer {

    private lateinit var plateDetector: YoloDetector
    private lateinit var characterDetector: CharacterDetector

    override fun initialize() {
        try {
            plateDetector = YoloDetector(context, "best.tflite")
            characterDetector = CharacterDetector(context, "yolo11m_car_plate_ocr_int8.tflite")
            Log.d(TAG, "Dual-YOLO pipeline ready")
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to initialize Dual-YOLO pipeline", exception)
        }
    }

    override fun isInitialized(): Boolean =
        ::plateDetector.isInitialized &&
            plateDetector.isInitialized &&
            ::characterDetector.isInitialized

    override suspend fun analyze(originalBitmap: Bitmap): PlateAnalysisResult =
        withContext(Dispatchers.Default) {
            if (!isInitialized()) {
                return@withContext PlateAnalysisResult(
                    isSuccess = false,
                    text = "",
                    bitmap = null,
                    message = "Models not ready",
                    confidence = 0f
                )
            }

            try {
                val detection = detectPlateWithRotationFallback(originalBitmap)
                if (detection == null) {
                    return@withContext PlateAnalysisResult(
                        isSuccess = false,
                        text = "",
                        bitmap = originalBitmap,
                        message = "لم يتم العثور على لوحة (حاول الاقتراب)",
                        confidence = 0f
                    )
                }

                val (frameBitmap, plateBoundingBox) = detection
                val plateBitmap = cropWithPadding(frameBitmap, plateBoundingBox, 20)
                val displayBitmap = createDisplayBitmap(plateBitmap)

                val characterDetections = characterDetector.detect(plateBitmap)
                val reconstructedText = characterDetector.reconstructPlateText(characterDetections)
                val averageConfidence = characterDetections
                    .map { it.confidence }
                    .average()
                    .toFloat()
                    .takeIf { !it.isNaN() }
                    ?: 0f

                val isSuccess = reconstructedText.isNotBlank() && averageConfidence >= MIN_PLATE_CONFIDENCE

                return@withContext PlateAnalysisResult(
                    isSuccess = isSuccess,
                    text = reconstructedText,
                    bitmap = displayBitmap,
                    message = if (isSuccess) "Read: $reconstructedText" else "Characters unclear",
                    confidence = averageConfidence
                )
            } catch (exception: Exception) {
                Log.e(TAG, "Plate analysis failed", exception)
                return@withContext PlateAnalysisResult(
                    isSuccess = false,
                    text = "",
                    bitmap = null,
                    message = "Error: ${exception.message}",
                    confidence = 0f
                )
            }
        }

    private fun detectPlateWithRotationFallback(bitmap: Bitmap): Pair<Bitmap, RectF>? {
        val directDetection = plateDetector.detect(bitmap).firstOrNull()
        if (directDetection != null) {
            return bitmap to directDetection.boundingBox
        }

        val rotatedBitmap = rotateBitmap(bitmap, 90f)
        val rotatedDetection = plateDetector.detect(rotatedBitmap).firstOrNull()
        if (rotatedDetection != null) {
            return rotatedBitmap to rotatedDetection.boundingBox
        }

        return null
    }

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun cropWithPadding(source: Bitmap, rectF: RectF, padding: Int): Bitmap {
        val left = (rectF.left - padding).toInt().coerceAtLeast(0)
        val top = (rectF.top - padding).toInt().coerceAtLeast(0)
        val right = (rectF.right + padding).toInt().coerceAtMost(source.width)
        val bottom = (rectF.bottom + padding).toInt().coerceAtMost(source.height)

        val width = (right - left).coerceAtLeast(1)
        val height = (bottom - top).coerceAtLeast(1)

        return Bitmap.createBitmap(source, left, top, width, height)
    }

    override fun close() {
        if (::plateDetector.isInitialized) {
            plateDetector.close()
        }
        if (::characterDetector.isInitialized) {
            characterDetector.close()
        }
    }

    private fun createDisplayBitmap(plateBitmap: Bitmap): Bitmap {
        if (plateBitmap.width <= DISPLAY_WIDTH_PX) return plateBitmap
        val scale = DISPLAY_WIDTH_PX.toFloat() / plateBitmap.width.toFloat()
        val targetHeight = (plateBitmap.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(plateBitmap, DISPLAY_WIDTH_PX, targetHeight, true)
    }

    companion object {
        private const val TAG = "LicensePlateAnalyzer"
        private const val MIN_PLATE_CONFIDENCE = 0.40f
        private const val DISPLAY_WIDTH_PX = 600
    }
}
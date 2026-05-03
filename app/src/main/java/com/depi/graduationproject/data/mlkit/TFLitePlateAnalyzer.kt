package com.depi.graduationproject.data.mlkit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import com.depi.graduationproject.domain.analyzer.IPlateAnalyzer
import com.depi.graduationproject.domain.model.PlateAnalysisResult
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TFLitePlateAnalyzer(private val context: Context) : IPlateAnalyzer {

    private lateinit var plateDetector: YoloDetector
    private lateinit var plateReader: CrnnPlateReader

    override val ocrModelName: String = "CRNN"

    override fun initialize() {
        try {
            plateDetector = YoloDetector(context, "best.tflite")
            plateReader = CrnnPlateReader(context, "plate_ocr_v3_fp16.tflite")
            Log.d(TAG, "YOLO+CRNN pipeline ready")
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to initialize YOLO+CRNN pipeline", exception)
        }
    }

    override fun isInitialized(): Boolean =
        ::plateDetector.isInitialized &&
            plateDetector.isInitialized &&
            ::plateReader.isInitialized

    override suspend fun analyze(imageData: ByteArray): PlateAnalysisResult =
        withContext(Dispatchers.Default) {
            val originalBitmap = android.graphics.BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            if (originalBitmap == null) {
                return@withContext PlateAnalysisResult(
                    isSuccess = false,
                    text = "",
                    imageBytes = null,
                    message = "Invalid image data format",
                    confidence = 0f
                )
            }

            if (!isInitialized()) {
                return@withContext PlateAnalysisResult(
                    isSuccess = false,
                    text = "",
                    imageBytes = null,
                    message = "Models not ready",
                    confidence = 0f
                )
            }

            val rotatedBitmapRef = arrayOf<Bitmap?>(null)
            try {
                val detection = detectPlateWithRotationFallback(originalBitmap, rotatedBitmapRef)
                if (detection == null) {
                    return@withContext PlateAnalysisResult(
                        isSuccess = false,
                        text = "",
                        imageBytes = imageData,
                        message = "لم يتم العثور على لوحة (حاول الاقتراب)",
                        confidence = 0f
                    )
                }

                val (frameBitmap, plateBoundingBox) = detection
                val plateBitmap = cropWithPadding(frameBitmap, plateBoundingBox, 20)
                val displayBitmap = createDisplayBitmap(plateBitmap)

                val readResult = plateReader.read(plateBitmap)
                val reconstructedText = readResult.text
                val averageConfidence = readResult.confidence

                // Recycle the cropped bitmap after use
                if (plateBitmap !== frameBitmap && plateBitmap !== originalBitmap) {
                    plateBitmap.recycle()
                }

                val outStream = java.io.ByteArrayOutputStream()
                displayBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                val imageBytes = outStream.toByteArray()

                val isSuccess = reconstructedText.isNotBlank() && averageConfidence >= MIN_PLATE_CONFIDENCE

                return@withContext PlateAnalysisResult(
                    isSuccess = isSuccess,
                    text = reconstructedText,
                    imageBytes = imageBytes,
                    message = if (isSuccess) "Read: $reconstructedText" else "Characters unclear",
                    confidence = averageConfidence
                )
            } catch (exception: Exception) {
                Log.e(TAG, "Plate analysis failed", exception)
                return@withContext PlateAnalysisResult(
                    isSuccess = false,
                    text = "",
                    imageBytes = null,
                    message = "Error: ${exception.message}",
                    confidence = 0f
                )
            } finally {
                // Recycle rotated bitmap if it was created
                rotatedBitmapRef[0]?.recycle()
            }
        }

    private fun detectPlateWithRotationFallback(bitmap: Bitmap, rotatedBitmapRef: Array<Bitmap?>): Pair<Bitmap, RectF>? {
        val directDetection = plateDetector.detect(bitmap).firstOrNull()
        if (directDetection != null) {
            return bitmap to directDetection.boundingBox
        }

        val rotatedBitmap = rotateBitmap(bitmap, 90f)
        rotatedBitmapRef[0] = rotatedBitmap
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
        if (::plateReader.isInitialized) {
            plateReader.close()
        }
    }

    private fun createDisplayBitmap(plateBitmap: Bitmap): Bitmap {
        if (plateBitmap.width <= DISPLAY_WIDTH_PX) return plateBitmap
        val scale = DISPLAY_WIDTH_PX.toFloat() / plateBitmap.width.toFloat()
        val targetHeight = (plateBitmap.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(plateBitmap, DISPLAY_WIDTH_PX, targetHeight, true)
    }

    companion object {
        private const val TAG = "TFLitePlateAnalyzer"
        private const val MIN_PLATE_CONFIDENCE = 0.40f
        private const val DISPLAY_WIDTH_PX = 600
    }
}

// Internal class to handle OCR using CRNN
internal class CrnnPlateReader(
    context: Context,
    modelPath: String = "plate_ocr_v3_fp16.tflite"
) {
    private val interpreter: Interpreter
    private val inputWidth = 200
    private val inputHeight = 64

    // Must match FULL_CHARSET from training
    private val charset = listOf(
        "أ", "ب", "ج", "د", "ر", "س", "ص", "ط", "ع", "ف",
        "ق", "ل", "م", "ن", "هـ", "و", "ي",
        "٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩"
    )
    private val blankIndex = 0

    private val validPlateLetters = "أبجدرسصطعفقلمنهـوي"

    data class PlateReadResult(
        val text: String,
        val confidence: Float
    )

    init {
        val modelFile = FileUtil.loadMappedFile(context, modelPath)
        val options = Interpreter.Options().apply { setNumThreads(4) }
        interpreter = Interpreter(modelFile, options)
    }

    fun read(plateBitmap: Bitmap): PlateReadResult {
        val inputBuffer = preprocessBitmap(plateBitmap)
        val outputShape = interpreter.getOutputTensor(0).shape()
        val T = outputShape[1]
        val C = outputShape[2]
        val outputBuffer = Array(1) { Array(T) { FloatArray(C) } }
        interpreter.run(inputBuffer, outputBuffer)

        val logits = outputBuffer[0]
        val decoded = greedyCtcDecode(logits)
        val confidence = computeConfidence(logits)

        val (validatedText, isValid) = validateEgyptianPlate(decoded)
        val finalConfidence = if (isValid) confidence else confidence * 0.8f

        return PlateReadResult(
            text = validatedText,
            confidence = finalConfidence
        )
    }

    fun close() {
        interpreter.close()
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
        val pixelCount = inputWidth * inputHeight
        val pixels = IntArray(pixelCount)
        resized.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        if (resized !== bitmap) resized.recycle()

        val buffer = ByteBuffer.allocateDirect(pixelCount * 3 * 4)
            .order(ByteOrder.nativeOrder())

        for (pixel in pixels) {
            buffer.putFloat((pixel shr 16 and 0xFF) / 255f)
            buffer.putFloat((pixel shr 8 and 0xFF) / 255f)
            buffer.putFloat((pixel and 0xFF) / 255f)
        }
        buffer.rewind()
        return buffer
    }

    private fun greedyCtcDecode(logits: Array<FloatArray>): String {
        val result = StringBuilder()
        var prevIndex = -1

        for (timestep in logits) {
            val maxIndex = timestep.indices.maxByOrNull { timestep[it] } ?: continue
            if (maxIndex != blankIndex && maxIndex != prevIndex) {
                val charIdx = maxIndex - 1
                if (charIdx in charset.indices) {
                    result.append(charset[charIdx])
                }
            }
            prevIndex = maxIndex
        }
        return result.toString()
    }

    private fun computeConfidence(logits: Array<FloatArray>): Float {
        var sumConf = 0f
        var count = 0
        var prevIndex = -1

        for (timestep in logits) {
            val maxIndex = timestep.indices.maxByOrNull { timestep[it] } ?: continue
            if (maxIndex != blankIndex && maxIndex != prevIndex) {
                val charIdx = maxIndex - 1
                if (charIdx in charset.indices) {
                    val maxVal = timestep.max()
                    val expSum = timestep.sumOf { Math.exp((it - maxVal).toDouble()) }
                    val prob = Math.exp((timestep[maxIndex] - maxVal).toDouble()) / expSum
                    sumConf += prob.toFloat()
                    count++
                }
            }
            prevIndex = maxIndex
        }
        return if (count > 0) sumConf / count else 0f
    }

    private fun validateEgyptianPlate(raw: String): Pair<String, Boolean> {
        val letters = raw.filter { validPlateLetters.contains(it) }
        val digits = raw.filter { it in '\u0660'..'\u0669' }

        val isValid = letters.length in 2..3 && digits.length in 3..4

        val orderedText = buildString {
            for (char in raw) {
                if (validPlateLetters.contains(char) || char in '\u0660'..'\u0669') {
                    append(char)
                }
            }
        }

        return if (isValid) {
            "$letters $digits" to true
        } else {
            orderedText to false
        }
    }
}

package com.depi.graduationproject.data.mlkit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import com.depi.graduationproject.domain.analyzer.IPlateAnalyzer
import com.depi.graduationproject.domain.model.ImageFrame
import com.depi.graduationproject.domain.model.PixelFormat
import com.depi.graduationproject.domain.model.PlateAnalysisResult
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.max
import kotlin.math.min

class TFLitePlateAnalyzer(private val context: Context) : IPlateAnalyzer {

    private lateinit var plateDetector: YoloDetector
    private lateinit var plateReader: CrnnPlateReader
    private val inferenceMutex = Mutex()

    override val ocrModelName: String = "CRNN"

    override fun initialize() {
        if (isInitialized()) return
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

    override suspend fun analyze(imageFrame: ImageFrame): PlateAnalysisResult =
        withContext(Dispatchers.Default) {
            inferenceMutex.withLock {
                if (!isInitialized()) {
                    return@withLock PlateAnalysisResult(
                        isSuccess = false,
                        text = "",
                        imageBytes = null,
                        message = "Models not ready",
                        confidence = 0f
                    )
                }

                val originalBitmap = try {
                    when (imageFrame.format) {
                        PixelFormat.ARGB_8888 -> {
                            val bitmap = Bitmap.createBitmap(
                                imageFrame.width,
                                imageFrame.height,
                                Bitmap.Config.ARGB_8888
                            )
                            val buffer = ByteBuffer.wrap(imageFrame.bytes)
                            bitmap.copyPixelsFromBuffer(buffer)
                            bitmap
                        }
                    }
                } catch (exception: Exception) {
                    return@withLock PlateAnalysisResult(
                        isSuccess = false,
                        text = "",
                        imageBytes = null,
                        message = "Invalid image data format",
                        confidence = 0f
                    )
                }

                val rotatedBitmapRef = arrayOf<Bitmap?>(null)
                try {
                    val detection = detectPlateWithRotationFallback(originalBitmap, rotatedBitmapRef)
                    if (detection == null) {
                        return@withLock PlateAnalysisResult(
                            isSuccess = false,
                            text = "",
                            imageBytes = imageFrame.bytes,
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

                    val outStream = java.io.ByteArrayOutputStream()
                    displayBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                    val imageBytes = outStream.toByteArray()

                    if (displayBitmap !== plateBitmap) {
                        displayBitmap.recycle()
                    }

                    if (plateBitmap !== frameBitmap && plateBitmap !== originalBitmap) {
                        plateBitmap.recycle()
                    }

                    val isSuccess = reconstructedText.isNotBlank() && averageConfidence >= MIN_PLATE_CONFIDENCE

                    return@withLock PlateAnalysisResult(
                        isSuccess = isSuccess,
                        text = reconstructedText,
                        imageBytes = imageBytes,
                        message = if (isSuccess) "Read: $reconstructedText" else "Characters unclear",
                        confidence = averageConfidence
                    )
                } catch (exception: Exception) {
                    Log.e(TAG, "Plate analysis failed", exception)
                    return@withLock PlateAnalysisResult(
                        isSuccess = false,
                        text = "",
                        imageBytes = null,
                        message = "Error: ${exception.message}",
                        confidence = 0f
                    )
                } finally {
                    rotatedBitmapRef[0]?.recycle()
                    if (!originalBitmap.isRecycled) {
                        originalBitmap.recycle()
                    }
                }
            }
        }

    private fun detectPlateWithRotationFallback(bitmap: Bitmap, rotatedBitmapRef: Array<Bitmap?>): Pair<Bitmap, RectF>? {
        val directDetection = plateDetector.detect(bitmap).firstOrNull()
        if (directDetection != null || !ENABLE_ROTATION_FALLBACK) {
            return directDetection?.let { bitmap to it.boundingBox }
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
        private const val ENABLE_ROTATION_FALLBACK = true
    }
}

// Internal class to handle OCR using CRNN
internal class CrnnPlateReader(
    context: Context,
    modelPath: String = "plate_ocr_v3_fp16.tflite"
) {
    private val interpreter: Interpreter
    private val inputWidth: Int
    private val inputHeight: Int
    private val inputChannels: Int
    private val classCount: Int

    private enum class ColorOrder { RGB, BGR }
    private enum class Normalization { ZERO_TO_ONE, NEG_ONE_TO_ONE }
    private enum class ResizeMode { STRETCH, LETTERBOX }

    private data class InputConfig(
        val colorOrder: ColorOrder,
        val normalization: Normalization,
        val resizeMode: ResizeMode
    )

    // Must match FULL_CHARSET from training
    private val charset = listOf(
        "أ", "ب", "ج", "د", "ر", "س", "ص", "ط", "ع", "ف",
        "ق", "ك", "ل", "م", "ن", "ه", "و", "ي",
        "٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩"
    )
    private val blankIndex = 0

    private val validPlateLetters = "أبجدرسصطعفقكلمنهوي"

    data class PlateReadResult(
        val text: String,
        val confidence: Float
    )

    init {
        val modelFile = FileUtil.loadMappedFile(context, modelPath)
        val options = Interpreter.Options().apply { setNumThreads(4) }
        interpreter = Interpreter(modelFile, options)

        val inputShape = interpreter.getInputTensor(0).shape()
        require(inputShape.size == 4) { "CRNN input must be rank-4, got ${inputShape.contentToString()}" }
        inputHeight = inputShape[1]
        inputWidth = inputShape[2]
        inputChannels = inputShape[3]
        require(inputChannels == 3) { "CRNN input must have 3 channels, got ${inputShape.contentToString()}" }
        classCount = charset.size + 1

        Log.i(
            TAG,
            "CRNN loaded. input=${inputShape.contentToString()} " +
                "output=${interpreter.getOutputTensor(0).shape().contentToString()} " +
                "inputType=${interpreter.getInputTensor(0).dataType()} " +
                "outputType=${interpreter.getOutputTensor(0).dataType()} " +
                "classes=$classCount"
        )
    }

    fun read(plateBitmap: Bitmap): PlateReadResult {
        val inputBuffer = preprocessBitmap(plateBitmap, DEFAULT_INPUT_CONFIG)
        val logits = runModel(inputBuffer)
        val decoded = greedyCtcDecode(logits)
        val confidence = computeConfidence(logits)

        if (DEBUG_OCR) {
            Log.d(TAG, "CTC raw='$decoded' confidence=$confidence path=${summarizeBestPath(logits)}")
            runPreprocessProbe(plateBitmap)
        }

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

    private fun runModel(inputBuffer: ByteBuffer): Array<FloatArray> {
        val outputShape = interpreter.getOutputTensor(0).shape()
        require(outputShape.size == 3 && outputShape[0] == 1) {
            "CRNN output must be [1,T,C] or [1,C,T], got ${outputShape.contentToString()}"
        }

        val dim1 = outputShape[1]
        val dim2 = outputShape[2]
        val rawOutput = Array(1) { Array(dim1) { FloatArray(dim2) } }
        interpreter.run(inputBuffer, rawOutput)
        return toTimeMajorLogits(rawOutput[0], outputShape)
    }

    private fun toTimeMajorLogits(raw: Array<FloatArray>, shape: IntArray): Array<FloatArray> {
        val dim1 = shape[1]
        val dim2 = shape[2]
        return when {
            dim2 == classCount -> raw
            dim1 == classCount -> Array(dim2) { timestep ->
                FloatArray(dim1) { classIndex -> raw[classIndex][timestep] }
            }
            else -> error(
                "Cannot infer CTC output layout. Expected one dimension to equal classCount=$classCount, " +
                    "got ${shape.contentToString()}"
            )
        }
    }

    private fun preprocessBitmap(bitmap: Bitmap, config: InputConfig): ByteBuffer {
        val resized = when (config.resizeMode) {
            ResizeMode.STRETCH -> Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
            ResizeMode.LETTERBOX -> letterboxBitmap(bitmap, inputWidth, inputHeight)
        }
        val pixelCount = inputWidth * inputHeight
        val pixels = IntArray(pixelCount)
        resized.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        if (resized !== bitmap) resized.recycle()

        val buffer = ByteBuffer.allocateDirect(pixelCount * 3 * 4)
            .order(ByteOrder.nativeOrder())

        for (pixel in pixels) {
            val r = pixel shr 16 and 0xFF
            val g = pixel shr 8 and 0xFF
            val b = pixel and 0xFF

            val first = if (config.colorOrder == ColorOrder.RGB) r else b
            val third = if (config.colorOrder == ColorOrder.RGB) b else r
            buffer.putFloat(normalize(first, config.normalization))
            buffer.putFloat(normalize(g, config.normalization))
            buffer.putFloat(normalize(third, config.normalization))
        }
        buffer.rewind()
        return buffer
    }

    private fun normalize(channel: Int, normalization: Normalization): Float {
        val zeroToOne = channel / 255f
        return when (normalization) {
            Normalization.ZERO_TO_ONE -> zeroToOne
            Normalization.NEG_ONE_TO_ONE -> zeroToOne * 2f - 1f
        }
    }

    private fun letterboxBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE)

        val scale = min(
            targetWidth.toFloat() / bitmap.width.toFloat(),
            targetHeight.toFloat() / bitmap.height.toFloat()
        )
        val scaledWidth = max(1, (bitmap.width * scale).toInt())
        val scaledHeight = max(1, (bitmap.height * scale).toInt())
        val left = (targetWidth - scaledWidth) / 2
        val top = (targetHeight - scaledHeight) / 2

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(
            bitmap,
            Rect(0, 0, bitmap.width, bitmap.height),
            Rect(left, top, left + scaledWidth, top + scaledHeight),
            paint
        )
        return result
    }

    private fun runPreprocessProbe(bitmap: Bitmap) {
        val configs = listOf(
            DEFAULT_INPUT_CONFIG,
            InputConfig(ColorOrder.BGR, Normalization.ZERO_TO_ONE, ResizeMode.STRETCH),
            InputConfig(ColorOrder.RGB, Normalization.NEG_ONE_TO_ONE, ResizeMode.STRETCH),
            InputConfig(ColorOrder.RGB, Normalization.ZERO_TO_ONE, ResizeMode.LETTERBOX)
        )

        configs.forEach { config ->
            val logits = runModel(preprocessBitmap(bitmap, config))
            Log.d(
                TAG,
                "Probe $config -> '${greedyCtcDecode(logits)}' " +
                    "confidence=${computeConfidence(logits)} path=${summarizeBestPath(logits)}"
            )
        }
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

    private fun summarizeBestPath(logits: Array<FloatArray>): String {
        val compact = mutableListOf<Int>()
        var prev = -1
        logits.forEach { timestep ->
            val maxIndex = timestep.indices.maxByOrNull { timestep[it] } ?: return@forEach
            if (maxIndex != prev) compact.add(maxIndex)
            prev = maxIndex
        }
        return compact.take(80).joinToString(",")
    }

    private fun validateEgyptianPlate(raw: String): Pair<String, Boolean> {
        val letters = raw.filter { validPlateLetters.contains(it) }
        val digits = raw.filter { it in '\u0660'..'\u0669' }

        val isValid = letters.length in 2..3 && digits.length in 3..4
        if (isValid) {
            val lastLetterIdx = raw.indexOfLast { validPlateLetters.contains(it) }
            val firstDigitIdx = raw.indexOfFirst { it in '\u0660'..'\u0669' }
            if (firstDigitIdx != -1 && lastLetterIdx > firstDigitIdx) {
                Log.w("CrnnPlateReader", "Suspicious order: letters/digits interleaved in '$raw'")
            }
        }

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

    private companion object {
        private const val TAG = "CrnnPlateReader"
        private const val DEBUG_OCR = false
        private val DEFAULT_INPUT_CONFIG = InputConfig(
            colorOrder = ColorOrder.RGB,
            normalization = Normalization.ZERO_TO_ONE,
            resizeMode = ResizeMode.STRETCH
        )
    }
}

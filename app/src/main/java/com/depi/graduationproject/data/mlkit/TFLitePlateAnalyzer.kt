package com.depi.graduationproject.data.mlkit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import com.depi.graduationproject.BuildConfig
import com.depi.graduationproject.core.utils.PlateUtils
import com.depi.graduationproject.domain.analyzer.IPlateAnalyzer
import com.depi.graduationproject.domain.model.ImageFrame
import com.depi.graduationproject.domain.model.PixelFormat
import com.depi.graduationproject.domain.model.PlateAnalysisResult
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Tensor
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val V4_DIGIT_SLOTS = 4
private const val V4_LETTER_SLOTS = 3
private const val V4_DIGIT_CLASSES = 11
private const val V4_LENGTH_CLASSES = 2
private val V4_LETTER_CLASSES = PlateUtils.ARABIC_LETTERS.size + 1

internal data class TensorDescriptor(
    val index: Int,
    val name: String,
    val shape: IntArray
)

internal data class OutputMapping(
    val digitLogitsIndex: Int,
    val letterLogitsIndex: Int,
    val digitLenIndex: Int,
    val letterLenIndex: Int
)

internal data class OutputMappingResult(
    val mapping: OutputMapping,
    val usedFallback: Boolean
)

internal fun mapV4Outputs(descriptors: List<TensorDescriptor>): OutputMappingResult {
    var digitLogitsIndex: Int? = null
    var letterLogitsIndex: Int? = null
    val lengthCandidates = mutableListOf<TensorDescriptor>()

    descriptors.forEach { descriptor ->
        when {
            descriptor.shape.contentEquals(intArrayOf(1, V4_DIGIT_SLOTS, V4_DIGIT_CLASSES)) ->
                digitLogitsIndex = descriptor.index
            descriptor.shape.contentEquals(intArrayOf(1, V4_LETTER_SLOTS, V4_LETTER_CLASSES)) ->
                letterLogitsIndex = descriptor.index
            descriptor.shape.contentEquals(intArrayOf(1, V4_LENGTH_CLASSES)) ->
                lengthCandidates.add(descriptor)
        }
    }

    if (digitLogitsIndex == null || letterLogitsIndex == null || lengthCandidates.size != 2) {
        val shapes = descriptors.joinToString { "${it.name}=${it.shape.contentToString()}" }
        error("Unable to map V4 outputs. Found: $shapes")
    }

    val digitLenCandidate = lengthCandidates.firstOrNull {
        val name = it.name.lowercase()
        name.contains("digit_len") ||
            name.contains("digitlen") ||
            name.contains("output_2") ||
            name.contains("serving_default_output_2")
    }
    val letterLenCandidate = lengthCandidates.firstOrNull {
        val name = it.name.lowercase()
        name.contains("letter_len") ||
            name.contains("letterlen") ||
            name.contains("output_3") ||
            name.contains("serving_default_output_3")
    }

    require(digitLenCandidate != null && letterLenCandidate != null) {
        "Static INT8 V4 length outputs must be identifiable by name. " +
            "Candidates=${lengthCandidates.joinToString { "${it.index}:${it.name}" }}"
    }
    require(digitLenCandidate.index != letterLenCandidate.index) {
        "Static INT8 V4 length outputs must be distinct. " +
            "Candidates=${lengthCandidates.joinToString { "${it.index}:${it.name}" }}"
    }

    return OutputMappingResult(
        mapping = OutputMapping(
            digitLogitsIndex = digitLogitsIndex!!,
            letterLogitsIndex = letterLogitsIndex!!,
            digitLenIndex = digitLenCandidate.index,
            letterLenIndex = letterLenCandidate.index
        ),
        usedFallback = false
    )
}

class TFLitePlateAnalyzer(private val context: Context) : IPlateAnalyzer {

    private lateinit var plateDetector: YoloDetector
    private lateinit var plateReader: PlateSlotTransformerReader
    private val inferenceMutex = Mutex()

    override val ocrModelName: String = "EALPR V4 Static INT8 PlateSlotTransformer"

    override fun initialize() {
        if (isInitialized()) return
        try {
            plateDetector = YoloDetector(context, "best.tflite")
            plateReader = PlateSlotTransformerReader(context, V4_OCR_MODEL)
            Log.d(TAG, "YOLO+V4 static INT8 pipeline ready")
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to initialize YOLO+V4 static INT8 pipeline", exception)
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

                    if (readResult.isValid && averageConfidence < MIN_PLATE_CONFIDENCE) {
                        Log.w(
                            TAG,
                            "Low confidence read text='$reconstructedText' " +
                                "digits=${readResult.digitLength} letters=${readResult.letterLength} " +
                                "confidence=${String.format(Locale.US, "%.3f", averageConfidence)}"
                        )
                    }

                    val isSuccess = readResult.isValid &&
                        reconstructedText.isNotBlank() &&
                        averageConfidence >= MIN_PLATE_CONFIDENCE
                    val message = when {
                        !readResult.isValid || reconstructedText.isBlank() -> "Characters unclear"
                        averageConfidence < MIN_PLATE_CONFIDENCE -> "Low confidence: $reconstructedText"
                        else -> "Read: $reconstructedText"
                    }

                    return@withLock PlateAnalysisResult(
                        isSuccess = isSuccess,
                        text = reconstructedText,
                        imageBytes = imageBytes,
                        message = message,
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
        private const val V4_OCR_MODEL = "ealpr_v4_static_wi8_ai8_full_integer.tflite"
        private const val MIN_PLATE_CONFIDENCE = 0.60f
        private const val DISPLAY_WIDTH_PX = 600
        private const val ENABLE_ROTATION_FALLBACK = true
    }
}

internal class PlateSlotTransformerReader(
    context: Context,
    modelPath: String = "ealpr_v4_static_wi8_ai8_full_integer.tflite"
) {
    private val interpreter: Interpreter
    private val outputMapping: OutputMapping
    private val decoder = PlateSlotDecoder()
    private val inputQuant: QuantParams
    private val inputByteCount: Int

    data class PlateReadResult(
        val text: String,
        val confidence: Float,
        val digitLength: Int,
        val letterLength: Int,
        val isValid: Boolean
    )

    private data class QuantParams(
        val dataType: DataType,
        val scale: Float,
        val zeroPoint: Int
    )

    init {
        val modelFile = FileUtil.loadMappedFile(context, modelPath)
        val options = Interpreter.Options().apply { setNumThreads(4) }
        interpreter = Interpreter(modelFile, options)

        val inputTensor = interpreter.getInputTensor(0)
        logTensorContract("V4 input[0]", inputTensor)
        requireQuantizedInput(inputTensor)
        inputQuant = inputTensor.toQuantParams()
        inputByteCount = inputTensor.numBytes()

        for (index in 0 until interpreter.outputTensorCount) {
            logTensorContract("V4 output[$index]", interpreter.getOutputTensor(index))
        }

        outputMapping = mapOutputs()
    }

    fun read(plateBitmap: Bitmap): PlateReadResult {
        val inputBuffer = preprocessBitmap(plateBitmap)

        val digitBuffer = allocateOutputBuffer(outputMapping.digitLogitsIndex)
        val letterBuffer = allocateOutputBuffer(outputMapping.letterLogitsIndex)
        val digitLenBuffer = allocateOutputBuffer(outputMapping.digitLenIndex)
        val letterLenBuffer = allocateOutputBuffer(outputMapping.letterLenIndex)

        val outputs = hashMapOf<Int, Any>(
            outputMapping.digitLogitsIndex to digitBuffer,
            outputMapping.letterLogitsIndex to letterBuffer,
            outputMapping.digitLenIndex to digitLenBuffer,
            outputMapping.letterLenIndex to letterLenBuffer
        )

        interpreter.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputs)

        val digitLogits = toSlotLogits(
            dequantizeOutput(outputMapping.digitLogitsIndex, digitBuffer),
            V4_DIGIT_SLOTS,
            V4_DIGIT_CLASSES
        )
        val letterLogits = toSlotLogits(
            dequantizeOutput(outputMapping.letterLogitsIndex, letterBuffer),
            V4_LETTER_SLOTS,
            V4_LETTER_CLASSES
        )
        val digitLenLogits = dequantizeOutput(outputMapping.digitLenIndex, digitLenBuffer)
        val letterLenLogits = dequantizeOutput(outputMapping.letterLenIndex, letterLenBuffer)

        val decodeResult = decoder.decode(
            digitLogits,
            letterLogits,
            digitLenLogits,
            letterLenLogits
        )

        if (shouldLogDiagnostics(decodeResult)) {
            logStats("digitLenLogits", digitLenLogits)
            logStats("letterLenLogits", letterLenLogits)
            digitLogits.forEachIndexed { index, slot ->
                logStats("digitLogits[$index]", slot)
            }
            letterLogits.forEachIndexed { index, slot ->
                logStats("letterLogits[$index]", slot)
            }
            Log.d(
                TAG,
                "V4 decode text='${decodeResult.text}' " +
                    "digits=${decodeResult.digitLength} letters=${decodeResult.letterLength} " +
                    "confidence=${String.format(Locale.US, "%.3f", decodeResult.confidence)} " +
                    "valid=${decodeResult.isValid}"
            )
        }

        return PlateReadResult(
            text = decodeResult.text,
            confidence = decodeResult.confidence,
            digitLength = decodeResult.digitLength,
            letterLength = decodeResult.letterLength,
            isValid = decodeResult.isValid
        )
    }

    fun close() {
        interpreter.close()
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, INPUT_WIDTH, INPUT_HEIGHT, true)
        val pixels = IntArray(INPUT_WIDTH * INPUT_HEIGHT)
        resized.getPixels(pixels, 0, INPUT_WIDTH, 0, 0, INPUT_WIDTH, INPUT_HEIGHT)

        val buffer = ByteBuffer
            .allocateDirect(inputByteCount)
            .order(ByteOrder.nativeOrder())

        for (channel in 0 until INPUT_CHANNELS) {
            for (pixel in pixels) {
                val value = when (channel) {
                    0 -> pixel shr 16 and 0xFF
                    1 -> pixel shr 8 and 0xFF
                    else -> pixel and 0xFF
                }
                val normalized = value / NORMALIZATION_DIVISOR - 1f
                buffer.put(quantizeToByte(normalized, inputQuant))
            }
        }

        buffer.rewind()
        if (resized !== bitmap) {
            resized.recycle()
        }
        return buffer
    }

    private fun quantizeToByte(value: Float, q: QuantParams): Byte {
        val raw = (value / q.scale + q.zeroPoint).roundToInt()
        val clamped = when (q.dataType) {
            DataType.INT8 -> raw.coerceIn(-128, 127)
            DataType.UINT8 -> raw.coerceIn(0, 255)
            else -> error("Unsupported quantized input dtype: ${q.dataType}")
        }
        return clamped.toByte()
    }

    private fun allocateOutputBuffer(index: Int): ByteBuffer {
        val tensor = interpreter.getOutputTensor(index)
        require(tensor.dataType() == DataType.INT8 || tensor.dataType() == DataType.UINT8) {
            "V4 static output[$index] must be INT8/UINT8, got ${tensor.dataType()}"
        }
        val q = tensor.quantizationParams()
        require(q.scale > 0f) {
            "V4 static output[$index] has invalid scale=${q.scale}"
        }
        return ByteBuffer
            .allocateDirect(tensor.numBytes())
            .order(ByteOrder.nativeOrder())
    }

    private fun dequantizeOutput(index: Int, buffer: ByteBuffer): FloatArray {
        val tensor = interpreter.getOutputTensor(index)
        val q = tensor.quantizationParams()
        val dataType = tensor.dataType()
        val count = tensor.shape().fold(1) { acc, dim -> acc * dim }

        buffer.rewind()
        return FloatArray(count) {
            val raw = when (dataType) {
                DataType.INT8 -> buffer.get().toInt()
                DataType.UINT8 -> buffer.get().toInt() and 0xFF
                else -> error("Unsupported quantized output dtype: $dataType")
            }
            (raw - q.zeroPoint) * q.scale
        }
    }

    private fun toSlotLogits(flat: FloatArray, slots: Int, classes: Int): Array<FloatArray> {
        require(flat.size == slots * classes) {
            "Expected ${slots * classes} logits, got ${flat.size}"
        }
        return Array(slots) { slot ->
            FloatArray(classes) { cls ->
                flat[slot * classes + cls]
            }
        }
    }

    private fun requireQuantizedInput(inputTensor: Tensor) {
        val inputShape = inputTensor.shape()
        val inputType = inputTensor.dataType()

        require(
            inputShape.contentEquals(intArrayOf(1, INPUT_CHANNELS, INPUT_HEIGHT, INPUT_WIDTH))
        ) {
            "V4 static INT8 input must be [1,$INPUT_CHANNELS,$INPUT_HEIGHT,$INPUT_WIDTH], " +
                "got ${inputShape.contentToString()}"
        }

        require(inputType == DataType.INT8 || inputType == DataType.UINT8) {
            "V4 static full-integer input must be INT8/UINT8, got $inputType"
        }

        val q = inputTensor.quantizationParams()
        require(q.scale > 0f) {
            "V4 static input must have quantization scale > 0, got ${q.scale}"
        }
    }

    private fun logTensorContract(prefix: String, tensor: Tensor) {
        val q = tensor.quantizationParams()
        Log.i(
            TAG,
            "$prefix name=${tensor.name()} shape=${tensor.shape().contentToString()} " +
                "dtype=${tensor.dataType()} scale=${q.scale} zeroPoint=${q.zeroPoint}"
        )
    }

    private fun Tensor.toQuantParams(): QuantParams {
        val q = quantizationParams()
        return QuantParams(
            dataType = dataType(),
            scale = q.scale,
            zeroPoint = q.zeroPoint
        )
    }

    private fun logStats(name: String, values: FloatArray) {
        if (values.isEmpty()) {
            Log.d(TAG, "$name empty")
            return
        }

        var minValue = values[0]
        var maxValue = values[0]
        var argmax = 0
        var sum = 0.0

        values.forEachIndexed { index, value ->
            if (value < minValue) minValue = value
            if (value > maxValue) {
                maxValue = value
                argmax = index
            }
            sum += value
        }

        val mean = sum / values.size
        Log.d(
            TAG,
            "$name min=$minValue max=$maxValue " +
                "mean=${String.format(Locale.US, "%.6f", mean)} " +
                "argmax=$argmax values=${values.joinToString()}"
        )
    }

    private fun shouldLogDiagnostics(result: PlateSlotDecoder.DecodeResult): Boolean {
        if (!BuildConfig.DEBUG) return false
        return result.text == DEFAULT_LOW_RESULT || result.confidence < DIAGNOSTIC_CONFIDENCE_THRESHOLD
    }

    private fun mapOutputs(): OutputMapping {
        val descriptors = (0 until interpreter.outputTensorCount).map { index ->
            val tensor = interpreter.getOutputTensor(index)
            TensorDescriptor(index, tensor.name(), tensor.shape())
        }

        val result = mapV4Outputs(descriptors)
        if (result.usedFallback) {
            Log.w(
                TAG,
                "Length output names not found; using index order " +
                    "(digitLen=${result.mapping.digitLenIndex}, " +
                    "letterLen=${result.mapping.letterLenIndex})."
            )
        }

        Log.i(
            TAG,
            "V4 output mapping: digit=${result.mapping.digitLogitsIndex}, " +
                "letter=${result.mapping.letterLogitsIndex}, " +
                "digitLen=${result.mapping.digitLenIndex}, " +
                "letterLen=${result.mapping.letterLenIndex}"
        )

        return result.mapping
    }

    companion object {
        private const val TAG = "PlateSlotTransformer"
        private const val INPUT_WIDTH = 320
        private const val INPUT_HEIGHT = 96
        private const val INPUT_CHANNELS = 3
        private const val NORMALIZATION_DIVISOR = 127.5f

        private const val DIAGNOSTIC_CONFIDENCE_THRESHOLD = 0.60f
        private const val DEFAULT_LOW_RESULT = "000أأ"
    }
}

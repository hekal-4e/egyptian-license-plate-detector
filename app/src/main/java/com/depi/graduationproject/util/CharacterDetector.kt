package com.depi.graduationproject.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class CharacterDetector(
    context: Context,
    modelPath: String = "yolo11m_car_plate_ocr_int8.tflite"
) {

    data class CharacterDetection(
        val symbol: String,
        val confidence: Float,
        val xCenter: Float,
        val boundingBox: RectF
    )

    private val interpreter: Interpreter
    private val inputWidth: Int
    private val inputHeight: Int
    private val inputDataType: DataType

    private val classNames = listOf(
        "0", "1", "2", "3", "4", "5", "6", "7", "7aa", "8", "9",
        "Taa", "Thaa", "ain", "alif", "baa", "daad", "daal", "faa", "ghayn",
        "haa", "jeem", "kaaf", "khaa", "laam", "meem", "noon", "qaaf", "raa",
        "saad", "seen", "sheen", "taa", "thaa", "waw", "yaa", "zaal", "zay"
    )

    private val validPlateLetters = "أبجدرسصطعفقكلمنهوي"

    // Only normalize OCR noise into allowed Egyptian plate letters.
    private val arabicNormalizationMap = mapOf(
        'ا' to 'أ',
        'إ' to 'أ',
        'آ' to 'أ',
        'ى' to 'ي',
        'ة' to 'ه',
        'ذ' to 'د',
        'ز' to 'ر',
        'ش' to 'س',
        'ض' to 'ص',
        'ظ' to 'ط',
        'غ' to 'ع',
        'ئ' to 'ي',
        'ؤ' to 'و'
    )

    init {
        val modelFile = FileUtil.loadMappedFile(context, modelPath)
        val options = Interpreter.Options().apply { setNumThreads(4) }
        interpreter = Interpreter(modelFile, options)

        val inputShape = interpreter.getInputTensor(0).shape()
        inputDataType = interpreter.getInputTensor(0).dataType()
        require(inputShape.size == 4) { "Unsupported input shape: ${inputShape.contentToString()}" }
        require(inputShape[3] == 3) { "Model input must be NHWC with 3 channels" }

        inputHeight = inputShape[1]
        inputWidth = inputShape[2]
        Log.d(TAG, "Character model loaded. Input=${inputWidth}x$inputHeight dtype=$inputDataType")
    }

    fun detect(
        plateBitmap: Bitmap,
        confidenceThreshold: Float = 0.40f,
        iouThreshold: Float = 0.45f
    ): List<CharacterDetection> {
        val inputBuffer = createInputBuffer(plateBitmap)
        val outputTensor = interpreter.getOutputTensor(0)
        val outputShape = outputTensor.shape()
        val outputType = outputTensor.dataType()

        val outputValues = runAndReadOutput(inputBuffer, outputShape, outputType, outputTensor)
        val rawDetections = decodeYoloOutput(
            outputValues = outputValues,
            outputShape = outputShape,
            sourceWidth = plateBitmap.width,
            sourceHeight = plateBitmap.height,
            confidenceThreshold = confidenceThreshold
        )

        return applyNms(rawDetections, iouThreshold)
    }

    fun reconstructPlateText(detections: List<CharacterDetection>): String {
        val letters = detections
            .filter { it.symbol.length == 1 && validPlateLetters.contains(it.symbol[0]) }
            .sortedByDescending { it.xCenter }
            .joinToString(" ") { it.symbol }

        val digits = detections
            .filter { it.symbol.length == 1 && it.symbol[0].isDigit() }
            .sortedBy { it.xCenter }
            .joinToString("") { it.symbol }

        return buildString {
            if (letters.isNotBlank()) append(letters)
            if (letters.isNotBlank() && digits.isNotBlank()) append(" ")
            if (digits.isNotBlank()) append(digits)
        }.trim()
    }

    fun close() {
        interpreter.close()
    }

    private fun createInputBuffer(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
        val pixelCount = inputWidth * inputHeight
        val pixels = IntArray(pixelCount)
        resized.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)

        val bytesPerChannel = if (inputDataType == DataType.FLOAT32) 4 else 1
        val buffer = ByteBuffer.allocateDirect(pixelCount * 3 * bytesPerChannel)
            .order(ByteOrder.nativeOrder())

        val inputTensor = interpreter.getInputTensor(0)
        val quantParams = inputTensor.quantizationParams()

        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF) / 255f
            val g = (pixel shr 8 and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f

            when (inputDataType) {
                DataType.FLOAT32 -> {
                    buffer.putFloat(r)
                    buffer.putFloat(g)
                    buffer.putFloat(b)
                }

                DataType.UINT8 -> {
                    buffer.put(quantizeToUInt8(r, quantParams.scale, quantParams.zeroPoint).toByte())
                    buffer.put(quantizeToUInt8(g, quantParams.scale, quantParams.zeroPoint).toByte())
                    buffer.put(quantizeToUInt8(b, quantParams.scale, quantParams.zeroPoint).toByte())
                }

                DataType.INT8 -> {
                    buffer.put(quantizeToInt8(r, quantParams.scale, quantParams.zeroPoint).toByte())
                    buffer.put(quantizeToInt8(g, quantParams.scale, quantParams.zeroPoint).toByte())
                    buffer.put(quantizeToInt8(b, quantParams.scale, quantParams.zeroPoint).toByte())
                }

                else -> throw IllegalStateException("Unsupported input dtype: $inputDataType")
            }
        }

        buffer.rewind()
        return buffer
    }

    private fun runAndReadOutput(
        inputBuffer: ByteBuffer,
        outputShape: IntArray,
        outputType: DataType,
        outputTensor: org.tensorflow.lite.Tensor
    ): FloatArray {
        val elementsCount = outputShape.fold(1) { acc, dim -> acc * dim }
        return when (outputType) {
            DataType.FLOAT32 -> {
                val out = ByteBuffer.allocateDirect(elementsCount * 4).order(ByteOrder.nativeOrder())
                interpreter.run(inputBuffer, out)
                out.rewind()
                FloatArray(elementsCount) { out.float }
            }

            DataType.UINT8, DataType.INT8 -> {
                val out = ByteBuffer.allocateDirect(elementsCount).order(ByteOrder.nativeOrder())
                interpreter.run(inputBuffer, out)
                out.rewind()

                val quant = outputTensor.quantizationParams()
                FloatArray(elementsCount) {
                    val q = if (outputType == DataType.UINT8) {
                        out.get().toInt() and 0xFF
                    } else {
                        out.get().toInt()
                    }
                    (q - quant.zeroPoint) * quant.scale
                }
            }

            else -> throw IllegalStateException("Unsupported output dtype: $outputType")
        }
    }

    private fun decodeYoloOutput(
        outputValues: FloatArray,
        outputShape: IntArray,
        sourceWidth: Int,
        sourceHeight: Int,
        confidenceThreshold: Float
    ): List<CharacterDetection> {
        if (outputShape.size < 2) return emptyList()

        val labelCount = classNames.size
        val layout = resolveLayout(outputShape, labelCount)
        if (layout.features < 6 || layout.anchors <= 0) return emptyList()

        val hasObjectness = (layout.features - 5) == labelCount
        val classStart = if (hasObjectness) 5 else 4

        val scaleX = sourceWidth.toFloat() / inputWidth
        val scaleY = sourceHeight.toFloat() / inputHeight
        val detections = ArrayList<CharacterDetection>()

        for (anchor in 0 until layout.anchors) {
            val objectness = if (hasObjectness) {
                valueAt(outputValues, layout, featureIndex = 4, anchorIndex = anchor)
            } else {
                1f
            }

            var bestClassIndex = -1
            var bestClassScore = 0f
            for (classIndex in 0 until labelCount) {
                val classScore = valueAt(
                    values = outputValues,
                    layout = layout,
                    featureIndex = classStart + classIndex,
                    anchorIndex = anchor
                )
                if (classScore > bestClassScore) {
                    bestClassScore = classScore
                    bestClassIndex = classIndex
                }
            }

            if (bestClassIndex < 0) continue
            val confidence = bestClassScore * objectness
            if (confidence < confidenceThreshold) continue

            val symbol = classIndexToPlateSymbol(bestClassIndex) ?: continue

            val cx = valueAt(outputValues, layout, 0, anchor)
            val cy = valueAt(outputValues, layout, 1, anchor)
            val w = valueAt(outputValues, layout, 2, anchor)
            val h = valueAt(outputValues, layout, 3, anchor)

            val actualX = if (cx <= 1f) cx * inputWidth else cx
            val actualY = if (cy <= 1f) cy * inputHeight else cy
            val actualW = if (w <= 1f) w * inputWidth else w
            val actualH = if (h <= 1f) h * inputHeight else h

            var left = (actualX - actualW / 2f) * scaleX
            var top = (actualY - actualH / 2f) * scaleY
            var right = (actualX + actualW / 2f) * scaleX
            var bottom = (actualY + actualH / 2f) * scaleY

            left = max(0f, left)
            top = max(0f, top)
            right = min(sourceWidth.toFloat(), right)
            bottom = min(sourceHeight.toFloat(), bottom)

            if (right <= left || bottom <= top) continue

            detections.add(
                CharacterDetection(
                    symbol = symbol,
                    confidence = confidence,
                    xCenter = (left + right) / 2f,
                    boundingBox = RectF(left, top, right, bottom)
                )
            )
        }

        return detections
    }

    private fun classIndexToPlateSymbol(classIndex: Int): String? {
        if (classIndex !in classNames.indices) return null
        val name = classNames[classIndex]
        if (name.length == 1 && name[0].isDigit()) return name

        val rawArabic = when (name) {
            "7aa" -> 'ح'
            "Taa" -> 'ط'
            "Thaa" -> 'ظ'
            "ain" -> 'ع'
            "alif" -> 'أ'
            "baa" -> 'ب'
            "daad" -> 'ض'
            "daal" -> 'د'
            "faa" -> 'ف'
            "ghayn" -> 'غ'
            "haa" -> 'ه'
            "jeem" -> 'ج'
            "kaaf" -> 'ك'
            "khaa" -> 'خ'
            "laam" -> 'ل'
            "meem" -> 'م'
            "noon" -> 'ن'
            "qaaf" -> 'ق'
            "raa" -> 'ر'
            "saad" -> 'ص'
            "seen" -> 'س'
            "sheen" -> 'ش'
            "taa" -> 'ت'
            "thaa" -> 'ث'
            "waw" -> 'و'
            "yaa" -> 'ي'
            "zaal" -> 'ذ'
            "zay" -> 'ز'
            else -> return null
        }

        val normalized = if (validPlateLetters.contains(rawArabic)) {
            rawArabic
        } else {
            arabicNormalizationMap[rawArabic] ?: return null
        }

        return if (validPlateLetters.contains(normalized)) normalized.toString() else null
    }

    private fun applyNms(detections: List<CharacterDetection>, iouThreshold: Float): List<CharacterDetection> {
        if (detections.isEmpty()) return detections

        val sorted = detections.sortedByDescending { it.confidence }.toMutableList()
        val selected = mutableListOf<CharacterDetection>()

        while (sorted.isNotEmpty()) {
            val current = sorted.removeAt(0)
            selected.add(current)
            sorted.removeAll { candidate ->
                candidate.symbol == current.symbol &&
                    intersectionOverUnion(current.boundingBox, candidate.boundingBox) > iouThreshold
            }
        }

        return selected
    }

    private fun intersectionOverUnion(a: RectF, b: RectF): Float {
        val left = max(a.left, b.left)
        val top = max(a.top, b.top)
        val right = min(a.right, b.right)
        val bottom = min(a.bottom, b.bottom)

        if (right <= left || bottom <= top) return 0f

        val intersection = (right - left) * (bottom - top)
        val union = a.width() * a.height() + b.width() * b.height() - intersection
        return if (union <= 0f) 0f else intersection / union
    }

    private fun valueAt(
        values: FloatArray,
        layout: YoloLayout,
        featureIndex: Int,
        anchorIndex: Int
    ): Float {
        val index = if (layout.featuresFirst) {
            featureIndex * layout.anchors + anchorIndex
        } else {
            anchorIndex * layout.features + featureIndex
        }
        return if (index in values.indices) values[index] else 0f
    }

    private fun resolveLayout(shape: IntArray, labelCount: Int): YoloLayout {
        val dims = shape.filterIndexed { index, _ -> !(index == 0 && shape[0] == 1) }
        val first = dims.getOrElse(0) { 0 }
        val second = dims.getOrElse(1) { 0 }

        val candidateFeatureSizes = setOf(labelCount + 4, labelCount + 5)
        return when {
            first in candidateFeatureSizes -> YoloLayout(features = first, anchors = second, featuresFirst = true)
            second in candidateFeatureSizes -> YoloLayout(features = second, anchors = first, featuresFirst = false)
            first in 6..256 && second > first -> YoloLayout(features = first, anchors = second, featuresFirst = true)
            else -> YoloLayout(features = second, anchors = first, featuresFirst = false)
        }
    }

    private fun quantizeToInt8(value: Float, scale: Float, zeroPoint: Int): Int {
        if (scale == 0f) return 0
        val quantized = (value / scale + zeroPoint).roundToInt()
        return quantized.coerceIn(-128, 127)
    }

    private fun quantizeToUInt8(value: Float, scale: Float, zeroPoint: Int): Int {
        if (scale == 0f) return 0
        val quantized = (value / scale + zeroPoint).roundToInt()
        return quantized.coerceIn(0, 255)
    }

    private data class YoloLayout(
        val features: Int,
        val anchors: Int,
        val featuresFirst: Boolean
    )

    companion object {
        private const val TAG = "CHAR_DETECTOR"
    }
}

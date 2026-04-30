package com.depi.graduationproject.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CrnnPlateReader(
    context: Context,
    modelPath: String = "plate_ocr_v3_fp16.tflite"
) {
    private val interpreter: Interpreter
    private val inputWidth = 200
    private val inputHeight = 64

    // Must match FULL_CHARSET from training: index 0 = CTC blank, 1-17 = Arabic letters, 18-27 = Eastern Arabic digits
    private val charset = listOf(
        "أ", "ب", "ج", "د", "ر", "س", "ص", "ط", "ع", "ف",
        "ق", "ل", "م", "ن", "هـ", "و", "ي",
        "٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩"
    )
    private val blankIndex = 0  // CTC blank is at index 0 in the training FULL_CHARSET

    private val validPlateLetters = "أبجدرسصطعفقلمنهـوي"

    data class PlateReadResult(
        val text: String,
        val confidence: Float
    )

    init {
        val modelFile = FileUtil.loadMappedFile(context, modelPath)
        val options = Interpreter.Options().apply { setNumThreads(4) }
        interpreter = Interpreter(modelFile, options)
        Log.d(TAG, "CrnnPlateReader loaded: $modelPath")
    }

    fun read(plateBitmap: Bitmap): PlateReadResult {
        // 1. Preprocess
        val inputBuffer = preprocessBitmap(plateBitmap)

        // 2. Run inference
        val outputShape = interpreter.getOutputTensor(0).shape()
        // Output shape from TFLite: (1, T, num_classes) after ONNX conversion
        val T = outputShape[1]
        val C = outputShape[2]
        val outputBuffer = Array(1) { Array(T) { FloatArray(C) } }
        interpreter.run(inputBuffer, outputBuffer)

        // 3. Greedy CTC decode
        val logits = outputBuffer[0]  // (T, C)
        val decoded = greedyCtcDecode(logits)

        // 4. Compute confidence (average max-prob across non-blank timesteps)
        val confidence = computeConfidence(logits)

        // 5. Validate Egyptian plate format and recalculate confidence if needed
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

        // RGB float32 normalized to [0, 1]
        val buffer = ByteBuffer.allocateDirect(pixelCount * 3 * 4)
            .order(ByteOrder.nativeOrder())

        for (pixel in pixels) {
            buffer.putFloat((pixel shr 16 and 0xFF) / 255f)  // R
            buffer.putFloat((pixel shr 8 and 0xFF) / 255f)   // G
            buffer.putFloat((pixel and 0xFF) / 255f)          // B
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
                // Offset by -1 because blank occupies index 0, so charset starts at model index 1
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
                    // Softmax the timestep to get probability
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
        // Extract characters in original order
        val letters = raw.filter { validPlateLetters.contains(it) }
        val digits = raw.filter { it in '\u0660'..'\u0669' }  // Eastern Arabic numerals ٠-٩

        // Egyptian format: 2-3 letters + 3-4 digits
        val isValid = letters.length in 2..3 && digits.length in 3..4

        // Preserve original order from the raw decoded text
        val orderedText = buildString {
            for (char in raw) {
                if (validPlateLetters.contains(char) || char in '\u0660'..'\u0669') {
                    append(char)
                }
            }
        }

        // Return ordered text with validation status
        return if (isValid) {
            // Format with space between letters and digits
            val formatted = "$letters $digits"
            formatted to true
        } else {
            orderedText to false
        }
    }

    companion object {
        private const val TAG = "CrnnPlateReader"
    }
}

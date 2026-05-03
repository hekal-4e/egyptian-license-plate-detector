package com.depi.graduationproject.data.mlkit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import kotlin.math.max
import kotlin.math.min

data class DetectionResult(val boundingBox: RectF, val confidence: Float)

// احتفظنا بالـ Tuple كما طلبت
private data class Tuple4(val a: Float, val b: Float, val c: Float, val d: Float)

class YoloDetector(context: Context, modelPath: String) {

    private lateinit var interpreter: Interpreter
    private var inputImageWidth: Int = 640
    private var inputImageHeight: Int = 640

    init {
        try {
            val modelFile = FileUtil.loadMappedFile(context, modelPath)
            val options = Interpreter.Options()
            interpreter = Interpreter(modelFile, options)

            val inputShape = interpreter.getInputTensor(0).shape()
            inputImageHeight = inputShape[1]
            inputImageWidth = inputShape[2]
            Log.d("YOLO", "Model Loaded Successfully. Input: $inputImageWidth x $inputImageHeight")
        } catch (e: Exception) {
            Log.e("YOLO", "Error loading model", e)
        }
    }

    val isInitialized: Boolean get() = ::interpreter.isInitialized

    fun detect(bitmap: Bitmap): List<DetectionResult> {
        if (!isInitialized) return emptyList()

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val outputTensor = interpreter.getOutputTensor(0)
        val outputShape = outputTensor.shape() // [1, 5, 8400]

        // =========================================================================
        // 🔥 بداية الإصلاح: التعامل مع المصفوفة ثلاثية الأبعاد 🔥
        // =========================================================================

        val outputBuffer: Array<FloatArray> // المصفوفة الـ 2D التي سيكمل بها كودك
        val anchors: Int

        // هنا نتحقق إذا كان الموديل يخرج 3D (زي YOLOv11)
        if (outputShape.size == 3 && outputShape[0] == 1) {
            val features = outputShape[1] // 5
            anchors = outputShape[2]      // 8400

            // 1. ننشئ مصفوفة 3D لاستقبال البيانات من الموديل (هذا يحل الكراش)
            val output3D = Array(1) { Array(features) { FloatArray(anchors) } }

            // 2. نشغل الموديل
            interpreter.run(tensorImage.buffer, output3D)

            // 3. نأخذ البعد الأول فقط ليكمل كودك القديم عمله (تحويل لـ 2D)
            outputBuffer = output3D[0]

        } else {
            // التعامل مع الحالات الأخرى (للاحتياط)
            anchors = outputShape[1] // fallback
            // مصفوفة مؤقتة لتجنب الأخطاء، لكن الموديل الحالي سيدخل في الـ if اللي فوق
            outputBuffer = Array(anchors) { FloatArray(5) }
            interpreter.run(tensorImage.buffer, outputBuffer)
        }

        // =========================================================================
        // 🔥 نهاية الإصلاح: باقي الكود كما هو (منطقك القديم) 🔥
        // =========================================================================

        val results = ArrayList<DetectionResult>()
        val detectionThreshold = 0.20f

        for (i in 0 until anchors) {
            // منطقك القديم لاستخراج الثقة (يعمل الآن لأن outputBuffer أصبح 2D)
            val confidence = if (outputBuffer.size > 4 && outputBuffer[4].size > i) {
                outputBuffer[4][i]
            } else 0f

            if (confidence > detectionThreshold) {
                // منطقك القديم لاستخراج الإحداثيات
                val x = outputBuffer[0][i]
                val y = outputBuffer[1][i]
                val w = outputBuffer[2][i]
                val h = outputBuffer[3][i]

                // تحويل الإحداثيات (باقي الكود كما هو)
                val actualX = if (x <= 1f) x * inputImageWidth else x
                val actualY = if (y <= 1f) y * inputImageHeight else y
                val actualW = if (w <= 1f) w * inputImageWidth else w
                val actualH = if (h <= 1f) h * inputImageHeight else h

                val (centerX, centerY) = Pair(actualX, actualY)
                val boxWidth = kotlin.math.abs(actualW)
                val boxHeight = kotlin.math.abs(actualH)

                val scaleX = bitmap.width.toFloat() / inputImageWidth
                val scaleY = bitmap.height.toFloat() / inputImageHeight

                var left = (centerX - boxWidth / 2) * scaleX
                var top = (centerY - boxHeight / 2) * scaleY
                var right = (centerX + boxWidth / 2) * scaleX
                var bottom = (centerY + boxHeight / 2) * scaleY

                // إضافة Padding بسيط لضمان عدم قص الأطراف
                val paddingX = (right - left) * 0.20f   // was 0.15
                val paddingY = (bottom - top) * 0.15f   // was 0.10
                left -= paddingX
                top -= paddingY
                right += paddingX
                bottom += paddingY

                left = max(0f, left)
                top = max(0f, top)
                right = min(bitmap.width.toFloat(), right)
                bottom = min(bitmap.height.toFloat(), bottom)

                val rect = RectF(left, top, right, bottom)

                if (rect.width() > 50 && rect.height() > 20) {
                    results.add(DetectionResult(rect, confidence))
                }
            }
        }

        // Apply Non-Maximum Suppression (NMS)
        val nmsResults = applyNms(results, iouThreshold = 0.45f)

        return nmsResults.sortedByDescending { it.boundingBox.width() * it.boundingBox.height() }.take(1)
    }

    private fun applyNms(detections: List<DetectionResult>, iouThreshold: Float): List<DetectionResult> {
        if (detections.isEmpty()) return emptyList()

        val sorted = detections.sortedByDescending { it.confidence }.toMutableList()
        val keep = mutableListOf<DetectionResult>()

        while (sorted.isNotEmpty()) {
            val current = sorted.removeAt(0)
            keep.add(current)

            sorted.removeAll { computeIou(current.boundingBox, it.boundingBox) > iouThreshold }
        }

        return keep
    }

    private fun computeIou(a: RectF, b: RectF): Float {
        val xLeft = max(a.left, b.left)
        val yTop = max(a.top, b.top)
        val xRight = min(a.right, b.right)
        val yBottom = min(a.bottom, b.bottom)

        if (xRight < xLeft || yBottom < yTop) return 0f

        val intersection = (xRight - xLeft) * (yBottom - yTop)
        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)
        val union = areaA + areaB - intersection

        return if (union > 0) intersection / union else 0f
    }

    fun close() {
        if (::interpreter.isInitialized) {
            interpreter.close()
        }
    }
}

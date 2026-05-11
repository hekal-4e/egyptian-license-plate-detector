package com.depi.graduationproject.presentation.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    onImageCaptured: (Bitmap) -> Unit,
    isFlashlightOn: Boolean,
    modifier: Modifier = Modifier,
    analysisIntervalMs: Long = 300L
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    var camera by remember { mutableStateOf<Camera?>(null) }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            var lastAnalyzed = 0L
            imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                val now = System.currentTimeMillis()
                if (now - lastAnalyzed < analysisIntervalMs) {
                    imageProxy.close()
                    return@setAnalyzer
                }
                lastAnalyzed = now
                val bitmap = imageProxy.imageProxyToBitmap()
                imageProxy.close()
                if (bitmap != null) {
                    onImageCaptured(bitmap)
                }
            }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (_: Exception) {
                camera = null
            }
        }

        cameraProviderFuture.addListener(listener, cameraExecutor)

        onDispose {
            try {
                cameraProviderFuture.get().unbindAll()
            } catch (_: Exception) {
                // Best-effort cleanup.
            } finally {
                analysisExecutor.shutdown()
            }
        }
    }

    LaunchedEffect(isFlashlightOn, camera) {
        camera?.cameraControl?.enableTorch(isFlashlightOn)
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

object CameraUtils {

    fun takePhoto(
        context: Context,
        imageCapture: ImageCapture?,
        onImageCaptured: (Bitmap) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val capture = imageCapture ?: return

        capture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = image.imageProxyToBitmap()
                    image.close()
                    if (bitmap != null) {
                        onImageCaptured(bitmap)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? = image.imageProxyToBitmap()
}

private fun ImageProxy.imageProxyToBitmap(): Bitmap? {
    val width = width
    val height = height
    val yPlane = planes[0]
    val uPlane = planes[1]
    val vPlane = planes[2]

    val nv21 = ByteArray(width * height * 3 / 2)
    var position = 0

    val yBuffer = yPlane.buffer
    val yRowStride = yPlane.rowStride
    for (row in 0 until height) {
        val yRowStart = row * yRowStride
        yBuffer.position(yRowStart)
        yBuffer.get(nv21, position, width)
        position += width
    }

    val uvRowStride = uPlane.rowStride
    val uvPixelStride = uPlane.pixelStride
    val uBuffer = uPlane.buffer
    val vBuffer = vPlane.buffer
    val uvHeight = height / 2
    val uvWidth = width / 2

    for (row in 0 until uvHeight) {
        val uvRowStart = row * uvRowStride
        for (col in 0 until uvWidth) {
            val uvIndex = uvRowStart + col * uvPixelStride
            val vByte = vBuffer.get(uvIndex)
            val uByte = uBuffer.get(uvIndex)
            nv21[position++] = vByte
            nv21[position++] = uByte
        }
    }

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 90, out)
    val imageBytes = out.toByteArray()
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) ?: return null

    if (imageInfo.rotationDegrees == 0) return bitmap

    val matrix = Matrix().apply { postRotate(imageInfo.rotationDegrees.toFloat()) }
    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    if (rotated != bitmap) bitmap.recycle()
    return rotated
}
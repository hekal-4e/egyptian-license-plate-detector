package com.depi.graduationproject.ui.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.depi.graduationproject.ui.components.CameraControls
import com.depi.graduationproject.ui.viewmodels.MainViewModel
import com.depi.graduationproject.util.CameraUtils

@Composable
fun CameraScreen(
    viewModel: MainViewModel,
    onNavigateToHistory: () -> Unit,
    onImageCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. عرض الكاميرا (Preview) كما هو في الكود الأصلي
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                    } catch (exc: Exception) {
                        Log.e("CameraScreen", "Camera binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. طبقة الـ AI Neon Overlay (تصميم جديد)
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 220.dp)) {
            val strokeWidth = 3.dp.toPx()
            drawRoundRect(
                color = Color(0xFF00E676), // Neon Green
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(60f, 30f), 0f)
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
            )
        }

        Text(
            text = "Align License Plate in Frame",
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 160.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )

        // 3. واجهة التحكم (الأصلية)
        CameraControls(
            onCaptureClick = {
                CameraUtils.takePhoto(
                    context = context,
                    imageCapture = imageCapture,
                    onImageCaptured = onImageCaptured,
                    onError = { Log.e("CameraScreen", "Error taking photo: ${it.message}") }
                )
            },
            onHistoryClick = onNavigateToHistory,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
package com.depi.graduationproject.data.mlkit

import android.graphics.Bitmap
import com.depi.graduationproject.data.model.PlateAnalysisResult

interface IPlateAnalyzer {
    val ocrModelName: String
    fun initialize()
    fun isInitialized(): Boolean
    suspend fun analyze(originalBitmap: Bitmap): PlateAnalysisResult
    fun close()
}
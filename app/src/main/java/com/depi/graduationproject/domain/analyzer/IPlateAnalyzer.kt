package com.depi.graduationproject.domain.analyzer

import com.depi.graduationproject.domain.model.ImageFrame
import com.depi.graduationproject.domain.model.PlateAnalysisResult

interface IPlateAnalyzer {
    val ocrModelName: String
    fun initialize()
    fun isInitialized(): Boolean
    suspend fun analyze(imageFrame: ImageFrame): PlateAnalysisResult
    fun close()
}
package com.depi.graduationproject.domain.analyzer

import com.depi.graduationproject.domain.model.PlateAnalysisResult

interface IPlateAnalyzer {
    val ocrModelName: String
    fun initialize()
    fun isInitialized(): Boolean
    suspend fun analyze(imageData: ByteArray): PlateAnalysisResult
    fun close()
}

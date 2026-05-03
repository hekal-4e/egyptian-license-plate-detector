package com.depi.graduationproject.domain.model

data class PlateAnalysisResult(
    val isSuccess: Boolean,
    val text: String,
    val imageBytes: ByteArray?,
    val message: String,
    val confidence: Float = 0f
)

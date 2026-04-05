package com.depi.graduationproject.data.model

import android.graphics.Bitmap

data class PlateAnalysisResult(
    val isSuccess: Boolean,
    val text: String,
    val bitmap: Bitmap?,
    val message: String
)
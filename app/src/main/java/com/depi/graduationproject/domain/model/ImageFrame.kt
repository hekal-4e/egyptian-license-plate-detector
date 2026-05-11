package com.depi.graduationproject.domain.model

enum class PixelFormat {
    ARGB_8888
}

data class ImageFrame(
    val bytes: ByteArray,
    val width: Int,
    val height: Int,
    val format: PixelFormat = PixelFormat.ARGB_8888
)

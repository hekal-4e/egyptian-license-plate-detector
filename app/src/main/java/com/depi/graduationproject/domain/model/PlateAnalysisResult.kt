package com.depi.graduationproject.domain.model

data class PlateAnalysisResult(
    val isSuccess: Boolean,
    val text: String,
    val imageBytes: ByteArray?,
    val message: String,
    val confidence: Float = 0f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlateAnalysisResult) return false

        if (isSuccess != other.isSuccess) return false
        if (text != other.text) return false
        if (!imageBytes.contentEquals(other.imageBytes)) return false
        if (message != other.message) return false
        if (confidence != other.confidence) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isSuccess.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + imageBytes.contentHashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + confidence.hashCode()
        return result
    }
}

private fun ByteArray?.contentEquals(other: ByteArray?): Boolean {
    if (this === other) return true
    if (this == null || other == null) return false
    return java.util.Arrays.equals(this, other)
}

private fun ByteArray?.contentHashCode(): Int {
    if (this == null) return 0
    return java.util.Arrays.hashCode(this)
}
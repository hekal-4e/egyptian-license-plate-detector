package com.depi.graduationproject.domain.model

data class Zone(
    val id: String,
    val name: String,
    val description: String,
    val totalCapacity: Int,
    val occupiedSpots: Int = 0
) {
    val isFull: Boolean get() = occupiedSpots >= totalCapacity
    val spotsAvailable: Int get() = totalCapacity - occupiedSpots
}

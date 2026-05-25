package com.depi.graduationproject.data.mlkit

import com.depi.graduationproject.core.utils.PlateUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PlateSlotTransformerMappingTest {

    @Test
    fun mapOutputs_output2Output3LengthNames_mapsCorrectly() {
        val letterClasses = PlateUtils.ARABIC_LETTERS.size + 1
        val descriptors = listOf(
            TensorDescriptor(0, "output_3", intArrayOf(1, 2)),
            TensorDescriptor(1, "output_2", intArrayOf(1, 2)),
            TensorDescriptor(2, "output_1", intArrayOf(1, 3, letterClasses)),
            TensorDescriptor(3, "output_0", intArrayOf(1, 4, 11))
        )

        val result = mapV4Outputs(descriptors)

        assertEquals(3, result.mapping.digitLogitsIndex)
        assertEquals(2, result.mapping.letterLogitsIndex)
        assertEquals(1, result.mapping.digitLenIndex)
        assertEquals(0, result.mapping.letterLenIndex)
    }

    @Test
    fun mapOutputs_unlabeledLengthHeads_throws() {
        val letterClasses = PlateUtils.ARABIC_LETTERS.size + 1
        val descriptors = listOf(
            TensorDescriptor(5, "len_a", intArrayOf(1, 2)),
            TensorDescriptor(7, "len_b", intArrayOf(1, 2)),
            TensorDescriptor(2, "digits", intArrayOf(1, 4, 11)),
            TensorDescriptor(3, "letters", intArrayOf(1, 3, letterClasses))
        )

        assertThrows(IllegalArgumentException::class.java) {
            mapV4Outputs(descriptors)
        }
    }
}

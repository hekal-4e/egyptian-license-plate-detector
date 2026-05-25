package com.depi.graduationproject.data.mlkit

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlateSlotTransformerInt8SmokeTest {

    @Test
    fun staticInt8Model_contractAndSingleInvoke_succeeds() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val reader = PlateSlotTransformerReader(
            context,
            "ealpr_v4_static_wi8_ai8_full_integer.tflite"
        )

        val bitmap = Bitmap.createBitmap(320, 96, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
        }

        try {
            val result = reader.read(bitmap)
            assertTrue(result.confidence >= 0f)
        } finally {
            bitmap.recycle()
            reader.close()
        }
    }
}

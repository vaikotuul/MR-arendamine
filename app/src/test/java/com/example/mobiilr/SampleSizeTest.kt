package com.example.mobiilr

import org.junit.Assert.assertEquals
import org.junit.Test

class SampleSizeTest {

    @Test
    fun vaikePilt_jaabSamaks() = assertEquals(1, sampleSize(1920, 1080))

    @Test
    fun suurPilt_kahandatakse() {
        assertEquals(2, sampleSize(4000, 3000))  // 12MP
        assertEquals(4, sampleSize(8160, 6120))  // 50MP
        assertEquals(4, sampleSize(6120, 8160))  // püstine
    }
}

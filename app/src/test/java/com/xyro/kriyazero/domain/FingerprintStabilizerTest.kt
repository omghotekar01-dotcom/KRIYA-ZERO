package com.xyro.kriyazero.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FingerprintStabilizerTest {
    @Test
    fun rollingWindow_averagesRecentCameraNoise() {
        val stabilizer = FingerprintStabilizer(windowSize = 3)

        stabilizer.push(fingerprint(100))
        stabilizer.push(fingerprint(110))
        val averaged = stabilizer.push(fingerprint(120))

        assertEquals(listOf(110, 110, 110, 110), averaged.y)
        assertEquals(3, stabilizer.sampleCount())
    }

    @Test
    fun rollingWindow_discardsOldestSample() {
        val stabilizer = FingerprintStabilizer(windowSize = 2)

        stabilizer.push(fingerprint(80))
        stabilizer.push(fingerprint(100))
        val averaged = stabilizer.push(fingerprint(120))

        assertEquals(listOf(110, 110, 110, 110), averaged.y)
        assertEquals(2, stabilizer.sampleCount())
    }

    private fun fingerprint(y: Int) = VisualFingerprint(
        gridSize = 2,
        y = listOf(y, y, y, y),
        u = listOf(128, 128, 128, 128),
        v = listOf(128, 128, 128, 128),
    )
}

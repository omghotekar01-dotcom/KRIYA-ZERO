package com.xyro.kriyazero.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VisualFingerprintTest {
    private val base = VisualFingerprint(
        gridSize = 2,
        y = listOf(90, 110, 100, 120),
        u = listOf(128, 128, 128, 128),
        v = listOf(128, 128, 128, 128),
    )

    @Test
    fun identicalFingerprint_hasPerfectSimilarity() {
        assertEquals(1f, base.similarity(base), 0.0001f)
    }

    @Test
    fun globalBrightnessShift_isMeanNormalized() {
        val brighter = base.copy(y = base.y.map { it + 30 })
        assertEquals(1f, base.similarity(brighter), 0.0001f)
    }

    @Test
    fun strongChromaChange_dropsBelowVerifierThreshold() {
        val changed = base.copy(u = listOf(200, 200, 200, 200))
        assertTrue(base.similarity(changed) < 0.94f)
    }
}

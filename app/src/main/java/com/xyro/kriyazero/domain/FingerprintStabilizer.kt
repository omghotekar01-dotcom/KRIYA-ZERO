package com.xyro.kriyazero.domain

import java.util.ArrayDeque

/**
 * Rolling consensus for live visual fingerprints.
 *
 * Camera exposure/autofocus can move individual YUV samples even when the
 * physical state is unchanged. Averaging a tiny recent window makes checkpoint
 * capture and verification substantially less jittery while staying local and
 * inexpensive.
 */
class FingerprintStabilizer(
    private val windowSize: Int = 5,
) {
    private val samples = ArrayDeque<VisualFingerprint>()

    init {
        require(windowSize in 2..15) { "Fingerprint window must be between 2 and 15 samples." }
    }

    @Synchronized
    fun push(sample: VisualFingerprint): VisualFingerprint {
        val incompatible = samples.firstOrNull()?.gridSize?.let { it != sample.gridSize } ?: false
        if (incompatible) samples.clear()

        samples.addLast(sample)
        while (samples.size > windowSize) samples.removeFirst()
        return average(samples.toList())
    }

    @Synchronized
    fun sampleCount(): Int = samples.size

    @Synchronized
    fun reset() {
        samples.clear()
    }

    private fun average(values: List<VisualFingerprint>): VisualFingerprint {
        require(values.isNotEmpty())
        val gridSize = values.first().gridSize
        val cellCount = gridSize * gridSize

        val y = MutableList(cellCount) { 0 }
        val u = MutableList(cellCount) { 0 }
        val v = MutableList(cellCount) { 0 }

        values.forEach { fingerprint ->
            require(fingerprint.gridSize == gridSize)
            for (index in 0 until cellCount) {
                y[index] += fingerprint.y[index]
                u[index] += fingerprint.u[index]
                v[index] += fingerprint.v[index]
            }
        }

        val divisor = values.size
        return VisualFingerprint(
            gridSize = gridSize,
            y = y.map { it / divisor },
            u = u.map { it / divisor },
            v = v.map { it / divisor },
        )
    }
}

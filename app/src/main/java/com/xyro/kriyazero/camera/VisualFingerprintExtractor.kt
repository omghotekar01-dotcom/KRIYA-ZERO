package com.xyro.kriyazero.camera

import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy
import com.xyro.kriyazero.domain.VisualFingerprint

/**
 * Produces a compact, task-agnostic fingerprint directly from the YUV camera
 * buffer. The central workspace is sampled into a grid. This is intentionally
 * lightweight enough to run on every analysed frame without a network/model.
 */
object VisualFingerprintExtractor {
    fun extract(
        image: ImageProxy,
        gridSize: Int = 12,
    ): VisualFingerprint? {
        if (image.format != ImageFormat.YUV_420_888 || image.planes.size < 3) return null
        if (gridSize < 2) return null

        val crop = image.cropRect
        if (crop.width() <= 0 || crop.height() <= 0) return null

        // Ignore the outer 10% where hands/UI framing changes tend to dominate.
        val horizontalInset = crop.width() / 10
        val verticalInset = crop.height() / 10
        val left = crop.left + horizontalInset
        val right = crop.right - horizontalInset
        val top = crop.top + verticalInset
        val bottom = crop.bottom - verticalInset

        if (right <= left || bottom <= top) return null

        val yValues = ArrayList<Int>(gridSize * gridSize)
        val uValues = ArrayList<Int>(gridSize * gridSize)
        val vValues = ArrayList<Int>(gridSize * gridSize)

        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        for (gridY in 0 until gridSize) {
            val y = sampleCoordinate(top, bottom, gridY, gridSize)
            for (gridX in 0 until gridSize) {
                val x = sampleCoordinate(left, right, gridX, gridSize)

                yValues += samplePlane(yPlane, x, y, fallback = 128)
                uValues += samplePlane(uPlane, x / 2, y / 2, fallback = 128)
                vValues += samplePlane(vPlane, x / 2, y / 2, fallback = 128)
            }
        }

        return VisualFingerprint(
            gridSize = gridSize,
            y = yValues,
            u = uValues,
            v = vValues,
        )
    }

    private fun sampleCoordinate(
        start: Int,
        endExclusive: Int,
        cell: Int,
        cells: Int,
    ): Int {
        val span = endExclusive - start
        val normalizedCenter = (cell + 0.5f) / cells.toFloat()
        return (start + (span * normalizedCenter).toInt())
            .coerceIn(start, endExclusive - 1)
    }

    private fun samplePlane(
        plane: ImageProxy.PlaneProxy,
        x: Int,
        y: Int,
        fallback: Int,
    ): Int {
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride
        if (rowStride <= 0 || pixelStride <= 0) return fallback

        val index = (y * rowStride) + (x * pixelStride)
        val buffer = plane.buffer
        if (index < 0 || index >= buffer.limit()) return fallback
        return buffer.get(index).toInt() and 0xFF
    }
}

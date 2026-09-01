package com.xyro.kriyazero.domain

import kotlin.math.abs

/**
 * Compact grid fingerprint sampled directly from a camera YUV frame.
 *
 * It is intentionally simple and transparent: the MVP can learn a new visual
 * checkpoint without a task-specific model or cloud call. More capable image
 * embeddings can replace this representation behind the same domain boundary.
 */
data class VisualFingerprint(
    val gridSize: Int,
    val y: List<Int>,
    val u: List<Int>,
    val v: List<Int>,
) {
    init {
        require(gridSize > 1) { "Fingerprint grid must be at least 2x2." }
        require(y.size == gridSize * gridSize) { "Unexpected Y fingerprint size." }
        require(u.size == y.size && v.size == y.size) { "Y/U/V fingerprint sizes must match." }
    }

    /**
     * Similarity in [0, 1]. Luma is mean-normalized so global brightness shifts
     * have less influence than local structure and chroma changes.
     */
    fun similarity(other: VisualFingerprint): Float {
        if (gridSize != other.gridSize || y.size != other.y.size) return 0f

        val meanY = y.average()
        val otherMeanY = other.y.average()
        var distance = 0f

        for (index in y.indices) {
            val relativeLumaA = y[index] - meanY
            val relativeLumaB = other.y[index] - otherMeanY
            val lumaDistance = abs(relativeLumaA - relativeLumaB).toFloat() / 255f
            val uDistance = abs(u[index] - other.u[index]).toFloat() / 255f
            val vDistance = abs(v[index] - other.v[index]).toFloat() / 255f

            distance += (0.25f * lumaDistance) +
                (0.375f * uDistance) +
                (0.375f * vDistance)
        }

        return (1f - (distance / y.size.toFloat())).coerceIn(0f, 1f)
    }
}

/**
 * A compact representation of one demonstration moment after perception has
 * converted camera/audio input into verifier evidence.
 */
data class DemonstrationSegment(
    val index: Int,
    val narration: String,
    val observedObjects: Set<String> = emptySet(),
    val stateTags: Set<String> = emptySet(),
    val visualFingerprint: VisualFingerprint? = null,
    val timestampMs: Long = 0L,
)

data class ProcedureStep(
    val id: String,
    val order: Int,
    val title: String,
    val instruction: String,
    val requiredObjects: Set<String>,
    val expectedStateTags: Set<String>,
    val visualFingerprint: VisualFingerprint? = null,
    val dependsOn: Set<String> = emptySet(),
)

data class SkillCapsule(
    val id: String,
    val name: String,
    val sourceNarration: String,
    val createdAtEpochMs: Long,
    val steps: List<ProcedureStep>,
) {
    init {
        require(name.isNotBlank()) { "Skill Capsule name must not be blank." }
        require(steps.isNotEmpty()) { "A Skill Capsule must contain at least one step." }
        require(steps.map { it.id }.distinct().size == steps.size) {
            "Procedure step identifiers must be unique."
        }
        require(steps.map { it.order }.distinct().size == steps.size) {
            "Procedure step order values must be unique."
        }
    }

    val orderedSteps: List<ProcedureStep>
        get() = steps.sortedBy { it.order }
}

/**
 * Evidence extracted from the learner's current live frame.
 *
 * The same observation can contain semantic detector output and/or a raw visual
 * fingerprint learned directly from the demonstration.
 */
data class Observation(
    val objectConfidence: Map<String, Float> = emptyMap(),
    val stateTags: Set<String> = emptySet(),
    val visualFingerprint: VisualFingerprint? = null,
    val capturedAtEpochMs: Long = System.currentTimeMillis(),
) {
    fun containsObject(label: String, threshold: Float): Boolean =
        (objectConfidence[label.normalizeLabel()] ?: 0f) >= threshold
}

enum class VerificationStatus {
    PASS,
    FAIL,
    SEQUENCE_ERROR,
    COMPLETE,
}

data class VerificationDecision(
    val status: VerificationStatus,
    val step: ProcedureStep?,
    val message: String,
    val missingObjects: Set<String> = emptySet(),
    val missingStateTags: Set<String> = emptySet(),
    val visualSimilarity: Float? = null,
    val visualCheckpointMissing: Boolean = false,
    val matchedFutureStep: ProcedureStep? = null,
)

data class AssessmentReport(
    val capsuleName: String,
    val totalSteps: Int,
    val completedSteps: Int,
    val firstAttemptPasses: Int,
    val failedAttempts: Int,
    val sequenceErrors: Int,
    val assistanceCount: Int,
) {
    val completionPercent: Int =
        if (totalSteps == 0) 0 else ((completedSteps * 100f) / totalSteps).toInt()

    val firstAttemptAccuracyPercent: Int =
        if (totalSteps == 0) 0 else ((firstAttemptPasses * 100f) / totalSteps).toInt()

    val independentlyCompleted: Boolean =
        completedSteps == totalSteps && assistanceCount == 0
}

internal fun String.normalizeLabel(): String =
    trim().lowercase().replace(Regex("\\s+"), " ")

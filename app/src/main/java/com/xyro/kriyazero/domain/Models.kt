package com.xyro.kriyazero.domain

/**
 * A compact representation of one demonstration moment after perception has
 * converted camera/audio input into semantic evidence.
 *
 * The perception layer is intentionally outside the domain module. During the
 * hackathon it can be backed by deterministic demo evidence, ML Kit/MediaPipe,
 * TFLite/LiteRT, or a local VLM without changing verification semantics.
 */
data class DemonstrationSegment(
    val index: Int,
    val narration: String,
    val observedObjects: Set<String>,
    val stateTags: Set<String> = emptySet(),
    val timestampMs: Long = 0L,
)

data class ProcedureStep(
    val id: String,
    val order: Int,
    val title: String,
    val instruction: String,
    val requiredObjects: Set<String>,
    val expectedStateTags: Set<String>,
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
 * Semantic evidence extracted from a live frame.
 *
 * objectConfidence keys are normalized lowercase labels. stateTags capture
 * task-specific visual states such as `resistor-in-row-10` or `led-oriented`.
 */
data class Observation(
    val objectConfidence: Map<String, Float>,
    val stateTags: Set<String> = emptySet(),
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

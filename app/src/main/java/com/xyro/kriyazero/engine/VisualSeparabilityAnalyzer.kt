package com.xyro.kriyazero.engine

import com.xyro.kriyazero.domain.SkillCapsule

enum class SeparabilityLevel {
    GOOD,
    TIGHT,
    AMBIGUOUS,
}

data class CheckpointSeparability(
    val stepId: String,
    val nearestStepId: String?,
    val nearestSimilarity: Float?,
    val level: SeparabilityLevel,
)

data class SeparabilityReport(
    val checkpoints: List<CheckpointSeparability>,
) {
    val hasAmbiguousCheckpoints: Boolean =
        checkpoints.any { it.level == SeparabilityLevel.AMBIGUOUS }

    val minimumDistance: Float? = checkpoints
        .mapNotNull { it.nearestSimilarity }
        .maxOrNull()
        ?.let { 1f - it }
}

/**
 * Audits whether learned visual checkpoints are distinct enough from one another
 * for a lightweight fixed-camera verifier.
 *
 * This does not pretend to predict all real-device variance. It catches a very
 * common hackathon failure early: two consecutive taught states whose visual
 * fingerprints are so similar that a global threshold cannot separate them.
 */
object VisualSeparabilityAnalyzer {
    const val AMBIGUOUS_SIMILARITY = 0.985f
    const val TIGHT_SIMILARITY = 0.965f

    fun analyze(capsule: SkillCapsule): SeparabilityReport {
        val visualSteps = capsule.orderedSteps.filter { it.visualFingerprint != null }

        val checkpoints = capsule.orderedSteps.map { step ->
            val fingerprint = step.visualFingerprint
            if (fingerprint == null) {
                return@map CheckpointSeparability(
                    stepId = step.id,
                    nearestStepId = null,
                    nearestSimilarity = null,
                    level = SeparabilityLevel.GOOD,
                )
            }

            val nearest = visualSteps
                .asSequence()
                .filter { it.id != step.id }
                .mapNotNull { other ->
                    other.visualFingerprint?.let { otherFingerprint ->
                        other.id to fingerprint.similarity(otherFingerprint)
                    }
                }
                .maxByOrNull { it.second }

            val similarity = nearest?.second
            val level = when {
                similarity == null -> SeparabilityLevel.GOOD
                similarity >= AMBIGUOUS_SIMILARITY -> SeparabilityLevel.AMBIGUOUS
                similarity >= TIGHT_SIMILARITY -> SeparabilityLevel.TIGHT
                else -> SeparabilityLevel.GOOD
            }

            CheckpointSeparability(
                stepId = step.id,
                nearestStepId = nearest?.first,
                nearestSimilarity = similarity,
                level = level,
            )
        }

        return SeparabilityReport(checkpoints)
    }
}

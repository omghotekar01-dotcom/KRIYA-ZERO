package com.xyro.kriyazero.engine

import com.xyro.kriyazero.domain.AssessmentReport
import com.xyro.kriyazero.domain.Observation
import com.xyro.kriyazero.domain.ProcedureStep
import com.xyro.kriyazero.domain.SkillCapsule
import com.xyro.kriyazero.domain.VerificationDecision
import com.xyro.kriyazero.domain.VerificationStatus
import com.xyro.kriyazero.domain.normalizeLabel

/**
 * Deterministic execution gate for a Skill Capsule.
 *
 * The verifier deliberately does not ask an LLM whether a learner passed.
 * Perception produces evidence; this engine applies explicit procedure
 * constraints to that evidence. This separation keeps pass/fail reproducible.
 */
class VerificationEngine(
    private val capsule: SkillCapsule,
    private val objectConfidenceThreshold: Float = 0.60f,
    private val visualSimilarityThreshold: Float = 0.94f,
    private val futureVisualMargin: Float = 0.015f,
) {
    private val steps = capsule.orderedSteps
    private val completedStepIds = linkedSetOf<String>()
    private val failedAttemptCountByStep = mutableMapOf<String, Int>()
    private val firstAttemptPassStepIds = linkedSetOf<String>()

    private var failedAttempts: Int = 0
    private var sequenceErrors: Int = 0
    private var assistanceCount: Int = 0

    init {
        require(objectConfidenceThreshold in 0f..1f) {
            "Object confidence threshold must be between 0 and 1."
        }
        require(visualSimilarityThreshold in 0f..1f) {
            "Visual similarity threshold must be between 0 and 1."
        }
        require(futureVisualMargin >= 0f) {
            "Future visual margin must be non-negative."
        }
    }

    fun currentStep(): ProcedureStep? =
        steps.firstOrNull { it.id !in completedStepIds }

    fun completedSteps(): Set<String> = completedStepIds.toSet()

    fun requestAssistance() {
        assistanceCount += 1
    }

    fun verify(observation: Observation): VerificationDecision {
        val step = currentStep()
            ?: return VerificationDecision(
                status = VerificationStatus.COMPLETE,
                step = null,
                message = "Skill already verified.",
            )

        val unmetDependencies = step.dependsOn - completedStepIds
        if (unmetDependencies.isNotEmpty()) {
            return VerificationDecision(
                status = VerificationStatus.SEQUENCE_ERROR,
                step = step,
                message = "Complete the previous required step before continuing.",
            )
        }

        if (!step.hasVerificationEvidence()) {
            registerFailedAttempt(step)
            return VerificationDecision(
                status = VerificationStatus.FAIL,
                step = step,
                message = "This step has no verifier evidence yet. Capture a checkpoint before assessment.",
            )
        }

        val currentMatch = step.evaluate(observation)

        // Check for a future state before accepting the current checkpoint. This is
        // necessary for cumulative procedures where a later state can also satisfy
        // all semantic requirements from an earlier state.
        val futureMatch = steps
            .dropWhile { it.id != step.id }
            .drop(1)
            .map { candidate -> candidate to candidate.evaluate(observation) }
            .firstOrNull { (candidate, result) ->
                if (!result.isMatch) return@firstOrNull false

                val currentVisual = currentMatch.visualSimilarity
                val candidateVisual = result.visualSimilarity

                when {
                    candidate.visualFingerprint == null -> true
                    step.visualFingerprint == null -> true
                    currentVisual == null || candidateVisual == null -> false
                    else -> candidateVisual >= currentVisual + futureVisualMargin
                }
            }

        if (futureMatch != null) {
            registerFailedAttempt(step)
            sequenceErrors += 1
            return VerificationDecision(
                status = VerificationStatus.SEQUENCE_ERROR,
                step = step,
                matchedFutureStep = futureMatch.first,
                message = "Execution jumped ahead to ${futureMatch.first.title}. Complete ${step.title} first.",
                missingObjects = currentMatch.missingObjects,
                missingStateTags = currentMatch.missingStateTags,
                visualSimilarity = currentMatch.visualSimilarity,
                visualCheckpointMissing = currentMatch.visualCheckpointMissing,
            )
        }

        if (currentMatch.isMatch) {
            val hadPreviousFailure = (failedAttemptCountByStep[step.id] ?: 0) > 0
            completedStepIds += step.id
            if (!hadPreviousFailure) firstAttemptPassStepIds += step.id

            val complete = completedStepIds.size == steps.size
            return VerificationDecision(
                status = if (complete) VerificationStatus.COMPLETE else VerificationStatus.PASS,
                step = step,
                message = if (complete) {
                    "Final checkpoint verified. Skill complete."
                } else {
                    "${step.title} verified. Continue to the next step."
                },
                visualSimilarity = currentMatch.visualSimilarity,
            )
        }

        registerFailedAttempt(step)
        return VerificationDecision(
            status = VerificationStatus.FAIL,
            step = step,
            message = buildFailureMessage(currentMatch),
            missingObjects = currentMatch.missingObjects,
            missingStateTags = currentMatch.missingStateTags,
            visualSimilarity = currentMatch.visualSimilarity,
            visualCheckpointMissing = currentMatch.visualCheckpointMissing,
        )
    }

    fun report(): AssessmentReport = AssessmentReport(
        capsuleName = capsule.name,
        totalSteps = steps.size,
        completedSteps = completedStepIds.size,
        firstAttemptPasses = firstAttemptPassStepIds.size,
        failedAttempts = failedAttempts,
        sequenceErrors = sequenceErrors,
        assistanceCount = assistanceCount,
    )

    private fun registerFailedAttempt(step: ProcedureStep) {
        failedAttempts += 1
        failedAttemptCountByStep[step.id] =
            (failedAttemptCountByStep[step.id] ?: 0) + 1
    }

    private fun ProcedureStep.evaluate(observation: Observation): StepMatch {
        if (!hasVerificationEvidence()) return StepMatch.noEvidence()

        val missingObjects = requiredObjects.filterNot {
            observation.containsObject(it, objectConfidenceThreshold)
        }.toSet()

        val observedStates = observation.stateTags.map { it.normalizeLabel() }.toSet()
        val missingStateTags = expectedStateTags - observedStates

        val visualCheckpointMissing = visualFingerprint != null && observation.visualFingerprint == null
        val visualSimilarity = if (visualFingerprint != null && observation.visualFingerprint != null) {
            visualFingerprint.similarity(observation.visualFingerprint)
        } else {
            null
        }
        val visualMatches = when {
            visualFingerprint == null -> true
            visualCheckpointMissing -> false
            else -> (visualSimilarity ?: 0f) >= visualSimilarityThreshold
        }

        return StepMatch(
            missingObjects = missingObjects,
            missingStateTags = missingStateTags,
            visualSimilarity = visualSimilarity,
            visualCheckpointMissing = visualCheckpointMissing,
            isMatch = missingObjects.isEmpty() && missingStateTags.isEmpty() && visualMatches,
        )
    }

    private fun ProcedureStep.hasVerificationEvidence(): Boolean =
        requiredObjects.isNotEmpty() ||
            expectedStateTags.isNotEmpty() ||
            visualFingerprint != null

    private fun buildFailureMessage(match: StepMatch): String {
        val reasons = buildList {
            if (match.missingObjects.isNotEmpty()) {
                add("missing: ${match.missingObjects.sorted().joinToString()}")
            }
            if (match.missingStateTags.isNotEmpty()) {
                add("state mismatch: ${match.missingStateTags.sorted().joinToString()}")
            }
            if (match.visualCheckpointMissing) {
                add("camera fingerprint unavailable")
            } else if (match.visualSimilarity != null && match.visualSimilarity < visualSimilarityThreshold) {
                add("visual similarity ${(match.visualSimilarity * 100).toInt()}%")
            }
        }
        return "Checkpoint not verified — ${reasons.joinToString("; ")}."
    }

    private data class StepMatch(
        val missingObjects: Set<String>,
        val missingStateTags: Set<String>,
        val visualSimilarity: Float?,
        val visualCheckpointMissing: Boolean,
        val isMatch: Boolean,
    ) {
        companion object {
            fun noEvidence() = StepMatch(
                missingObjects = emptySet(),
                missingStateTags = emptySet(),
                visualSimilarity = null,
                visualCheckpointMissing = true,
                isMatch = false,
            )
        }
    }
}

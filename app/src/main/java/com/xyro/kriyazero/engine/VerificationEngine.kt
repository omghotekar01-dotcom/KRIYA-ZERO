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
 * Perception produces semantic evidence; this engine applies explicit procedure
 * constraints to that evidence. This separation keeps pass/fail reproducible.
 */
class VerificationEngine(
    private val capsule: SkillCapsule,
    private val objectConfidenceThreshold: Float = 0.60f,
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
            failedAttempts += 1
            failedAttemptCountByStep[step.id] =
                (failedAttemptCountByStep[step.id] ?: 0) + 1
            return VerificationDecision(
                status = VerificationStatus.FAIL,
                step = step,
                message = "This step has no visual verifier evidence yet. Capture a checkpoint before assessment.",
            )
        }

        val missingObjects = step.requiredObjects.filterNot {
            observation.containsObject(it, objectConfidenceThreshold)
        }.toSet()

        val normalizedObservedStates = observation.stateTags
            .map { it.normalizeLabel() }
            .toSet()
        val missingStateTags = step.expectedStateTags - normalizedObservedStates

        if (missingObjects.isEmpty() && missingStateTags.isEmpty()) {
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
            )
        }

        failedAttempts += 1
        failedAttemptCountByStep[step.id] =
            (failedAttemptCountByStep[step.id] ?: 0) + 1

        val futureMatch = steps
            .dropWhile { it.id != step.id }
            .drop(1)
            .firstOrNull { candidate -> candidate.matches(observation) }

        if (futureMatch != null) {
            sequenceErrors += 1
            return VerificationDecision(
                status = VerificationStatus.SEQUENCE_ERROR,
                step = step,
                matchedFutureStep = futureMatch,
                message = "Execution jumped ahead to ${futureMatch.title}. Complete ${step.title} first.",
                missingObjects = missingObjects,
                missingStateTags = missingStateTags,
            )
        }

        return VerificationDecision(
            status = VerificationStatus.FAIL,
            step = step,
            message = buildFailureMessage(missingObjects, missingStateTags),
            missingObjects = missingObjects,
            missingStateTags = missingStateTags,
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

    private fun ProcedureStep.matches(observation: Observation): Boolean {
        if (!hasVerificationEvidence()) return false

        val objectsMatch = requiredObjects.all {
            observation.containsObject(it, objectConfidenceThreshold)
        }
        val observedStates = observation.stateTags.map { it.normalizeLabel() }.toSet()
        val statesMatch = expectedStateTags.all { it in observedStates }
        return objectsMatch && statesMatch
    }

    private fun ProcedureStep.hasVerificationEvidence(): Boolean =
        requiredObjects.isNotEmpty() || expectedStateTags.isNotEmpty()

    private fun buildFailureMessage(
        missingObjects: Set<String>,
        missingStateTags: Set<String>,
    ): String {
        val reasons = buildList {
            if (missingObjects.isNotEmpty()) {
                add("missing: ${missingObjects.sorted().joinToString()}")
            }
            if (missingStateTags.isNotEmpty()) {
                add("state mismatch: ${missingStateTags.sorted().joinToString()}")
            }
        }
        return "Checkpoint not verified — ${reasons.joinToString("; ")}."
    }
}

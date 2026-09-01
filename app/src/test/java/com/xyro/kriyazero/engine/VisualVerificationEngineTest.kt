package com.xyro.kriyazero.engine

import com.xyro.kriyazero.domain.Observation
import com.xyro.kriyazero.domain.ProcedureStep
import com.xyro.kriyazero.domain.SkillCapsule
import com.xyro.kriyazero.domain.VerificationStatus
import com.xyro.kriyazero.domain.VisualFingerprint
import org.junit.Assert.assertEquals
import org.junit.Test

class VisualVerificationEngineTest {
    private val first = fingerprint(u = 128)
    private val second = fingerprint(u = 200)

    private val capsule = SkillCapsule(
        id = "visual-test",
        name = "Visual test",
        sourceNarration = "one two",
        createdAtEpochMs = 1L,
        steps = listOf(
            ProcedureStep(
                id = "step-01",
                order = 0,
                title = "First",
                instruction = "First state",
                requiredObjects = emptySet(),
                expectedStateTags = emptySet(),
                visualFingerprint = first,
            ),
            ProcedureStep(
                id = "step-02",
                order = 1,
                title = "Second",
                instruction = "Second state",
                requiredObjects = emptySet(),
                expectedStateTags = emptySet(),
                visualFingerprint = second,
                dependsOn = setOf("step-01"),
            ),
        ),
    )

    @Test
    fun learnedVisualState_canDriveEntireVerificationWithoutSemanticLabels() {
        val engine = VerificationEngine(capsule)

        assertEquals(
            VerificationStatus.PASS,
            engine.verify(Observation(visualFingerprint = first)).status,
        )
        assertEquals(
            VerificationStatus.COMPLETE,
            engine.verify(Observation(visualFingerprint = second)).status,
        )
    }

    @Test
    fun futureVisualState_isRejectedBeforeCurrentStepCompletes() {
        val engine = VerificationEngine(capsule)

        val decision = engine.verify(Observation(visualFingerprint = second))

        assertEquals(VerificationStatus.SEQUENCE_ERROR, decision.status)
        assertEquals("step-02", decision.matchedFutureStep?.id)
        assertEquals("step-01", engine.currentStep()?.id)
    }

    @Test
    fun globalBrightnessChange_doesNotBreakVisualCheckpoint() {
        val engine = VerificationEngine(capsule)
        val brighter = first.copy(y = first.y.map { it + 25 })

        assertEquals(
            VerificationStatus.PASS,
            engine.verify(Observation(visualFingerprint = brighter)).status,
        )
    }

    private fun fingerprint(u: Int): VisualFingerprint = VisualFingerprint(
        gridSize = 2,
        y = listOf(90, 110, 100, 120),
        u = listOf(u, u, u, u),
        v = listOf(128, 128, 128, 128),
    )
}

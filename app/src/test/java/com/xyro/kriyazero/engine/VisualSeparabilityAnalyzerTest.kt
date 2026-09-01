package com.xyro.kriyazero.engine

import com.xyro.kriyazero.domain.ProcedureStep
import com.xyro.kriyazero.domain.SkillCapsule
import com.xyro.kriyazero.domain.VisualFingerprint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VisualSeparabilityAnalyzerTest {
    @Test
    fun nearlyIdenticalLearnedStates_areFlaggedBeforeDemo() {
        val capsule = capsule(
            fingerprint(u = 128),
            fingerprint(u = 130),
        )

        val report = VisualSeparabilityAnalyzer.analyze(capsule)

        assertTrue(report.hasAmbiguousCheckpoints)
        assertEquals(SeparabilityLevel.AMBIGUOUS, report.checkpoints[0].level)
        assertEquals("step-02", report.checkpoints[0].nearestStepId)
    }

    @Test
    fun clearlyDifferentStates_areAccepted() {
        val capsule = capsule(
            fingerprint(u = 80),
            fingerprint(u = 210),
        )

        val report = VisualSeparabilityAnalyzer.analyze(capsule)

        assertFalse(report.hasAmbiguousCheckpoints)
        assertEquals(SeparabilityLevel.GOOD, report.checkpoints[0].level)
        assertEquals(SeparabilityLevel.GOOD, report.checkpoints[1].level)
    }

    private fun capsule(first: VisualFingerprint, second: VisualFingerprint) = SkillCapsule(
        id = "separability",
        name = "Separability test",
        sourceNarration = "one two",
        createdAtEpochMs = 1L,
        steps = listOf(
            ProcedureStep(
                id = "step-01",
                order = 0,
                title = "One",
                instruction = "One",
                requiredObjects = emptySet(),
                expectedStateTags = emptySet(),
                visualFingerprint = first,
            ),
            ProcedureStep(
                id = "step-02",
                order = 1,
                title = "Two",
                instruction = "Two",
                requiredObjects = emptySet(),
                expectedStateTags = emptySet(),
                visualFingerprint = second,
                dependsOn = setOf("step-01"),
            ),
        ),
    )

    private fun fingerprint(u: Int) = VisualFingerprint(
        gridSize = 2,
        y = listOf(90, 110, 100, 120),
        u = listOf(u, u, u, u),
        v = listOf(128, 128, 128, 128),
    )
}

package com.xyro.kriyazero.engine

import com.xyro.kriyazero.domain.DemonstrationSegment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcedureCompilerTest {
    private val compiler = ProcedureCompiler(Clock { 42L })

    @Test
    fun compile_sortsSegments_normalizesEvidence_andBuildsSequentialDependencies() {
        val capsule = compiler.compile(
            skillName = "  Test Skill  ",
            segments = listOf(
                DemonstrationSegment(
                    index = 8,
                    narration = "Place the LED.",
                    observedObjects = setOf(" LED ", "BreadBoard"),
                    stateTags = setOf(" LED-Oriented "),
                ),
                DemonstrationSegment(
                    index = 2,
                    narration = "Prepare the board.",
                    observedObjects = setOf(" BreadBoard "),
                    stateTags = setOf(" Workspace-Ready "),
                ),
            ),
        )

        assertEquals("Test Skill", capsule.name)
        assertEquals("test-skill-42", capsule.id)
        assertEquals(2, capsule.steps.size)

        val first = capsule.orderedSteps[0]
        val second = capsule.orderedSteps[1]

        assertEquals("step-01", first.id)
        assertTrue(first.dependsOn.isEmpty())
        assertEquals(setOf("breadboard"), first.requiredObjects)
        assertEquals(setOf("workspace-ready"), first.expectedStateTags)

        assertEquals("step-02", second.id)
        assertEquals(setOf("step-01"), second.dependsOn)
        assertEquals(setOf("led", "breadboard"), second.requiredObjects)
        assertEquals(setOf("led-oriented"), second.expectedStateTags)
    }

    @Test(expected = IllegalArgumentException::class)
    fun compile_rejectsDuplicateSegmentIndices() {
        compiler.compile(
            skillName = "Bad demo",
            segments = listOf(
                DemonstrationSegment(0, "One", setOf("board")),
                DemonstrationSegment(0, "Two", setOf("wire")),
            ),
        )
    }
}

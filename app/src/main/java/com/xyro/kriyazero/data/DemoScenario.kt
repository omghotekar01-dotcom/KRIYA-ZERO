package com.xyro.kriyazero.data

import com.xyro.kriyazero.domain.DemonstrationSegment
import com.xyro.kriyazero.domain.Observation
import com.xyro.kriyazero.domain.SkillCapsule
import com.xyro.kriyazero.engine.Clock
import com.xyro.kriyazero.engine.ProcedureCompiler

/**
 * A reproducible tabletop scenario used as a fallback and regression fixture.
 * It keeps the complete Teach -> Compile -> Verify -> Assess story demonstrable
 * even while real perception adapters are being integrated.
 */
object DemoScenario {
    private val demonstration = listOf(
        DemonstrationSegment(
            index = 0,
            narration = "Start with the breadboard clear and the power disconnected.",
            observedObjects = setOf("breadboard"),
            stateTags = setOf("workspace-ready"),
        ),
        DemonstrationSegment(
            index = 1,
            narration = "Place the 220 ohm resistor across the marked rows.",
            observedObjects = setOf("breadboard", "220 ohm resistor"),
            stateTags = setOf("workspace-ready", "resistor-seated"),
        ),
        DemonstrationSegment(
            index = 2,
            narration = "Insert the LED with the correct polarity after the resistor.",
            observedObjects = setOf("breadboard", "220 ohm resistor", "led"),
            stateTags = setOf("workspace-ready", "resistor-seated", "led-oriented"),
        ),
        DemonstrationSegment(
            index = 3,
            narration = "Connect the jumper wire to complete the signal path.",
            observedObjects = setOf("breadboard", "220 ohm resistor", "led", "jumper wire"),
            stateTags = setOf(
                "workspace-ready",
                "resistor-seated",
                "led-oriented",
                "jumper-connected",
            ),
        ),
        DemonstrationSegment(
            index = 4,
            narration = "Connect the battery only after every component is in the verified state.",
            observedObjects = setOf(
                "breadboard",
                "220 ohm resistor",
                "led",
                "jumper wire",
                "battery",
            ),
            stateTags = setOf(
                "workspace-ready",
                "resistor-seated",
                "led-oriented",
                "jumper-connected",
                "circuit-powered",
            ),
        ),
    )

    fun skillCapsule(): SkillCapsule = ProcedureCompiler(
        clock = Clock { 1_788_281_400_000L },
    ).compile(
        skillName = "LED Safety Circuit",
        segments = demonstration,
    )

    fun correctObservationFor(stepIndex: Int): Observation {
        val segment = demonstration[stepIndex.coerceIn(demonstration.indices)]
        return Observation(
            objectConfidence = segment.observedObjects.associateWith { 0.96f },
            stateTags = segment.stateTags,
        )
    }

    fun wrongObservationFor(stepIndex: Int): Observation {
        val safeIndex = stepIndex.coerceIn(demonstration.indices)
        val segment = demonstration[safeIndex]

        return if (safeIndex == 0) {
            Observation(
                objectConfidence = mapOf("battery" to 0.94f),
                stateTags = setOf("circuit-powered"),
            )
        } else {
            val previous = demonstration[safeIndex - 1]
            Observation(
                objectConfidence = previous.observedObjects.associateWith { 0.96f },
                stateTags = previous.stateTags,
            )
        }
    }

    fun futureObservationFor(stepIndex: Int): Observation {
        val futureIndex = (stepIndex + 1).coerceAtMost(demonstration.lastIndex)
        return correctObservationFor(futureIndex)
    }
}

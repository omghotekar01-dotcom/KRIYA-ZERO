package com.xyro.kriyazero.engine

import com.xyro.kriyazero.data.DemoScenario
import com.xyro.kriyazero.domain.VerificationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VerificationEngineTest {
    @Test
    fun correctSequence_passesAndCompletes() {
        val capsule = DemoScenario.skillCapsule()
        val engine = VerificationEngine(capsule)

        capsule.orderedSteps.indices.forEach { index ->
            val decision = engine.verify(DemoScenario.correctObservationFor(index))
            val expected = if (index == capsule.orderedSteps.lastIndex) {
                VerificationStatus.COMPLETE
            } else {
                VerificationStatus.PASS
            }
            assertEquals(expected, decision.status)
        }

        val report = engine.report()
        assertEquals(100, report.completionPercent)
        assertEquals(100, report.firstAttemptAccuracyPercent)
        assertEquals(0, report.failedAttempts)
        assertEquals(0, report.sequenceErrors)
        assertTrue(report.independentlyCompleted)
    }

    @Test
    fun missingEvidence_failsThenCorrectionPasses() {
        val engine = VerificationEngine(DemoScenario.skillCapsule())

        assertEquals(
            VerificationStatus.PASS,
            engine.verify(DemoScenario.correctObservationFor(0)).status,
        )

        val failure = engine.verify(DemoScenario.wrongObservationFor(1))
        assertEquals(VerificationStatus.FAIL, failure.status)
        assertTrue("220 ohm resistor" in failure.missingObjects)

        val corrected = engine.verify(DemoScenario.correctObservationFor(1))
        assertEquals(VerificationStatus.PASS, corrected.status)

        val report = engine.report()
        assertEquals(1, report.failedAttempts)
        assertEquals(1, report.firstAttemptPasses)
    }

    @Test
    fun futureCumulativeState_isRejectedAsSequenceError() {
        val engine = VerificationEngine(DemoScenario.skillCapsule())

        engine.verify(DemoScenario.correctObservationFor(0))

        val decision = engine.verify(DemoScenario.futureObservationFor(1))

        assertEquals(VerificationStatus.SEQUENCE_ERROR, decision.status)
        assertEquals("step-03", decision.matchedFutureStep?.id)
        assertEquals("step-02", engine.currentStep()?.id)
        assertEquals(1, engine.report().sequenceErrors)
    }

    @Test
    fun assistance_isReportedSeparatelyFromVisualFailure() {
        val engine = VerificationEngine(DemoScenario.skillCapsule())
        engine.requestAssistance()
        engine.verify(DemoScenario.correctObservationFor(0))

        val report = engine.report()
        assertEquals(1, report.assistanceCount)
        assertEquals(0, report.failedAttempts)
    }
}

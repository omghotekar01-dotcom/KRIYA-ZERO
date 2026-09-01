package com.xyro.kriyazero.data

import com.xyro.kriyazero.domain.ProcedureStep
import com.xyro.kriyazero.domain.SkillCapsule
import com.xyro.kriyazero.domain.VisualFingerprint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class SkillCapsulePersistenceTest {
    private val capsule = SkillCapsule(
        id = "demo capsule/42",
        name = "Demo Skill",
        sourceNarration = "First do this. Then do that.",
        createdAtEpochMs = 123456L,
        steps = listOf(
            ProcedureStep(
                id = "step-01",
                order = 0,
                title = "Prepare",
                instruction = "Prepare the board",
                requiredObjects = setOf("board"),
                expectedStateTags = setOf("ready"),
                visualFingerprint = VisualFingerprint(
                    gridSize = 2,
                    y = listOf(1, 2, 3, 4),
                    u = listOf(5, 6, 7, 8),
                    v = listOf(9, 10, 11, 12),
                ),
            ),
            ProcedureStep(
                id = "step-02",
                order = 1,
                title = "Finish",
                instruction = "Finish the board",
                requiredObjects = emptySet(),
                expectedStateTags = emptySet(),
                visualFingerprint = VisualFingerprint(
                    gridSize = 2,
                    y = listOf(20, 21, 22, 23),
                    u = listOf(30, 31, 32, 33),
                    v = listOf(40, 41, 42, 43),
                ),
                dependsOn = setOf("step-01"),
            ),
        ),
    )

    @Test
    fun codec_roundTripsCompleteCapsule() {
        val decoded = SkillCapsuleCodec.decode(SkillCapsuleCodec.encode(capsule))
        assertEquals(capsule, decoded)
    }

    @Test
    fun store_savesLoadsListsAndDeletesCapsules() {
        val root = Files.createTempDirectory("kriya-test").toFile()
        try {
            val store = SkillCapsuleStore(root)
            val saved = store.save(capsule)

            assertTrue(saved.isFile)
            assertEquals(capsule, store.loadById(capsule.id))
            assertEquals(listOf(capsule), store.loadAll())

            assertTrue(store.delete(capsule.id))
            assertNull(store.loadById(capsule.id))
        } finally {
            root.deleteRecursively()
        }
    }
}

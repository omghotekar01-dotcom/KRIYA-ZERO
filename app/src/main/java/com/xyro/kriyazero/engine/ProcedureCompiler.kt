package com.xyro.kriyazero.engine

import com.xyro.kriyazero.domain.DemonstrationSegment
import com.xyro.kriyazero.domain.ProcedureStep
import com.xyro.kriyazero.domain.SkillCapsule
import com.xyro.kriyazero.domain.normalizeLabel

fun interface Clock {
    fun nowEpochMs(): Long
}

class ProcedureCompiler(
    private val clock: Clock = Clock { System.currentTimeMillis() },
) {
    fun compile(
        skillName: String,
        segments: List<DemonstrationSegment>,
    ): SkillCapsule {
        require(skillName.isNotBlank()) { "Skill name must not be blank." }
        require(segments.isNotEmpty()) { "At least one demonstration segment is required." }
        require(segments.map { it.index }.distinct().size == segments.size) {
            "Demonstration segment indices must be unique."
        }
        require(
            segments.all {
                it.observedObjects.isNotEmpty() ||
                    it.stateTags.isNotEmpty() ||
                    it.visualFingerprint != null
            },
        ) {
            "Every demonstration checkpoint must contain verifier evidence."
        }

        val ordered = segments.sortedBy { it.index }
        val steps = ordered.mapIndexed { position, segment ->
            val stepId = "step-${(position + 1).toString().padStart(2, '0')}"
            val previousStepId = if (position == 0) null else
                "step-${position.toString().padStart(2, '0')}"

            ProcedureStep(
                id = stepId,
                order = position,
                title = titleFrom(segment.narration, position + 1),
                instruction = segment.narration.trim().ifBlank { "Complete step ${position + 1}" },
                requiredObjects = segment.observedObjects
                    .map { it.normalizeLabel() }
                    .filter { it.isNotBlank() }
                    .toSet(),
                expectedStateTags = segment.stateTags
                    .map { it.normalizeLabel() }
                    .filter { it.isNotBlank() }
                    .toSet(),
                visualFingerprint = segment.visualFingerprint,
                dependsOn = previousStepId?.let(::setOf) ?: emptySet(),
            )
        }

        val createdAt = clock.nowEpochMs()
        return SkillCapsule(
            id = "${slug(skillName)}-$createdAt",
            name = skillName.trim(),
            sourceNarration = ordered.joinToString(" ") { it.narration.trim() }.trim(),
            createdAtEpochMs = createdAt,
            steps = steps,
        )
    }

    private fun titleFrom(narration: String, fallbackIndex: Int): String {
        val words = narration
            .trim()
            .replace(Regex("[^A-Za-z0-9 ]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(5)

        if (words.isEmpty()) return "Step $fallbackIndex"

        return words.joinToString(" ")
            .replaceFirstChar { character -> character.uppercase() }
    }

    private fun slug(value: String): String = value
        .normalizeLabel()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .ifBlank { "skill" }
}

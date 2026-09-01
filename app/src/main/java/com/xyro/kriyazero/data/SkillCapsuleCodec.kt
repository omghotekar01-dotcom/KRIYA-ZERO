package com.xyro.kriyazero.data

import com.xyro.kriyazero.domain.ProcedureStep
import com.xyro.kriyazero.domain.SkillCapsule
import com.xyro.kriyazero.domain.VisualFingerprint
import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Dependency-free, versioned text codec for local Skill Capsule persistence.
 *
 * The format is deliberately simple enough to inspect/debug during a hackathon
 * while avoiding fragile delimiter escaping through URL-safe Base64 strings.
 */
object SkillCapsuleCodec {
    private const val VERSION = "KRIYA1"

    fun encode(capsule: SkillCapsule): String = buildString {
        appendLine(VERSION)
        appendLine("ID|${text(capsule.id)}")
        appendLine("NAME|${text(capsule.name)}")
        appendLine("SOURCE|${text(capsule.sourceNarration)}")
        appendLine("CREATED|${capsule.createdAtEpochMs}")
        capsule.orderedSteps.forEach { step ->
            appendLine(
                listOf(
                    "STEP",
                    text(step.id),
                    step.order.toString(),
                    text(step.title),
                    text(step.instruction),
                    textSet(step.requiredObjects),
                    textSet(step.expectedStateTags),
                    fingerprint(step.visualFingerprint),
                    textSet(step.dependsOn),
                ).joinToString("|"),
            )
        }
    }

    fun decode(encoded: String): SkillCapsule {
        val lines = encoded.lineSequence()
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }
            .toList()
        require(lines.firstOrNull() == VERSION) { "Unsupported Skill Capsule format." }

        var id: String? = null
        var name: String? = null
        var source = ""
        var createdAt: Long? = null
        val steps = mutableListOf<ProcedureStep>()

        lines.drop(1).forEach { line ->
            val parts = line.split('|')
            when (parts.firstOrNull()) {
                "ID" -> id = decodeText(parts.requireField(1, line))
                "NAME" -> name = decodeText(parts.requireField(1, line))
                "SOURCE" -> source = decodeText(parts.requireField(1, line))
                "CREATED" -> createdAt = parts.requireField(1, line).toLong()
                "STEP" -> {
                    require(parts.size == 9) { "Malformed STEP record." }
                    steps += ProcedureStep(
                        id = decodeText(parts[1]),
                        order = parts[2].toInt(),
                        title = decodeText(parts[3]),
                        instruction = decodeText(parts[4]),
                        requiredObjects = decodeTextSet(parts[5]),
                        expectedStateTags = decodeTextSet(parts[6]),
                        visualFingerprint = decodeFingerprint(parts[7]),
                        dependsOn = decodeTextSet(parts[8]),
                    )
                }
            }
        }

        return SkillCapsule(
            id = requireNotNull(id) { "Missing capsule id." },
            name = requireNotNull(name) { "Missing capsule name." },
            sourceNarration = source,
            createdAtEpochMs = requireNotNull(createdAt) { "Missing capsule timestamp." },
            steps = steps,
        )
    }

    private fun text(value: String): String = Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decodeText(value: String): String = String(
        Base64.getUrlDecoder().decode(value),
        StandardCharsets.UTF_8,
    )

    private fun textSet(values: Set<String>): String = values
        .sorted()
        .joinToString(",") { text(it) }

    private fun decodeTextSet(value: String): Set<String> = if (value.isBlank()) {
        emptySet()
    } else {
        value.split(',').map(::decodeText).toSet()
    }

    private fun fingerprint(value: VisualFingerprint?): String {
        if (value == null) return "-"
        return listOf(
            value.gridSize.toString(),
            value.y.joinToString("."),
            value.u.joinToString("."),
            value.v.joinToString("."),
        ).joinToString(";")
    }

    private fun decodeFingerprint(value: String): VisualFingerprint? {
        if (value == "-") return null
        val parts = value.split(';')
        require(parts.size == 4) { "Malformed visual fingerprint." }
        return VisualFingerprint(
            gridSize = parts[0].toInt(),
            y = intList(parts[1]),
            u = intList(parts[2]),
            v = intList(parts[3]),
        )
    }

    private fun intList(value: String): List<Int> =
        if (value.isBlank()) emptyList() else value.split('.').map(String::toInt)

    private fun List<String>.requireField(index: Int, line: String): String =
        getOrNull(index) ?: error("Malformed record: $line")
}

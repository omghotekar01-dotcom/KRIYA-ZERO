package com.xyro.kriyazero.data

import com.xyro.kriyazero.domain.SkillCapsule
import java.io.File

class SkillCapsuleStore(
    rootDirectory: File,
) {
    private val capsuleDirectory = File(rootDirectory, "kriya_capsules")

    fun save(capsule: SkillCapsule): File {
        capsuleDirectory.mkdirs()
        require(capsuleDirectory.isDirectory) { "Unable to create capsule directory." }

        val target = File(capsuleDirectory, "${safeFileName(capsule.id)}.kriya")
        val temp = File(capsuleDirectory, "${target.name}.tmp")
        temp.writeText(SkillCapsuleCodec.encode(capsule))

        if (target.exists() && !target.delete()) {
            temp.delete()
            error("Unable to replace existing Skill Capsule.")
        }

        if (!temp.renameTo(target)) {
            temp.copyTo(target, overwrite = true)
            temp.delete()
        }
        return target
    }

    fun loadById(capsuleId: String): SkillCapsule? {
        val file = File(capsuleDirectory, "${safeFileName(capsuleId)}.kriya")
        if (!file.isFile) return null
        return SkillCapsuleCodec.decode(file.readText())
    }

    fun loadAll(): List<SkillCapsule> {
        if (!capsuleDirectory.isDirectory) return emptyList()
        return capsuleDirectory
            .listFiles { file -> file.isFile && file.extension == "kriya" }
            .orEmpty()
            .sortedByDescending(File::lastModified)
            .mapNotNull { file -> runCatching { SkillCapsuleCodec.decode(file.readText()) }.getOrNull() }
    }

    fun delete(capsuleId: String): Boolean {
        val file = File(capsuleDirectory, "${safeFileName(capsuleId)}.kriya")
        return !file.exists() || file.delete()
    }

    private fun safeFileName(value: String): String = value
        .replace(Regex("[^A-Za-z0-9._-]"), "-")
        .take(120)
        .ifBlank { "capsule" }
}

package com.xyro.kriyazero.data

import com.xyro.kriyazero.domain.SkillCapsule
import java.io.File
import java.security.MessageDigest

class SkillCapsuleStore(
    rootDirectory: File,
) {
    private val capsuleDirectory = File(rootDirectory, "kriya_capsules")

    fun save(capsule: SkillCapsule): File {
        capsuleDirectory.mkdirs()
        require(capsuleDirectory.isDirectory) { "Unable to create capsule directory." }

        val target = capsuleFile(capsule.id)
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
        val preferred = capsuleFile(capsuleId)
        decodeIfMatching(preferred, capsuleId)?.let { return it }

        // Backward-compatible lookup for capsules persisted before hashed filenames were introduced.
        return decodeIfMatching(legacyCapsuleFile(capsuleId), capsuleId)
    }

    fun loadAll(): List<SkillCapsule> {
        if (!capsuleDirectory.isDirectory) return emptyList()
        return capsuleDirectory
            .listFiles { file -> file.isFile && file.extension == "kriya" }
            .orEmpty()
            .sortedByDescending(File::lastModified)
            .mapNotNull { file -> runCatching { SkillCapsuleCodec.decode(file.readText()) }.getOrNull() }
            .distinctBy(SkillCapsule::id)
    }

    fun delete(capsuleId: String): Boolean {
        val preferred = capsuleFile(capsuleId)
        val legacy = legacyCapsuleFile(capsuleId)
        val preferredDeleted = !preferred.exists() || preferred.delete()
        val legacyDeleted = legacy == preferred || !legacy.exists() || legacy.delete()
        return preferredDeleted && legacyDeleted
    }

    private fun capsuleFile(capsuleId: String): File = File(
        capsuleDirectory,
        "${safeFileName(capsuleId)}-${stableIdSuffix(capsuleId)}.kriya",
    )

    private fun legacyCapsuleFile(capsuleId: String): File =
        File(capsuleDirectory, "${safeFileName(capsuleId)}.kriya")

    private fun decodeIfMatching(file: File, capsuleId: String): SkillCapsule? {
        if (!file.isFile) return null
        val capsule = runCatching { SkillCapsuleCodec.decode(file.readText()) }.getOrNull() ?: return null
        return capsule.takeIf { it.id == capsuleId }
    }

    private fun safeFileName(value: String): String = value
        .replace(Regex("[^A-Za-z0-9._-]"), "-")
        .take(96)
        .ifBlank { "capsule" }

    private fun stableIdSuffix(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return digest.take(8).joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }
}

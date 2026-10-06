package com.keepr.utils

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.keepr.data.model.VaultEntry
import com.keepr.security.CryptoManager
import com.keepr.security.EncryptedData
import android.util.Base64

data class BackupFile(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val iv: String,
    val data: String
)

data class BackupData(
    val entries: List<BackupEntry>
)

data class BackupEntry(
    val id: String,
    val serviceName: String,
    val accountLabel: String,
    val username: String,
    val email: String,
    val passwordEncrypted: String,
    val website: String,
    val category: String,
    val notes: String,
    val isFavorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

class BackupManager(
    private val context: Context,
    private val cryptoManager: CryptoManager
) {

    companion object {
        private const val MAX_BACKUP_BYTES = 10 * 1024 * 1024
        private const val MAX_ENTRIES = 10_000
        private const val MAX_FIELD_LENGTH = 100_000
    }

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun createBackup(entries: List<VaultEntry>): ByteArray {
        val backupData = BackupData(
            entries = entries.map { entry ->
                BackupEntry(
                    id = entry.id,
                    serviceName = entry.serviceName,
                    accountLabel = entry.accountLabel,
                    username = entry.username,
                    email = entry.email,
                    passwordEncrypted = entry.passwordEncrypted,
                    website = entry.website,
                    category = entry.category,
                    notes = entry.notes,
                    isFavorite = entry.isFavorite,
                    createdAt = entry.createdAt,
                    updatedAt = entry.updatedAt
                )
            }
        )

        val jsonBytes = gson.toJson(backupData).toByteArray(Charsets.UTF_8)
        val key = cryptoManager.getOrCreateVaultKey()
        val encrypted = cryptoManager.encrypt(jsonBytes, key)

        val backupFile = BackupFile(
            iv = Base64.encodeToString(encrypted.iv, Base64.NO_WRAP),
            data = Base64.encodeToString(encrypted.ciphertext, Base64.NO_WRAP)
        )

        return gson.toJson(backupFile).toByteArray(Charsets.UTF_8)
    }

    fun writeBackupToUri(uri: Uri, data: ByteArray) {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(data)
        } ?: throw Exception("Cannot open output stream for backup")
    }

    fun restoreBackup(uri: Uri): Result<List<VaultEntry>> {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes()
            } ?: return Result.failure(Exception("Cannot read backup file"))

            require(bytes.size <= MAX_BACKUP_BYTES) { "Backup file is too large" }
            val content = bytes.toString(Charsets.UTF_8)

            val backupFile = gson.fromJson(content, BackupFile::class.java)
                ?: return Result.failure(Exception("Invalid backup format"))

            if (backupFile.version != 1) {
                return Result.failure(Exception("Unsupported backup version"))
            }

            val iv = Base64.decode(backupFile.iv, Base64.NO_WRAP)
            val ciphertext = Base64.decode(backupFile.data, Base64.NO_WRAP)
            require(iv.size == 12) { "Invalid backup IV" }
            require(ciphertext.size >= 16) { "Invalid backup payload" }

            val key = cryptoManager.getOrCreateVaultKey()
            val decrypted = cryptoManager.decrypt(EncryptedData(iv, ciphertext), key)
            val json = decrypted.toString(Charsets.UTF_8)

            val backupData = gson.fromJson(json, BackupData::class.java)
                ?: return Result.failure(Exception("Corrupted backup data"))

            require(backupData.entries.size <= MAX_ENTRIES) { "Backup contains too many entries" }

            val entries = backupData.entries.map { entry ->
                validateEntry(entry)
                VaultEntry(
                    id = entry.id,
                    serviceName = entry.serviceName,
                    accountLabel = entry.accountLabel,
                    username = entry.username,
                    email = entry.email,
                    passwordEncrypted = entry.passwordEncrypted,
                    website = entry.website,
                    category = entry.category,
                    notes = entry.notes,
                    isFavorite = entry.isFavorite,
                    createdAt = entry.createdAt,
                    updatedAt = entry.updatedAt
                )
            }

            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to restore backup: ${e.message}"))
        }
    }
    private fun validateEntry(entry: BackupEntry) {
        val fields = listOf(
            entry.id, entry.serviceName, entry.accountLabel, entry.username,
            entry.email, entry.passwordEncrypted, entry.website, entry.category, entry.notes
        )
        require(fields.all { it.length <= MAX_FIELD_LENGTH }) { "Backup contains an oversized field" }
    }

}

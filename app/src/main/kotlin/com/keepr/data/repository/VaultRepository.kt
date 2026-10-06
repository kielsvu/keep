package com.keepr.data.repository

import com.keepr.data.database.VaultDao
import com.keepr.data.model.VaultEntry
import com.keepr.security.VaultEncryption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class VaultRepository(
    private val vaultDao: VaultDao,
    private val vaultEncryption: VaultEncryption
) {

    fun observeEntries(): Flow<List<VaultEntry>> =
        vaultDao.getAllEntries()
            .map { entries -> entries.map(::decryptEntry) }
            .flowOn(Dispatchers.Default)

    suspend fun getEntryById(id: String): VaultEntry? =
        vaultDao.getEntryById(id)?.let { raw ->
            withContext(Dispatchers.Default) { decryptEntry(raw) }
        }

    suspend fun insertEntry(entry: VaultEntry) {
        val encrypted = withContext(Dispatchers.Default) { encryptEntry(entry) }
        vaultDao.insertEntry(encrypted)
    }

    suspend fun updateEntry(entry: VaultEntry) {
        val encrypted = withContext(Dispatchers.Default) {
            encryptEntry(entry.copy(updatedAt = System.currentTimeMillis()))
        }
        vaultDao.updateEntry(encrypted)
    }

    suspend fun deleteEntry(entry: VaultEntry) {
        vaultDao.deleteEntry(entry)
    }

    suspend fun deleteAllEntries() {
        vaultDao.deleteAllEntries()
    }

    suspend fun getEntryCount(): Int = vaultDao.getEntryCount()

    suspend fun getRawEntries(): List<VaultEntry> = vaultDao.getAllEntriesOnce()

    suspend fun importEntries(entries: List<VaultEntry>) {
        val encrypted = withContext(Dispatchers.Default) { entries.map(::encryptEntry) }
        vaultDao.insertAll(encrypted)
    }

    private fun encryptEntry(entry: VaultEntry): VaultEntry = entry.copy(
        username = if (entry.username.isEmpty()) "" else vaultEncryption.encryptField(entry.username),
        email = if (entry.email.isEmpty()) "" else vaultEncryption.encryptField(entry.email),
        passwordEncrypted = if (entry.passwordEncrypted.isEmpty()) "" else vaultEncryption.encryptField(entry.passwordEncrypted),
        website = if (entry.website.isEmpty()) "" else vaultEncryption.encryptField(entry.website),
        notes = if (entry.notes.isEmpty()) "" else vaultEncryption.encryptField(entry.notes)
    )

    private fun decryptEntry(entry: VaultEntry): VaultEntry = entry.copy(
        username = if (entry.username.isEmpty()) "" else vaultEncryption.decryptField(entry.username),
        email = if (entry.email.isEmpty()) "" else vaultEncryption.decryptField(entry.email),
        passwordEncrypted = if (entry.passwordEncrypted.isEmpty()) "" else vaultEncryption.decryptField(entry.passwordEncrypted),
        website = if (entry.website.isEmpty()) "" else vaultEncryption.decryptField(entry.website),
        notes = if (entry.notes.isEmpty()) "" else vaultEncryption.decryptField(entry.notes)
    )
}

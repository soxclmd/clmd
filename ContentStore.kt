package ph.gov.deped.region12.soxclmd.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File

private val Context.dataStore by preferencesDataStore(name = "soxclmd_prefs")

/**
 * Local content cache + sync metadata.
 *
 * The cached JSON files live in filesDir/content and are replaced atomically.
 * Preferences keep the last-synced manifest version and per-file hashes so
 * unchanged feeds are never downloaded again.
 */
class ContentStore(private val context: Context) {

    data class SyncState(
        val contentVersion: String = "",
        val lastUpdated: String = "",
        val lastSyncMillis: Long = 0,
        val fileHashes: Map<String, String> = emptyMap()
    )

    private val dir: File
        get() = File(context.filesDir, "content").apply { mkdirs() }

    fun fileFor(key: String): File = File(dir, "$key.json")

    fun readCached(key: String): String? =
        fileFor(key).takeIf { it.exists() }?.readText()

    fun writeCache(key: String, text: String) {
        val tmp = File(dir, "$key.tmp")
        tmp.writeText(text)
        if (!tmp.renameTo(fileFor(key))) {
            fileFor(key).delete()
            tmp.renameTo(fileFor(key))
        }
    }

    val syncFlow = context.dataStore.data.map { p ->
        SyncState(
            contentVersion = p[KEY_VERSION] ?: "",
            lastUpdated = p[KEY_UPDATED] ?: "",
            lastSyncMillis = p[KEY_SYNC_MILLIS]?.toLongOrNull() ?: 0L,
            fileHashes = p.asMap().entries
                .filter { it.key.name.startsWith("hash_") }
                .associate { it.key.name.removePrefix("hash_") to (it.value as? String ?: "") }
        )
    }

    suspend fun currentSyncState(): SyncState = syncFlow.first()

    suspend fun saveSyncState(version: String, updated: String, hashes: Map<String, String>) {
        context.dataStore.edit { p ->
            p[KEY_VERSION] = version
            p[KEY_UPDATED] = updated
            p[KEY_SYNC_MILLIS] = System.currentTimeMillis().toString()
            val names = hashes.keys.map { "hash_$it" }.toSet()
            p.asMap().keys.filter { it.name.startsWith("hash_") && it.name !in names }
                .forEach { p.remove(it) }
            hashes.forEach { (k, v) -> p[stringPreferencesKey("hash_$k")] = v }
        }
    }

    /* "seen" snapshot ids for the in-app update feed */
    suspend fun saveSeenIds(ids: Set<String>) {
        context.dataStore.edit { p -> p[KEY_SEEN] = ids.joinToString("|") }
    }

    suspend fun seenIds(): Set<String> =
        context.dataStore.data.map { p -> (p[KEY_SEEN] ?: "").split('|').filter { it.isNotBlank() }.toSet() }.first()

    companion object {
        private val KEY_VERSION = stringPreferencesKey("content_version")
        private val KEY_UPDATED = stringPreferencesKey("content_updated")
        private val KEY_SYNC_MILLIS = stringPreferencesKey("last_sync_millis")
        private val KEY_SEEN = stringPreferencesKey("seen_ids")
    }
}

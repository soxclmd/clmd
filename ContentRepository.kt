package ph.gov.deped.region12.soxclmd.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ph.gov.deped.region12.soxclmd.data.model.*

/**
 * Offline-first content repository.
 *
 * UI ALWAYS reads from the local cache (fast, works with no connection);
 * synchronization with the GitHub-Powered content layer happens in the
 * background (app open, return from background, manual refresh, periodic
 * WorkManager job, or when connectivity returns).
 */
class ContentRepository(
    private val context: Context,
    val api: ContentApi,
    private val store: ContentStore
) {
    companion object {
        const val BASE_URL = "https://soxclmd.github.io/clmd/"
        const val MANIFEST_PATH = "assets/data/manifest.json"

        // manifest key -> relative path of the feed
        val FEEDS: Map<String, String> = mapOf(
            "news" to "assets/data/news.json",
            "events" to "assets/data/events.json",
            "memoranda" to "assets/data/advisories.json",
            "learningAreas" to "assets/data/learning-areas.json",
            "resources" to "assets/data/downloads.json",
            "site" to "assets/data/site.json",
            "searchIndex" to "assets/data/search-index.json"
        )
    }

    sealed interface SyncStatus {
        data object Idle : SyncStatus
        data object Checking : SyncStatus
        data class Downloading(val done: Int, val total: Int) : SyncStatus
        data class Done(val changed: Boolean, val at: Long) : SyncStatus
        data class Offline(val lastSuccess: Long) : SyncStatus
        data class Failed(val message: String) : SyncStatus
    }

    data class UpdateHighlights(
        val newMemoranda: List<String> = emptyList(),
        val newAnnouncements: List<String> = emptyList(),
        val newNews: List<String> = emptyList(),
        val newEvents: List<String> = emptyList()
    ) {
        val total get() = newMemoranda.size + newAnnouncements.size + newNews.size + newEvents.size
    }

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _manifest = MutableStateFlow<ContentManifest?>(null)
    val manifest: StateFlow<ContentManifest?> = _manifest.asStateFlow()

    private val _news = MutableStateFlow(NewsFeed())
    val news: StateFlow<NewsFeed> = _news.asStateFlow()

    private val _events = MutableStateFlow(EventsFeed())
    val events: StateFlow<EventsFeed> = _events.asStateFlow()

    private val _memoranda = MutableStateFlow<List<Memorandum>>(emptyList())
    val memoranda: StateFlow<List<Memorandum>> = _memoranda.asStateFlow()

    private val _learningAreas = MutableStateFlow<List<LearningArea>>(emptyList())
    val learningAreas: StateFlow<List<LearningArea>> = _learningAreas.asStateFlow()

    private val _resources = MutableStateFlow<List<Resource>>(emptyList())
    val resources: StateFlow<List<Resource>> = _resources.asStateFlow()

    private val _site = MutableStateFlow(SiteInfo())
    val site: StateFlow<SiteInfo> = _site.asStateFlow()

    private val _searchIndex = MutableStateFlow<List<SearchEntry>>(emptyList())
    val searchIndex: StateFlow<List<SearchEntry>> = _searchIndex.asStateFlow()

    private val _highlights = MutableStateFlow(UpdateHighlights())
    val highlights: StateFlow<UpdateHighlights> = _highlights.asStateFlow()

    val json: Json get() = api.json

    init {
        loadFromCache()
    }

    /* ---------------- cache -> state ---------------- */

    private fun loadFromCache() {
        runCatching {
            store.readCached("site")?.let { _site.value = json.decodeFromString<SiteInfo>(it) }
            store.readCached("news")?.let { _news.value = json.decodeFromString<NewsFeed>(it) }
            store.readCached("events")?.let { _events.value = json.decodeFromString<EventsFeed>(it) }
            store.readCached("learningAreas")?.let { _learningAreas.value = json.decodeFromString<List<LearningArea>>(it) }
            store.readCached("resources")?.let { _resources.value = json.decodeFromString<List<Resource>>(it) }
            store.readCached("memoranda")?.let { _memoranda.value = json.decodeFromString<List<Memorandum>>(it) }
            store.readCached("searchIndex")?.let { _searchIndex.value = json.decodeFromString<List<SearchEntry>>(it) }
        }.onFailure { Log.w("ContentRepository", "cache load issue: ${it.message}") }
    }

    private fun parseAndPublish(key: String, text: String) {
        when (key) {
            "site" -> _site.value = json.decodeFromString<SiteInfo>(text)
            "news" -> _news.value = json.decodeFromString<NewsFeed>(text)
            "events" -> _events.value = json.decodeFromString<EventsFeed>(text)
            "learningAreas" -> _learningAreas.value = json.decodeFromString<List<LearningArea>>(text)
            "resources" -> _resources.value = json.decodeFromString<List<Resource>>(text)
            "memoranda" -> _memoranda.value = json.decodeFromString<List<Memorandum>>(text)
            "searchIndex" -> _searchIndex.value = json.decodeFromString<List<SearchEntry>>(text)
        }
    }

    /* ---------------- synchronization ---------------- */

    /** Downloads the manifest and any changed feeds. Safe to call repeatedly. */
    suspend fun sync(force: Boolean = false) {
        val cached = store.currentSyncState()
        _syncStatus.value = SyncStatus.Checking
        val manifest = try {
            json.decodeFromString<ContentManifest>(api.fetchText(MANIFEST_PATH, noCache = true))
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Offline(cached.lastSyncMillis)
            return
        }
        _manifest.value = manifest

        if (!force && manifest.content_version == cached.contentVersion) {
            _syncStatus.value = SyncStatus.Done(changed = false, at = System.currentTimeMillis())
            return
        }

        val targets = FEEDS.filter { (key, path) ->
            val want = manifest.files[key]?.sha256
            want != null && (force || want != cached.fileHashes[key])
        }
        var done = 0
        val hashes = HashMap<String, String>(cached.fileHashes)
        for ((key, path) in targets) {
            _syncStatus.value = SyncStatus.Downloading(done, targets.size)
            try {
                val bytes = api.fetchBytes(path)
                store.writeCache(key, String(bytes, Charsets.UTF_8))
                hashes[key] = ContentApi.sha256(bytes)
                parseAndPublish(key, String(bytes, Charsets.UTF_8))
            } catch (e: Exception) {
                Log.w("ContentRepository", "feed $key failed: ${e.message}")
            }
            done++
        }
        // feeds absent from the manifest (or failed) keep their old hash entries
        FEEDS.keys.forEach { key ->
            manifest.files[key]?.sha256?.let { hashes[key] = it }
        }

        computeHighlights()
        store.saveSyncState(manifest.content_version, manifest.last_updated, hashes)
        _syncStatus.value = SyncStatus.Done(changed = targets.isNotEmpty(), at = System.currentTimeMillis())
    }

    private fun currentIds(): Set<String> = buildSet {
        _memoranda.value.forEach { add("memo:${it.number}") }
        _site.value.announcements.forEach { add("ann:${it.title}") }
        _news.value.items.forEach { add("news:${it.id}") }
        _events.value.items.forEach { add("ev:${it.title}:${it.startDate}") }
    }

    private suspend fun computeHighlights() {
        val current = currentIds()
        val seen = store.seenIds()
        // First ever sync: record everything as seen so the update feed starts clean.
        if (seen.isEmpty() && store.currentSyncState().lastSyncMillis == 0L) {
            store.saveSeenIds(current)
            _highlights.value = UpdateHighlights()
            return
        }
        val newly = current - seen
        _highlights.value = UpdateHighlights(
            newMemoranda = newly.filter { it.startsWith("memo:") }.map { it.removePrefix("memo:") },
            newAnnouncements = newly.filter { it.startsWith("ann:") }.map { it.removePrefix("ann:") },
            newNews = newly.filter { it.startsWith("news:") }.map { it.removePrefix("news:") },
            newEvents = newly.filter { it.startsWith("ev:") }.map { it.removePrefix("ev:").substringBefore(':') }
        )
        if (newly.isNotEmpty()) store.saveSeenIds(current)
    }

    fun absoluteUrl(path: String): String = api.absolute(path)
}

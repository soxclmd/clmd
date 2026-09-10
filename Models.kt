package ph.gov.deped.region12.soxclmd.data.model

import kotlinx.serialization.Serializable

/* ------------------------------------------------------------------ */
/* Content manifest — the app's entry point for change detection      */
/* ------------------------------------------------------------------ */

@Serializable
data class ContentManifest(
    val content_version: String = "",
    val last_updated: String = "",
    val minimum_app_version: String = "1.0.0",
    val maintenance_mode: Boolean = false,
    val files: Map<String, ManifestFile> = emptyMap()
)

@Serializable
data class ManifestFile(
    val path: String = "",
    val sha256: String = "",
    val bytes: Long = 0
)

/* ------------------------------------------------------------------ */
/* News (from assets/data/news.json)                                  */
/* ------------------------------------------------------------------ */

@Serializable
data class NewsFeed(
    val version: String = "1",
    val items: List<NewsItem> = emptyList()
)

@Serializable
data class NewsItem(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val date: String = "",
    val tags: String = "",
    val summary: String = "",
    val body: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val imageAlts: List<String> = emptyList()
)

/* ------------------------------------------------------------------ */
/* Activities / calendar events (from assets/data/events.json)        */
/* ------------------------------------------------------------------ */

@Serializable
data class EventsFeed(
    val version: String = "1",
    val items: List<CalendarEvent> = emptyList()
)

@Serializable
data class CalendarEvent(
    val startDate: String = "",
    val endDate: String = "",
    val time: String? = null,
    val title: String = "",
    val venue: String = "",
    val audience: String = "",
    val memoNumbers: List<String> = emptyList()
)

/* ------------------------------------------------------------------ */
/* Memoranda / advisories (from assets/data/advisories.json)          */
/* ------------------------------------------------------------------ */

@Serializable
data class Memorandum(
    val date: String = "",
    val title: String = "",
    val number: String = "",
    val year: Int? = null,
    val file: String = "",
    val documentType: String = "Regional Memorandum",
    val official: Boolean = true,
    val sizeBytes: Long? = null,
    val pages: Int? = null,
    val learningArea: String? = null,
    val program: String? = null,
    val driveId: String? = null
)

/* ------------------------------------------------------------------ */
/* Learning areas (from assets/data/learning-areas.json)              */
/* ------------------------------------------------------------------ */

@Serializable
data class LearningArea(
    val slug: String = "",
    val title: String = "",
    val icon: String = "",
    val supervisor: String = "",
    val position: String = "",
    val email: String = "",
    val image: String = "",
    val overview: String = "",
    val short: String = "",
    val objectives: List<String> = emptyList(),
    val responsibilities: List<String> = emptyList(),
    val programsHandled: List<String> = emptyList(),
    val projects: List<String> = emptyList(),
    val bestPractices: List<String> = emptyList(),
    val resourceTags: List<String> = emptyList()
)

/* ------------------------------------------------------------------ */
/* Resource center / downloads (from assets/data/downloads.json)      */
/* ------------------------------------------------------------------ */

@Serializable
data class Resource(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val year: Int? = null,
    val type: String = "",
    val size: String = "",
    val file: String = "",
    val tags: List<String> = emptyList(),
    val date: String = ""
)

/* ------------------------------------------------------------------ */
/* Site info: about, contact, leaders, announcements                   */
/* (from assets/data/site.json)                                       */
/* ------------------------------------------------------------------ */

@Serializable
data class SiteInfo(
    val name: String = "",
    val shortName: String = "",
    val agency: String = "",
    val region: String = "",
    val tagline: String = "",
    val vision: String = "",
    val mission: String = "",
    val contact: Contact = Contact(),
    val coreValues: List<CoreValue> = emptyList(),
    val leaders: List<Leader> = emptyList(),
    val stats: List<Stat> = emptyList(),
    val announcements: List<Announcement> = emptyList()
)

@Serializable
data class Contact(
    val email: String = "",
    val clmdEmail: String = "",
    val telephone: String = "",
    val clmdTelephone: String = "",
    val address: String = "",
    val facebook: String = "",
    val website: String = ""
)

@Serializable
data class CoreValue(
    val title: String = "",
    val icon: String = "",
    val text: String = ""
)

@Serializable
data class Leader(
    val name: String = "",
    val position: String = "",
    val image: String = "",
    val message: String = ""
)

@Serializable
data class Stat(
    val label: String = "",
    val value: Int = 0,
    val suffix: String = "",
    val icon: String = ""
)

@Serializable
data class Announcement(
    val date: String = "",
    val tag: String = "",
    val title: String = "",
    val text: String = "",
    val memoNumber: String? = null,
    val file: String? = null
)

/* ------------------------------------------------------------------ */
/* Global search (from assets/data/search-index.json)                 */
/* ------------------------------------------------------------------ */

@Serializable
data class SearchEntry(
    val title: String = "",
    val url: String = "",
    val type: String = "",
    val keywords: String = ""
)

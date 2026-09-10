package ph.gov.deped.region12.soxclmd.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import ph.gov.deped.region12.soxclmd.data.ContentRepository
import ph.gov.deped.region12.soxclmd.data.model.NewsItem
import ph.gov.deped.region12.soxclmd.ui.AppViewModel
import ph.gov.deped.region12.soxclmd.ui.components.NewPill
import ph.gov.deped.region12.soxclmd.ui.components.SyncBanner
import ph.gov.deped.region12.soxclmd.ui.components.TagChip
import ph.gov.deped.region12.soxclmd.ui.theme.*
import ph.gov.deped.region12.soxclmd.util.TimeUtils
import java.time.LocalDate

@Composable
fun HomeScreen(vm: AppViewModel, nav: NavHostController) {
    val context = LocalContext.current
    val site by vm.site.collectAsState()
    val news by vm.news.collectAsState()
    val events by vm.events.collectAsState()
    val sync by vm.syncStatus.collectAsState()
    val highlights by vm.highlights.collectAsState()
    val memoranda by vm.memoranda.collectAsState()

    val today = LocalDate.now().toString()
    val upcoming = events.items
        .filter { (it.endDate.ifBlank { it.startDate }) >= today }
        .sortedBy { it.startDate }
        .take(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        /* ---------- masthead ---------- */
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(BrandPrimaryDark)
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = vm.absoluteUrl("assets/images/deped-region-xii-logo.jpg"),
                        contentDescription = "DepEd Region XII seal",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color.White)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("SOXCLMD", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text(
                            "Curriculum and Learning Management Division",
                            color = Color(0xE6FFFFFF), fontSize = 12.sp
                        )
                        Text("DepEd Regional Office XII", color = BrandGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { vm.refresh(force = true) }) {
                        Icon(Icons.Filled.Sync, contentDescription = "Sync now", tint = Color.White)
                    }
                    IconButton(onClick = { nav.navigate("settings") }) {
                        BadgedBox(badge = {
                            if (highlights.total > 0) Badge { Text("${highlights.total}") }
                        }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Updates", tint = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    site.tagline.ifBlank { "Connecting Curriculum, Learning, and Innovation." },
                    color = Color(0xCCFFFFFF), fontSize = 12.sp
                )
            }
        }

        /* ---------- sync banner ---------- */
        item { SyncBanner(sync) }

        /* ---------- welcome + stats ---------- */
        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text(
                    "Welcome to the official CLMD Region XII mobile companion.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    site.stats.take(3).forEach { stat ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${stat.value}${stat.suffix}",
                                    color = BrandPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp
                                )
                                Text(
                                    stat.label, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        /* ---------- update highlights ---------- */
        if (highlights.total > 0) item {
            Surface(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clickable { nav.navigate("resources") },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("What's new", fontWeight = FontWeight.Black, color = BrandPrimary)
                    Spacer(Modifier.height(4.dp))
                    highlights.newMemoranda.take(3).forEach {
                        Text("📄 New memorandum: $it", style = MaterialTheme.typography.bodyMedium)
                    }
                    highlights.newAnnouncements.take(2).forEach {
                        Text("📣 $it", style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    }
                    highlights.newNews.take(2).forEach {
                        Text("📰 $it", style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        /* ---------- quick access ---------- */
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                QuickTitle("Quick Access")
                Spacer(Modifier.height(10.dp))
                val quick = listOf(
                    Quick("Learning Areas", Icons.Filled.School) { nav.navigate("more") },
                    Quick("MAPEH Hub", Icons.Filled.SportsKabaddi) { nav.navigate("mapeh") },
                    Quick("Resources", Icons.Filled.GridView) { nav.navigate("resources") },
                    Quick("Announcements", Icons.Filled.Campaign) { nav.navigate("news") },
                    Quick("Calendar", Icons.Filled.Event) { nav.navigate("calendar") },
                    Quick("Website", Icons.Filled.Language) {
                        context.startActivity(
                            android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(ContentRepository.BASE_URL)
                            )
                        )
                    }
                )
                quick.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { q ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { q.onClick() }
                            ) {
                                Column(
                                    Modifier.padding(vertical = 14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(q.icon, contentDescription = q.label, tint = BrandPrimary, modifier = Modifier.size(26.dp))
                                    Spacer(Modifier.height(6.dp))
                                    Text(q.label, style = MaterialTheme.typography.labelLarge, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        /* ---------- top stories ---------- */
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                QuickTitle("Top Stories")
                Spacer(Modifier.height(10.dp))
            }
        }
        items(news.items.take(4), key = { "news-${it.id}" }) { item ->
            NewsCard(item = item, resolve = { vm.absoluteUrl(it) }, onClick = { nav.navigate("article/${item.id}") })
        }

        /* ---------- upcoming activities ---------- */
        item {
            Column(Modifier.padding(16.dp)) {
                QuickTitle("Upcoming Activities")
                Spacer(Modifier.height(8.dp))
                if (upcoming.isEmpty()) {
                    Text(
                        "No upcoming activities are currently listed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                upcoming.forEach { ev ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(
                                Modifier
                                    .background(BrandSoft, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val d = TimeUtils.parseDate(ev.startDate)
                                Text(
                                    d?.dayOfMonth?.toString() ?: "–",
                                    color = BrandPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp
                                )
                                Text(
                                    d?.month?.name?.take(3)?.uppercase() ?: "",
                                    color = BrandPrimary, style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    TimeUtils.statusOf(ev.startDate, ev.endDate),
                                    color = if (TimeUtils.statusOf(ev.startDate, ev.endDate) == "Ongoing") OngoingGreen else BrandSecondary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(ev.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                                Text(
                                    listOfNotNull(ev.venue.ifBlank { null }, ev.time).joinToString(" · "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = { nav.navigate("activities") }) {
                    Text("View all activities")
                }
                TextButton(onClick = { nav.navigate("resources") }) {
                    Text("Browse ${memoranda.size} memoranda")
                }
            }
        }
    }
}

@Composable
private fun QuickTitle(text: String) {
    Column {
        Text(text, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Box(Modifier.width(38.dp).height(3.dp).background(BrandGold, RoundedCornerShape(3.dp)))
    }
}

@Composable
fun NewsCard(item: NewsItem, resolve: (String) -> String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() }
    ) {
        Column {
            if (item.images.isNotEmpty()) {
                AsyncImage(
                    model = resolve(item.images.first()),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TagChip(item.category)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        TimeUtils.parseDate(item.date)?.format(TimeUtils.display) ?: item.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    item.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
            }
        }
    }
}

private data class Quick(val label: String, val icon: ImageVector, val onClick: () -> Unit)

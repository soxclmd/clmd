package ph.gov.deped.region12.soxclmd.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ph.gov.deped.region12.soxclmd.ui.AppViewModel
import ph.gov.deped.region12.soxclmd.ui.components.EmptyState
import ph.gov.deped.region12.soxclmd.ui.components.ModuleHeader
import ph.gov.deped.region12.soxclmd.ui.components.SyncBanner
import ph.gov.deped.region12.soxclmd.ui.components.TagChip
import ph.gov.deped.region12.soxclmd.util.Downloads
import androidx.compose.ui.platform.LocalContext

@Composable
fun NewsScreen(vm: AppViewModel, nav: NavHostController) {
    val context = LocalContext.current
    val news by vm.news.collectAsState()
    val site by vm.site.collectAsState()
    val sync by vm.syncStatus.collectAsState()
    var query by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        ModuleHeader("News & Announcements", "Official CLMD Region XII stories and announcements")
        SyncBanner(sync)

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search news") },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge
        )

        val announcements = site.announcements.filter {
            query.isBlank() || it.title.contains(query, ignoreCase = true) || it.tag.contains(query, ignoreCase = true)
        }
        val stories = news.items.filter {
            query.isBlank() || it.title.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) || it.tags.contains(query, ignoreCase = true)
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 20.dp)) {
            if (announcements.isNotEmpty()) {
                item {
                    Text(
                        "Announcements",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(announcements, key = { "ann-${it.title}" }) { a ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row {
                                Icon(Icons.Filled.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    a.date, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(a.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                a.text, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!a.file.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = {
                                    Downloads.download(context, vm.absoluteUrl(a.file!!), "memorandum.pdf", a.title)
                                }) { Text("Open attachment · ${a.memoNumber ?: ""}") }
                            }
                        }
                    }
                }
            }
            item {
                Text(
                    "Latest Stories",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            if (stories.isEmpty() && announcements.isEmpty()) {
                item { EmptyState("No stories match your search.") }
            }
            items(stories, key = { it.id }) { item ->
                NewsCard(item = item, resolve = { vm.absoluteUrl(it) }, onClick = { nav.navigate("article/${item.id}") })
            }
        }
    }
}

@Composable
fun NewsDetailScreen(vm: AppViewModel, id: String) {
    val news by vm.news.collectAsState()
    val item = news.items.firstOrNull { it.id == id }

    if (item == null) {
        Column(Modifier.fillMaxSize()) {
            ModuleHeader("Story", "")
            EmptyState("This story is not available yet. Pull the latest content from Settings → Sync now.")
        }
        return
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp)) {
        item {
            if (item.images.isNotEmpty()) {
                coil.compose.AsyncImage(
                    model = vm.absoluteUrl(item.images.first()),
                    contentDescription = item.title,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(230.dp)
                )
            }
        }
        item {
            Column(Modifier.padding(18.dp)) {
                Row {
                    TagChip(item.category)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        ph.gov.deped.region12.soxclmd.util.TimeUtils.parseDate(item.date)
                            ?.format(ph.gov.deped.region12.soxclmd.util.TimeUtils.display) ?: item.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(item.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(10.dp))
                Text(
                    item.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                item.body.forEach { paragraph ->
                    if (paragraph.isNotBlank()) {
                        Text(
                            paragraph,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }
        }
        if (item.images.size > 1) {
            item {
                Text(
                    "Photographs",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
            items(item.images.drop(1), key = { it }) { img ->
                coil.compose.AsyncImage(
                    model = vm.absoluteUrl(img),
                    contentDescription = "Story photograph",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 5.dp)
                        .height(200.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

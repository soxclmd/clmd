package ph.gov.deped.region12.soxclmd.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ph.gov.deped.region12.soxclmd.ui.AppViewModel
import ph.gov.deped.region12.soxclmd.ui.components.EmptyState
import ph.gov.deped.region12.soxclmd.ui.components.ModuleHeader
import ph.gov.deped.region12.soxclmd.ui.components.SyncBanner
import ph.gov.deped.region12.soxclmd.ui.components.TagChip
import ph.gov.deped.region12.soxclmd.util.Downloads
import ph.gov.deped.region12.soxclmd.util.TimeUtils

/**
 * SOXCLMD Resource Center: downloads (guides, templates, forms) and the full
 * memoranda repository in one place. Tapping an item downloads the official
 * file through the system DownloadManager.
 */
@Composable
fun ResourcesScreen(vm: AppViewModel) {
    val context = LocalContext.current
    val resources by vm.resources.collectAsState()
    val memoranda by vm.memoranda.collectAsState()
    val sync by vm.syncStatus.collectAsState()
    var tab by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        ModuleHeader("SOXCLMD Resource Center", "Memoranda · advisories · guidelines · learning materials")
        SyncBanner(sync)

        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Downloads") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Memoranda") })
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(if (tab == 0) "Search resources" else "Search memoranda") },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge
        )

        if (tab == 0) {
            val list = resources.filter {
                query.isBlank() || it.title.contains(query, true) || it.category.contains(query, true)
            }.sortedByDescending { it.date }
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp)) {
                if (list.isEmpty()) item { EmptyState("No resources match your search.") }
                items(list, key = { it.id }) { r ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                Downloads.download(
                                    context,
                                    vm.absoluteUrl(r.file),
                                    r.file.substringAfterLast('/'),
                                    r.title
                                )
                            }
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                TagChip("${r.type} · ${r.category}")
                                Spacer(Modifier.height(6.dp))
                                Text(r.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    listOfNotNull(
                                        TimeUtils.parseDate(r.date)?.format(TimeUtils.short),
                                        r.size.ifBlank { null }
                                    ).joinToString(" · "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "Download",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        } else {
            val list = memoranda.filter {
                query.isBlank() || it.title.contains(query, true) || it.number.contains(query, true)
            }.sortedByDescending { it.date }
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp)) {
                if (list.isEmpty()) item { EmptyState("No memoranda match your search.") }
                items(list, key = { it.number }) { m ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                Downloads.download(
                                    context,
                                    vm.absoluteUrl(m.file),
                                    "${m.number.replace(Regex("[^A-Za-z0-9-]"), "_")}.pdf",
                                    m.title
                                )
                            }
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    m.number,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    TimeUtils.parseDate(m.date)?.format(TimeUtils.display) ?: m.date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(m.title, style = MaterialTheme.typography.titleMedium)
                            if (m.learningArea != null || m.program != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    listOfNotNull(m.learningArea, m.program).joinToString(" · "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Tap to download PDF" +
                                    (if (m.pages != null) " · ${m.pages} pages" else ""),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

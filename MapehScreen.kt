package ph.gov.deped.region12.soxclmd.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ph.gov.deped.region12.soxclmd.ui.AppViewModel
import ph.gov.deped.region12.soxclmd.ui.components.EmptyState
import ph.gov.deped.region12.soxclmd.ui.components.ModuleHeader
import ph.gov.deped.region12.soxclmd.ui.components.TagChip
import java.time.LocalDate

/**
 * MAPEH Hub — dedicated space for Music & Arts and PE & Health, driven
 * entirely by the official learning-areas and memoranda data (only programs
 * that exist in the data source are shown).
 */
@Composable
fun MapehScreen(vm: AppViewModel, nav: NavHostController) {
    val areas by vm.learningAreas.collectAsState()
    val memoranda by vm.memoranda.collectAsState()
    val resources by vm.resources.collectAsState()
    val events by vm.events.collectAsState()
    var tab by remember { mutableStateOf(0) }

    val mapeh = areas.firstOrNull { it.slug == "mapeh" }
    val mapehKeywords = listOf("MAPEH", "Music", "Arts", "Physical Education", "PE", "Health", "SPA", "SPS", "SPFL")
    fun matchesMapeh(haystack: String?): Boolean {
        val h = haystack ?: return false
        return mapehKeywords.any { h.contains(it, true) }
    }
    val mapehMemos = memoranda.filter { matchesMapeh(it.learningArea) || matchesMapeh(it.program) }
        .sortedByDescending { it.date }
    val mapehResources = resources.filter {
        matchesMapeh(it.category) || it.tags.any { t -> matchesMapeh(t) }
    }
    val today = LocalDate.now().toString()
    val mapehEvents = events.items.filter {
        matchesMapeh(it.title) || matchesMapeh(it.audience)
    }.filter { (it.endDate.ifBlank { it.startDate }) >= today }.sortedBy { it.startDate }

    Column(Modifier.fillMaxSize()) {
        ModuleHeader(
            "MAPEH Hub",
            mapeh?.supervisor ?: "Music, Arts, Physical Education, and Health"
        )
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Music & Arts") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("PE & Health") })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Programs") })
        }

        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            if (tab == 2) {
                if (mapeh == null || mapeh.programsHandled.isEmpty()) {
                    item { EmptyState("Program information will appear after the first sync.") }
                } else {
                    item {
                        Text("Programs handled by the MAPEH portfolio", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                    }
                    items(mapeh.programsHandled) { p ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(p, Modifier.padding(14.dp), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    if (mapeh.bestPractices.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(10.dp))
                            Text("Best practices", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                        }
                        items(mapeh.bestPractices) { Text("•  $it", style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            } else {
                val focus = if (tab == 0) "Music & Arts" else "PE & Health"
                val filteredMemos = mapehMemos.filter {
                    it.learningArea?.contains(focus.substringBefore(" & "), true) ?: false ||
                        it.title.contains(focus.substringBefore(" & "), true) ||
                        it.program?.let { p -> mapehKeywords.any { p.contains(it, true) } } ?: false
                }
                item {
                    Text(
                        "$focus — memoranda, trainings, and materials",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (mapehEvents.isNotEmpty()) {
                    items(mapehEvents.take(3), key = { "ev-${it.title}" }) { ev ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                TagChip("Upcoming · ${ev.startDate}")
                                Spacer(Modifier.height(4.dp))
                                Text(ev.title, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                if (filteredMemos.isEmpty() && mapehMemos.isEmpty() && mapehResources.isEmpty()) {
                    item { EmptyState("No $focus items published yet.") }
                }
                val shown = if (filteredMemos.isNotEmpty()) filteredMemos else mapehMemos
                items(shown.take(15), key = { "m-${it.number}" }) { m ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row {
                                TagChip(m.number, gold = true)
                                Spacer(Modifier.width(8.dp))
                                Text(m.date, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(m.title, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

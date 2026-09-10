package ph.gov.deped.region12.soxclmd.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import ph.gov.deped.region12.soxclmd.data.model.LearningArea
import ph.gov.deped.region12.soxclmd.ui.AppViewModel
import ph.gov.deped.region12.soxclmd.ui.components.EmptyState
import ph.gov.deped.region12.soxclmd.ui.components.ModuleHeader
import ph.gov.deped.region12.soxclmd.ui.components.TagChip
import ph.gov.deped.region12.soxclmd.ui.theme.BrandGold
import ph.gov.deped.region12.soxclmd.ui.theme.BrandPrimary

/** Learning Areas directory (data-driven from learning-areas.json). */
@Composable
fun LearningAreasScreen(vm: AppViewModel, nav: NavHostController) {
    val areas by vm.learningAreas.collectAsState()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp)) {
        item { ModuleHeader("Learning Areas", "Curriculum leadership, programs, and supervision") }
        if (areas.isEmpty()) item { EmptyState("Learning areas will appear after the first sync.") }
        items(areas, key = { it.slug }) { area ->
            AreaRow(area, resolve = { vm.absoluteUrl(it) }, onClick = { nav.navigate("area/${area.slug}") })
        }
    }
}

@Composable
private fun AreaRow(area: LearningArea, resolve: (String) -> String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() }
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (area.image.isNotBlank()) {
                AsyncImage(
                    model = resolve(area.image),
                    contentDescription = area.supervisor,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(area.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    area.supervisor,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("›", color = BrandPrimary, fontWeight = FontWeight.Black)
        }
    }
}

/** Full learning-area profile. */
@Composable
fun LearningAreaDetailScreen(vm: AppViewModel, nav: NavHostController, slug: String) {
    val areas by vm.learningAreas.collectAsState()
    val memoranda by vm.memoranda.collectAsState()
    val area = areas.firstOrNull { it.slug == slug }

    if (area == null) {
        Column(Modifier.fillMaxSize()) {
            ModuleHeader("Learning Area", "")
            EmptyState("This learning area is not available yet.")
        }
        return
    }

    val relatedMemos = memoranda.filter {
        (it.learningArea?.contains(area.title.substringBefore('·').trim(), true) ?: false) ||
        (it.program?.contains(area.title.substringBefore('·').trim(), true) ?: false)
    }.sortedByDescending { it.date }.take(10)

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { ModuleHeader(area.title, area.supervisor) }
        item {
            Column(Modifier.padding(16.dp)) {
                if (area.image.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = vm.absoluteUrl(area.image),
                            contentDescription = area.supervisor,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(36.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(area.position, style = MaterialTheme.typography.titleMedium, color = BrandPrimary)
                            Text(
                                area.overview,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Text(
                        area.overview,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (area.programsHandled.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text("Programs handled", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    area.programsHandled.forEach {
                        Text("•  $it", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (area.objectives.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text("Objectives", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    area.objectives.forEach {
                        Text("•  $it", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (area.responsibilities.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text("Responsibilities", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    area.responsibilities.forEach {
                        Text("•  $it", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (relatedMemos.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text("Related memoranda", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    relatedMemos.forEach { m ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row {
                                    TagChip(m.number, gold = true)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        m.date, style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
}

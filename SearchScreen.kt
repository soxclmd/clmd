package ph.gov.deped.region12.soxclmd.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ph.gov.deped.region12.soxclmd.ui.AppViewModel
import ph.gov.deped.region12.soxclmd.ui.components.EmptyState
import ph.gov.deped.region12.soxclmd.ui.components.TagChip
import ph.gov.deped.region12.soxclmd.util.Downloads
import java.net.URLEncoder

/**
 * Global search across the shared search index (news, memoranda, resources,
 * learning areas, programs, pages) with type filtering.
 */
@Composable
fun SearchScreen(vm: AppViewModel) {
    val context = LocalContext.current
    val index by vm.searchIndex.collectAsState()
    var query by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf<String?>(null) }

    val types = index.map { it.type }.distinct().sorted()
    val results = index.filter { e ->
        (query.length >= 2) && (
            e.title.contains(query, true) ||
                e.keywords.contains(query, true) ||
                e.type.contains(query, true)
        ) && (typeFilter == null || e.type == typeFilter)
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search memoranda, news, resources, programs…") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge
        )
        Row(Modifier.padding(horizontal = 16.dp)) {
            FilterChip(
                selected = typeFilter == null,
                onClick = { typeFilter = null },
                label = { Text("All") }
            )
            Spacer(Modifier.width(6.dp))
            types.take(6).forEach { t ->
                FilterChip(
                    selected = typeFilter == t,
                    onClick = { typeFilter = if (typeFilter == t) null else t },
                    label = { Text(t) }
                )
                Spacer(Modifier.width(6.dp))
            }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            if (query.length < 2) item {
                Text(
                    "Type at least two letters to search across ${index.size} indexed items.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (query.length >= 2 && results.isEmpty()) item { EmptyState("No results for “$query”.") }
            items(results, key = { "${it.type}-${it.title}" }) { r ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            val browse = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://soxclmd.github.io/clmd/")
                            )
                            context.startActivity(browse)
                        }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        TagChip(r.type)
                        Spacer(Modifier.height(6.dp))
                        Text(r.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            r.keywords, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

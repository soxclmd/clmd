package ph.gov.deped.region12.soxclmd.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ph.gov.deped.region12.soxclmd.ui.AppViewModel
import ph.gov.deped.region12.soxclmd.ui.components.ModuleHeader
import ph.gov.deped.region12.soxclmd.ui.components.SyncBanner

/** More menu: secondary modules and settings. */
@Composable
fun MoreScreen(vm: AppViewModel, nav: NavHostController) {
    val context = LocalContext.current
    val sync by vm.syncStatus.collectAsState()

    data class Row(val icon: ImageVector, val label: String, val sub: String, val onClick: () -> Unit)

    val rows = listOf(
        Row(Icons.Filled.School, "Learning Areas", "Curriculum portfolios and program holders") { nav.navigate("learning-areas") },
        Row(Icons.Filled.Search, "Global Search", "Search everything in the portal") { nav.navigate("search") },
        Row(Icons.Filled.CalendarMonth, "Calendar", "Month view of activities and deadlines") { nav.navigate("calendar") },
        Row(Icons.Filled.Campaign, "Announcements", "Official CLMD announcements") { nav.navigate("news") },
        Row(Icons.Filled.Info, "About SOXCLMD", "Mandate, vision, mission, leadership") { nav.navigate("about") },
        Row(Icons.Filled.Mail, "Contact", "Official contact information") { nav.navigate("about") },
        Row(Icons.Filled.Settings, "Settings", "Sync, storage, and app information") { nav.navigate("settings") },
        Row(Icons.Filled.HelpOutline, "Help", "How content updates work") { nav.navigate("settings") }
    )

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp)) {
        item { ModuleHeader("More", "SOXCLMD · DepEd Regional Office XII") }
        item { SyncBanner(sync) }
        items(rows.size) { i ->
            val r = rows[i]
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { r.onClick() }
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(r.icon, contentDescription = r.label, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(r.label, style = MaterialTheme.typography.titleMedium)
                        Text(
                            r.sub, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

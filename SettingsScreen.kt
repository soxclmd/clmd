package ph.gov.deped.region12.soxclmd.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ph.gov.deped.region12.soxclmd.BuildConfig
import ph.gov.deped.region12.soxclmd.data.ContentRepository
import ph.gov.deped.region12.soxclmd.ui.AppViewModel
import ph.gov.deped.region12.soxclmd.ui.components.ModuleHeader
import ph.gov.deped.region12.soxclmd.ui.components.SyncBanner
import ph.gov.deped.region12.soxclmd.util.Downloads
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Settings: content synchronization status, storage usage, and the
 * app-update gate driven by manifest.minimum_app_version.
 */
@Composable
fun SettingsScreen(vm: AppViewModel) {
    val context = LocalContext.current
    val sync by vm.syncStatus.collectAsState()
    val manifest by vm.manifest.collectAsState()
    val repo = vm.repo
    var cacheSize by remember { mutableStateOf(0L) }
    androidx.compose.runtime.LaunchedEffect(sync) {
        cacheSize = Downloads.cacheDirSize(context)
    }

    val appOutdated = manifest?.let {
        it.minimum_app_version > BuildConfig.VERSION_NAME
    } ?: false
    val maintenance = manifest?.maintenance_mode == true

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp)) {
        item { ModuleHeader("Settings", "Content synchronization and app information") }

        if (maintenance) item {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(
                    "The SOXCLMD content service is under maintenance. Saved content remains available.",
                    Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        if (appOutdated) item {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(
                    "An app update is required to continue receiving content " +
                        "(content requires app version ${manifest?.minimum_app_version}; " +
                        "installed: ${BuildConfig.VERSION_NAME}).",
                    Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item { SyncBanner(sync) }

        item {
            Column(Modifier.padding(16.dp)) {
                Text("Content synchronization", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    "This app reads its content directly from the official SOXCLMD " +
                        "GitHub Pages source — the same source as the website. It checks for " +
                        "updates when opened, when you return to the app, every six hours, " +
                        "and whenever your device reconnects to the internet. Only files that " +
                        "actually changed are downloaded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Button(onClick = { vm.refresh(force = true) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Sync now")
                }
                Spacer(Modifier.height(10.dp))
                Detail("App version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                Detail("Content version", manifest?.content_version ?: "not yet synced")
                Detail("Content last updated", manifest?.last_updated ?: "—")
                Detail("Content cache", Downloads.humanBytes(cacheSize))
                Detail("Content source", ContentRepository.BASE_URL)
                Detail(
                    "Last sync attempt",
                    when (val s = sync) {
                        is ContentRepository.SyncStatus.Done ->
                            SimpleDateFormat("MMM d, yyyy h:mm a", Locale.ENGLISH).format(Date(s.at))
                        is ContentRepository.SyncStatus.Offline ->
                            if (s.lastSuccess > 0) SimpleDateFormat("MMM d, yyyy h:mm a", Locale.ENGLISH).format(Date(s.lastSuccess)) else "no successful sync yet"
                        else -> "in progress"
                    }
                )
                Spacer(Modifier.height(18.dp))
                Text("How updates work", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text(
                    "• Content (news, memoranda, activities, resources) updates itself " +
                        "automatically — no app reinstall needed.\n" +
                        "• App updates are only needed when the application itself changes.\n" +
                        "• Offline: the app shows your saved copy of the content and marks it clearly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(18.dp))
                Text("Data privacy", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text(
                    "SOXCLMD collects no personal data. The app requests only internet " +
                        "access and stores content on your device for offline use.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Detail(label: String, value: String) {
    Row(Modifier.padding(vertical = 4.dp)) {
        Text(
            label, style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(150.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

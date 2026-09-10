package ph.gov.deped.region12.soxclmd.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ph.gov.deped.region12.soxclmd.ui.AppViewModel
import ph.gov.deped.region12.soxclmd.ui.components.ModuleHeader

/** About + Contact: official mandate, vision, mission, leadership, contacts. */
@Composable
fun AboutScreen(vm: AppViewModel) {
    val context = LocalContext.current
    val site by vm.site.collectAsState()

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp)) {
        item { ModuleHeader(site.name.ifBlank { "About SOXCLMD" }, site.agency) }
        item {
            Column(Modifier.padding(16.dp)) {
                Section("Mandate")
                Text(
                    "The Curriculum and Learning Management Division (CLMD) leads curriculum " +
                        "implementation, learning management, and quality assurance across the " +
                        "Schools Division Offices of ${site.region}.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Section("Vision")
                Text(site.vision, style = MaterialTheme.typography.bodyLarge)
                Section("Mission")
                Text(site.mission, style = MaterialTheme.typography.bodyLarge)

                if (site.coreValues.isNotEmpty()) {
                    Section("Core Values")
                    site.coreValues.forEach { cv ->
                        Text("•  ${cv.title}", style = MaterialTheme.typography.titleMedium)
                        Text(
                            cv.text, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                if (site.leaders.isNotEmpty()) {
                    Section("Leadership")
                    site.leaders.forEach { l ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(l.position, style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary)
                                Text(l.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    l.message, style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Section("Official Contact")
                val c = site.contact
                ContactRow("Address", c.address)
                ContactRow("Telephone", c.telephone)
                ContactRow("CLMD Direct Line", c.clmdTelephone)
                ContactRow("Email", c.email)
                TextButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(c.facebook)))
                }) { Text("Facebook Page ↗") }
                TextButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(c.website)))
                }) { Text("Official Website ↗") }
            }
        }
    }
}

@Composable
private fun Section(title: String) {
    Spacer(Modifier.height(14.dp))
    Text(title, style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun ContactRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(Modifier.padding(vertical = 3.dp)) {
        Text(
            label, style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(140.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

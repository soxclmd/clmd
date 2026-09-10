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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.gov.deped.region12.soxclmd.ui.AppViewModel
import ph.gov.deped.region12.soxclmd.ui.components.EmptyState
import ph.gov.deped.region12.soxclmd.ui.components.ModuleHeader
import ph.gov.deped.region12.soxclmd.ui.components.SyncBanner
import ph.gov.deped.region12.soxclmd.ui.theme.BrandGold
import ph.gov.deped.region12.soxclmd.ui.theme.BrandPrimary
import ph.gov.deped.region12.soxclmd.ui.theme.BrandSoft
import ph.gov.deped.region12.soxclmd.ui.theme.OngoingGreen
import ph.gov.deped.region12.soxclmd.util.TimeUtils
import java.time.LocalDate
import java.time.YearMonth

/** Activities: upcoming trainings, workshops and regional activities. */
@Composable
fun ActivitiesScreen(vm: AppViewModel) {
    val events by vm.events.collectAsState()
    val sync by vm.syncStatus.collectAsState()
    val today = LocalDate.now()

    val active = events.items
        .filter { (it.endDate.ifBlank { it.startDate }) >= today.toString() }
        .sortedBy { it.startDate }

    Column(Modifier.fillMaxSize()) {
        ModuleHeader("Activities", "Trainings, workshops, and regional activities")
        SyncBanner(sync)
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (active.isEmpty()) item { EmptyState("No upcoming activities are currently listed.") }
            items(active, key = { "${it.title}-${it.startDate}" }) { ev ->
                EventCard(ev, onOpen = { })
            }
        }
    }
}

/** Month calendar with dots on days that have activities. */
@Composable
fun CalendarScreen(vm: AppViewModel) {
    val events by vm.events.collectAsState()
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selected by remember { mutableStateOf(LocalDate.now()) }

    val byDate: Map<LocalDate, List<ph.gov.deped.region12.soxclmd.data.model.CalendarEvent>> =
        events.items.mapNotNull { e ->
            val s = TimeUtils.parseDate(e.startDate) ?: return@mapNotNull null
            val en = TimeUtils.parseDate(e.endDate.ifBlank { e.startDate }) ?: s
            val days = java.time.temporal.ChronoUnit.DAYS.between(s, en)
            (0..days).map { s.plusDays(it) to e }
        }.flatten().groupBy({ it.first }, { it.second })

    Column(Modifier.fillMaxSize()) {
        ModuleHeader("SOXCLMD Calendar", "Trainings · workshops · deadlines · regional activities")
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { month = month.minusMonths(1) }) { Text("‹ Prev") }
            Text(
                "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = { month = month.plusMonths(1) }) { Text("Next ›") }
        }

        val grid = TimeUtils.monthGrid(month)
        Column(Modifier.padding(horizontal = 12.dp)) {
            Row(Modifier.fillMaxWidth()) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                    Text(
                        it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            grid.forEach { week ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    week.forEach { day ->
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    color = when {
                                        day == null -> Color.Transparent
                                        day == selected -> BrandPrimary
                                        day == LocalDate.now() -> BrandGold.copy(alpha = 0.35f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = RoundedCornerShape(9.dp)
                                )
                                .clickable { day?.let { selected = it } },
                            contentAlignment = Alignment.Center
                        ) {
                            if (day != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        day.dayOfMonth.toString(),
                                        color = if (day == selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp,
                                        fontWeight = if (day == LocalDate.now()) FontWeight.Black else FontWeight.Normal
                                    )
                                    if (byDate.containsKey(day)) {
                                        Box(
                                            Modifier
                                                .padding(top = 1.dp)
                                                .size(4.dp)
                                                .background(
                                                    if (day == selected) Color.White else OngoingGreen,
                                                    RoundedCornerShape(2.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Text(
            TimeUtils.display.format(selected),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        val dayEvents = byDate[selected].orEmpty()
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (dayEvents.isEmpty()) item {
                Text(
                    "No activities on this date.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(dayEvents.distinctBy { it.title }, key = { "${it.title}-${it.startDate}" }) { ev ->
                EventCard(ev, onOpen = { })
            }
        }
    }
}

@Composable
private fun EventCard(ev: ph.gov.deped.region12.soxclmd.data.model.CalendarEvent, onOpen: () -> Unit) {
    val status = TimeUtils.statusOf(ev.startDate, ev.endDate)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp)) {
            Column(
                Modifier.background(BrandSoft, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val d = TimeUtils.parseDate(ev.startDate)
                Text(d?.dayOfMonth?.toString() ?: "–", color = BrandPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text(d?.month?.name?.take(3)?.uppercase() ?: "", color = BrandPrimary, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "$status · Official memo",
                    color = if (status == "Ongoing") OngoingGreen else BrandPrimary,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(ev.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    listOfNotNull(
                        TimeUtils.rangeLabel(ev.startDate, ev.endDate),
                        ev.time,
                        ev.venue.ifBlank { null },
                        ev.audience.ifBlank { null }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (ev.memoNumbers.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Memoranda: ${ev.memoNumbers.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandPrimary
                    )
                }
            }
        }
    }
}

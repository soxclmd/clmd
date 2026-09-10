package ph.gov.deped.region12.soxclmd.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeUtils {
    private val iso: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val display: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
    val short: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
    val dayMonth: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)

    fun parseDate(s: String?): LocalDate? =
        runCatching { LocalDate.parse(s ?: return null, iso) }.getOrNull()

    fun rangeLabel(start: String, end: String?): String {
        val s = parseDate(start) ?: return start
        val e = parseDate(end ?: start) ?: return display.format(s)
        return if (s == e) display.format(s) else "${short.format(s)} – ${display.format(e)}"
    }

    fun statusOf(start: String, end: String?, today: LocalDate = LocalDate.now()): String {
        val s = parseDate(start) ?: return "Scheduled"
        val e = parseDate(end ?: start) ?: return "Scheduled"
        return when {
            today < s -> "Upcoming"
            today in s..e -> "Ongoing"
            else -> "Ended"
        }
    }

    fun monthGrid(yearMonth: YearMonth): List<List<LocalDate?>> {
        val first = yearMonth.atDay(1)
        val lead = first.dayOfWeek.value - 1 // Monday-first grid
        val daysInMonth = yearMonth.lengthOfMonth()
        val cells = MutableList<LocalDate?>(lead) { null }
        for (d in 1..daysInMonth) cells += yearMonth.atDay(d)
        while (cells.size % 7 != 0) cells += null
        return cells.chunked(7)
    }
}

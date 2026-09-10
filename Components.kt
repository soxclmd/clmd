package ph.gov.deped.region12.soxclmd.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.gov.deped.region12.soxclmd.data.ContentRepository
import ph.gov.deped.region12.soxclmd.ui.theme.BrandGold
import ph.gov.deped.region12.soxclmd.ui.theme.BrandPrimary
import ph.gov.deped.region12.soxclmd.ui.theme.BrandPrimaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Violet header banner used at the top of every module screen. */
@Composable
fun ModuleHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(listOf(BrandPrimaryDark, BrandPrimary)),
                shape = RoundedCornerShape(bottomStart = (20).dp, bottomEnd = (20).dp)
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(3.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xE6FFFFFF))
        }
    }
}

/** Small violet chip, e.g. category / status labels. */
@Composable
fun TagChip(text: String, modifier: Modifier = Modifier, gold: Boolean = false) {
    Text(
        text = text.uppercase(Locale.ROOT),
        modifier = modifier
            .background(
                color = if (gold) BrandGold else MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        color = if (gold) Color(0xFF3D2E00) else MaterialTheme.colorScheme.onPrimaryContainer,
        style = MaterialTheme.typography.labelSmall
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(text, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .width(38.dp)
                .height(3.dp)
                .background(BrandGold, RoundedCornerShape(3.dp))
        )
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.SearchOff, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(44.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun LoadingState(label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = BrandPrimary, strokeWidth = 3.dp, modifier = Modifier.size(34.dp))
        Spacer(Modifier.height(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Banner reflecting the current sync state; explains offline mode honestly. */
@Composable
fun SyncBanner(status: ContentRepository.SyncStatus, modifier: Modifier = Modifier) {
    val (icon, tint, text) = when (status) {
        is ContentRepository.SyncStatus.Checking ->
            Triple(Icons.Filled.Sync, MaterialTheme.colorScheme.primary, "Checking for content updates…")
        is ContentRepository.SyncStatus.Downloading ->
            Triple(Icons.Filled.Sync, MaterialTheme.colorScheme.primary, "Downloading updates (${status.done}/${status.total})…")
        is ContentRepository.SyncStatus.Offline -> {
            val last = if (status.lastSuccess > 0)
                "Showing saved content · last updated " + SimpleDateFormat("MMM d, h:mm a", Locale.ENGLISH).format(Date(status.lastSuccess))
            else "No connection yet — content will load once you go online."
            Triple(Icons.Filled.CloudOff, MaterialTheme.colorScheme.onSurfaceVariant, last)
        }
        else -> return
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = tint, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Gold "NEW" pill used on the home dashboard. */
@Composable
fun NewPill(modifier: Modifier = Modifier) {
    Text(
        "NEW",
        modifier = modifier
            .background(BrandGold, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        color = Color(0xFF3D2E00),
        fontSize = 9.sp,
        fontWeight = FontWeight.Black
    )
}

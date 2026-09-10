package ph.gov.deped.region12.soxclmd.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = BrandSoft,
    onPrimaryContainer = BrandPrimaryDark,
    secondary = BrandSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    tertiary = BrandGold,
    onTertiary = GoldDark,
    background = androidx.compose.ui.graphics.Color(0xFFF7F5FA),
    onBackground = InkDark,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = InkDark,
    surfaceVariant = BrandSoft,
    onSurfaceVariant = MutedDark,
    outline = LineDark,
    error = androidx.compose.ui.graphics.Color(0xFFB3261E)
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFCBA6E8),
    onPrimary = CardDark,
    primaryContainer = BrandSecondary,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = BrandGold,
    tertiary = BrandGold,
    background = androidx.compose.ui.graphics.Color(0xFF17131C),
    onBackground = androidx.compose.ui.graphics.Color(0xFFF3F2F5),
    surface = androidx.compose.ui.graphics.Color(0xFF201C27),
    onSurface = androidx.compose.ui.graphics.Color(0xFFF3F2F5),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF35293A),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFB3AFB8)
)

@Composable
fun SOXCLMDTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SOXCLMDTypography,
        content = content
    )
}

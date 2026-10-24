package dev.nitrodroid.clock.ui

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import dev.nitrodroid.clock.Prefs

/** Nitro blue by default; Material You wallpaper colors are an opt-in setting. */
object ThemeState {
    var dynamic by mutableStateOf(false)
    fun init(c: Context) { dynamic = Prefs.dynamicColor(c) }
}

private val NitroLight = lightColorScheme(
    primary = Color(0xFF0061A4), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1E4FF), onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF535F70), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD7E3F8), onSecondaryContainer = Color(0xFF101C2B),
    tertiary = Color(0xFF6B5778), tertiaryContainer = Color(0xFFF2DAFF),
    background = Color(0xFFFDFCFF), onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFDFCFF), onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDFE2EB), onSurfaceVariant = Color(0xFF42474E),
    outline = Color(0xFF73777F), outlineVariant = Color(0xFFC3C7CF),
    error = Color(0xFFBA1A1A), errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
    surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFF3F4F9),
    surfaceContainer = Color(0xFFEDEEF3), surfaceContainerHigh = Color(0xFFE7E8ED),
    surfaceContainerHighest = Color(0xFFE1E2E7),
)

private val NitroDark = darkColorScheme(
    primary = Color(0xFF9ECAFF), onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D), onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFBBC7DB), onSecondary = Color(0xFF253140),
    secondaryContainer = Color(0xFF3B4858), onSecondaryContainer = Color(0xFFD7E3F8),
    tertiary = Color(0xFFD6BEE4), tertiaryContainer = Color(0xFF523F5F),
    background = Color(0xFF111316), onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF111316), onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF42474E), onSurfaceVariant = Color(0xFFC3C7CF),
    outline = Color(0xFF8D9199), outlineVariant = Color(0xFF42474E),
    error = Color(0xFFFFB4AB), errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
    surfaceContainerLowest = Color(0xFF0B0D10), surfaceContainerLow = Color(0xFF191C1E),
    surfaceContainer = Color(0xFF1D2022), surfaceContainerHigh = Color(0xFF272A2D),
    surfaceContainerHighest = Color(0xFF323538),
)

@Composable
fun NitroClockTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val scheme = when {
        ThemeState.dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> NitroDark
        else -> NitroLight
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

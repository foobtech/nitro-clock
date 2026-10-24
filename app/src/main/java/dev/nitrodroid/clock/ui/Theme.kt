package dev.nitrodroid.clock.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val NitroLight = lightColorScheme(
    primary = Color(0xFFB83F00),
    secondary = Color(0xFF77574A),
    tertiary = Color(0xFF6B5E2F),
)
private val NitroDark = darkColorScheme(
    primary = Color(0xFFFFB59A),
    secondary = Color(0xFFE7BDAE),
    tertiary = Color(0xFFD7C68D),
)

@Composable
fun NitroClockTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> NitroDark
        else -> NitroLight
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

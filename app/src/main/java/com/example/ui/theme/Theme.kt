package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = VioletPrimary,
    onPrimary = Color.White,
    primaryContainer = VioletPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = LavenderSecondary,
    onSecondary = Color(0xFF1B0047),
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = Color.White,
    tertiary = PinkTertiary,
    onTertiary = Color.White,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = LavenderSecondary,
    outline = BorderOutline,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = VioletPrimaryDark,
    onPrimary = Color.White,
    secondary = VioletPrimary,
    onSecondary = Color.White,
    background = Color(0xFFF7F5FC),
    onBackground = Color(0xFF1B1729),
    surface = Color.White,
    onSurface = Color(0xFF1B1729),
    surfaceVariant = Color(0xFFEBE6F8),
    onSurfaceVariant = Color(0xFF4A3B6E),
    outline = Color(0xFFD4CBEF),
    error = ErrorRed
)

@Composable
fun PersonaChatTheme(
    darkTheme: Boolean = true, // Default to modern dark aesthetic per user specification
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.pyera.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonYellow,
    onPrimary = DarkGreen,
    primaryContainer = NeonYellow.copy(alpha = 0.15f),
    onPrimaryContainer = NeonYellow,
    secondary = SurfaceElevated,
    onSecondary = TextPrimary,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = ColorWarning,
    onTertiary = DarkGreen,
    tertiaryContainer = ColorWarningContainer,
    onTertiaryContainer = ColorWarning,
    background = DarkGreen,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceTint = NeonYellow,
    error = ColorError,
    onError = TextPrimary,
    errorContainer = ColorErrorContainer,
    onErrorContainer = ColorError,
    outline = ColorBorder,
    outlineVariant = ColorBorder.copy(alpha = 0.5f),
    scrim = Color(0xFF000000)
)

// Light scheme as fallback - primarily using dark mode as brand identity
private val LightColorScheme = lightColorScheme(
    primary = NeonYellow,
    onPrimary = DarkGreen,
    primaryContainer = NeonYellow.copy(alpha = 0.15f),
    onPrimaryContainer = NeonYellowDark,
    secondary = SurfaceElevated,
    onSecondary = TextPrimary,
    background = Color(0xFFF5F5F5),
    onBackground = DarkGreen,
    surface = Color(0xFFFFFFFF),
    onSurface = DarkGreen,
    surfaceVariant = Color(0xFFE8E8E8),
    onSurfaceVariant = TextSecondary,
    error = ColorError,
    onError = Color(0xFFFFFFFF),
    errorContainer = ColorErrorContainer,
    outline = ColorBorder
)

@Composable
fun PyeraTheme(
    darkTheme: Boolean = true, // Force dark theme by default as per design
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic color to stick to brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

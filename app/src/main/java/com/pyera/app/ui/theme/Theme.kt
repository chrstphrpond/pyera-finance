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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.pyera.app.data.preferences.ThemeMode

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
    scrim = androidx.compose.ui.graphics.Color(0xFF000000)
)

// Light Color Scheme - Material You inspired with Pyera brand colors
val LightColorScheme = lightColorScheme(
    primary = ColorPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = ColorPrimaryContainerLight,
    onPrimaryContainer = ColorPrimaryDark,
    secondary = ColorSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = ColorSecondaryContainerLight,
    onSecondaryContainer = ColorSecondaryDark,
    tertiary = ColorTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = ColorTertiaryContainerLight,
    onTertiaryContainer = ColorTertiaryDark,
    background = ColorBackgroundLight,
    onBackground = ColorOnBackgroundLight,
    surface = ColorSurfaceLight,
    onSurface = ColorOnSurfaceLight,
    surfaceVariant = ColorSurfaceVariantLight,
    onSurfaceVariant = ColorOnSurfaceVariantLight,
    surfaceTint = ColorPrimaryLight,
    error = ColorErrorLight,
    onError = Color.White,
    errorContainer = ColorErrorContainerLight,
    onErrorContainer = ColorErrorDark,
    outline = ColorOutlineLight,
    outlineVariant = ColorOutlineVariantLight,
    scrim = androidx.compose.ui.graphics.Color(0xFF000000)
)

@Composable
fun PyeraTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic color to stick to brand identity
    content: @Composable () -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemInDarkTheme
    }
    
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

/**
 * Legacy overload for backward compatibility
 */
@Composable
fun PyeraTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT
    PyeraTheme(
        themeMode = themeMode,
        dynamicColor = dynamicColor,
        content = content
    )
}

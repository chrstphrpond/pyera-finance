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
import com.pyera.app.data.preferences.ThemeMode
import com.pyera.app.ui.theme.tokens.ColorTokens

/**
 * Dark color scheme - Primary theme for Pyera Finance.
 */
private val DarkColorScheme = darkColorScheme(
    primary = ColorTokens.Primary500,
    onPrimary = Color.White,
    primaryContainer = ColorTokens.Primary900,
    onPrimaryContainer = ColorTokens.Primary100,

    secondary = ColorTokens.Success500,
    onSecondary = Color.Black,
    secondaryContainer = ColorTokens.Success500.copy(alpha = 0.2f),
    onSecondaryContainer = ColorTokens.Success100,

    tertiary = ColorTokens.Info500,
    onTertiary = Color.White,
    tertiaryContainer = ColorTokens.Info500.copy(alpha = 0.2f),
    onTertiaryContainer = ColorTokens.Info100,

    background = ColorTokens.SurfaceLevel0,
    onBackground = ColorTokens.Slate100,

    surface = ColorTokens.SurfaceLevel1,
    onSurface = ColorTokens.Slate100,
    surfaceVariant = ColorTokens.SurfaceLevel2,
    onSurfaceVariant = ColorTokens.Slate400,
    surfaceTint = ColorTokens.Primary500,

    inverseSurface = ColorTokens.Slate100,
    inverseOnSurface = ColorTokens.Slate900,
    inversePrimary = ColorTokens.Primary300,

    error = ColorTokens.Error500,
    onError = Color.White,
    errorContainer = ColorTokens.Error500.copy(alpha = 0.2f),
    onErrorContainer = ColorTokens.Error100,

    outline = ColorTokens.Slate600,
    outlineVariant = ColorTokens.Slate700,

    scrim = Color.Black.copy(alpha = 0.6f)
)

/**
 * Light color scheme - Secondary theme.
 */
private val LightColorScheme = lightColorScheme(
    primary = ColorTokens.Primary500,
    onPrimary = Color.White,
    primaryContainer = ColorTokens.Primary100,
    onPrimaryContainer = ColorTokens.Primary900,

    secondary = ColorTokens.Success500,
    onSecondary = Color.White,
    secondaryContainer = ColorTokens.Success50,
    onSecondaryContainer = ColorTokens.Success600,

    tertiary = ColorTokens.Info500,
    onTertiary = Color.White,
    tertiaryContainer = ColorTokens.Info50,
    onTertiaryContainer = ColorTokens.Info600,

    background = ColorTokens.Slate50,
    onBackground = ColorTokens.Slate900,

    surface = Color.White,
    onSurface = ColorTokens.Slate900,
    surfaceVariant = ColorTokens.Slate100,
    onSurfaceVariant = ColorTokens.Slate600,
    surfaceTint = ColorTokens.Primary500,

    inverseSurface = ColorTokens.Slate800,
    inverseOnSurface = ColorTokens.Slate50,
    inversePrimary = ColorTokens.Primary400,

    error = ColorTokens.Error500,
    onError = Color.White,
    errorContainer = ColorTokens.Error50,
    onErrorContainer = ColorTokens.Error600,

    outline = ColorTokens.Slate200,
    outlineVariant = ColorTokens.Slate100,

    scrim = Color.Black.copy(alpha = 0.4f)
)

/**
 * Main theme composable for Pyera Finance.
 * 
 * @param darkTheme Whether to use dark theme (default: follows system)
 * @param dynamicColor Whether to use dynamic colors on Android 12+ (default: false for brand consistency)
 * @param content Content to be themed
 */
@Composable
fun PyeraTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    PyeraTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}

@Composable
fun PyeraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

            // Configure edge-to-edge
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Status bar
            window.statusBarColor = colorScheme.background.toArgb()

            // Navigation bar
            window.navigationBarColor = colorScheme.surface.toArgb()

            // Icon colors
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

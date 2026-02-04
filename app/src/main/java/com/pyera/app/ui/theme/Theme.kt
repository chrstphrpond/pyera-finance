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

private val DarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = DeepBackground,
    primaryContainer = CardBackground,
    onPrimaryContainer = AccentGreen,
    secondary = PaleViolet,
    onSecondary = DeepBackground,
    secondaryContainer = InfoContainer,
    onSecondaryContainer = PaleViolet,
    tertiary = Orange,
    onTertiary = DeepBackground,
    tertiaryContainer = WarningContainer,
    onTertiaryContainer = Orange,
    background = DeepBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    outlineVariant = CardBorder,
    error = ColorError,
    onError = BrightSnow,
    errorContainer = ErrorContainer,
    onErrorContainer = ColorError
)

// Light scheme as fallback - primarily using dark mode as brand identity
private val LightColorScheme = lightColorScheme(
    primary = Inchworm,
    onPrimary = DarkBackground,
    primaryContainer = SurfaceDark,
    onPrimaryContainer = Inchworm,
    secondary = PaleViolet,
    onSecondary = DarkBackground,
    tertiary = Orange,
    onTertiary = DarkBackground,
    background = BrightSnow,
    onBackground = DarkBackground,
    surface = AmericanSilver,
    onSurface = DarkBackground
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

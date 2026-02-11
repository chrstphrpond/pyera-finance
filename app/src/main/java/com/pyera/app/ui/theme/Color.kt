package com.pyera.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.pyera.app.ui.theme.tokens.ColorTokens

// ============================================
// MIDNIGHT SLATE THEME (2026 Refresh)
// ============================================

// Primary Brand Colors - Trust Blue
val PrimaryAccent = ColorTokens.Primary500
val PrimaryAccentDark = ColorTokens.Primary600
val PrimaryAccentLight = ColorTokens.Primary300

// Background Colors - Slate
val BackgroundPrimary = ColorTokens.SurfaceLevel0
val BackgroundSecondary = ColorTokens.SurfaceLevel1
val BackgroundTertiary = ColorTokens.SurfaceLevel2
val BackgroundElevated = ColorTokens.SurfaceLevel3

// Surface Colors - Layered depth
val SurfacePrimary = ColorTokens.SurfaceLevel1
val SurfaceSecondary = ColorTokens.SurfaceLevel2
val SurfaceElevated = ColorTokens.SurfaceLevel2
val SurfaceOverlay = ColorTokens.SurfaceLevel3

// Card Accent Colors - Cool system accents
val CardAccentMint = ColorTokens.Success500
val CardAccentPink = ColorTokens.Error500
val CardAccentBlue = ColorTokens.Info500
val CardAccentOrange = ColorTokens.Warning500
val CardAccentPurple = ColorTokens.Primary400
val CardAccentTeal = ColorTokens.Primary300

// Semantic Colors
val ColorSuccess = ColorTokens.Success500
val ColorSuccessDark = ColorTokens.Success600
val ColorSuccessContainer = ColorTokens.Success500.copy(alpha = 0.2f)
val ColorError = ColorTokens.Error500
val ColorErrorContainer = ColorTokens.Error500.copy(alpha = 0.2f)
val ColorWarning = ColorTokens.Warning500
val ColorWarningContainer = ColorTokens.Warning500.copy(alpha = 0.2f)
val ColorInfo = ColorTokens.Info500

// Text Colors - Cool neutral
val TextPrimary = ColorTokens.Slate100
val TextSecondary = ColorTokens.Slate300
val TextTertiary = ColorTokens.Slate400
val TextMuted = ColorTokens.Slate500
val TextDisabled = ColorTokens.Slate600

// Interactive States
val OverlayPressed = Color(0x22FFFFFF)
val OverlayHover = Color(0x14FFFFFF)
val OverlayDrag = Color(0x33FFFFFF)

// Borders - Subtle
val ColorBorder = ColorTokens.Slate700
val ColorBorderFocused = PrimaryAccent.copy(alpha = 0.6f)
val ColorBorderSubtle = ColorTokens.Slate800

// Currency Colors
val ColorIncome = ColorTokens.Success500
val ColorExpense = ColorTokens.Error500
val ColorDebt = ColorTokens.Warning500

// Glassmorphism Colors
val GlassBackground = ColorTokens.SurfaceLevel2.copy(alpha = 0.86f)
val GlassBorder = ColorTokens.Slate700.copy(alpha = 0.08f)

// Gradient Colors
val GradientStart = ColorTokens.Slate950
val GradientEnd = ColorTokens.Slate900


// ============================================
// LIGHT THEME SUPPORT (Midnight Slate - Light)
// ============================================

val LightBackgroundPrimary = ColorTokens.Slate50
val LightBackgroundSecondary = ColorTokens.Slate100
val LightSurfacePrimary = Color.White
val LightSurfaceSecondary = ColorTokens.Slate100
val LightSurfaceElevated = ColorTokens.Slate200
val LightSurfaceOverlay = ColorTokens.Slate200

val LightTextPrimary = ColorTokens.Slate900
val LightTextSecondary = ColorTokens.Slate700
val LightTextTertiary = ColorTokens.Slate600
val LightTextMuted = ColorTokens.Slate500

val LightBorder = ColorTokens.Slate200
val LightBorderSubtle = ColorTokens.Slate100

val LightSuccessContainer = ColorTokens.Success50
val LightWarningContainer = ColorTokens.Warning50
val LightErrorContainer = ColorTokens.Error50
val LightInfoContainer = ColorTokens.Info50


// ============================================
// LEGACY ALIASES (Backward Compatibility)
// ============================================

val NeonYellow = ColorTokens.Primary500
val NeonYellowDark = PrimaryAccentDark
val NeonYellowLight = PrimaryAccentLight
val DarkGreen = ColorTokens.SurfaceLevel0
val SurfaceDark = ColorTokens.SurfaceLevel1
val CardBackground = SurfaceSecondary
val DeepBackground = BackgroundPrimary
val AccentGreen = ColorTokens.Primary500
val AccentGreenDim = PrimaryAccentDark
val BrightSnow = TextPrimary
val AmericanSilver = TextSecondary
val Gunmetal = SurfaceSecondary
val Orange = CardAccentOrange
val PaleViolet = CardAccentPurple
val CardBorder = ColorBorder
val GlassOverlay = OverlayHover
val GreenGlow = PrimaryAccent.copy(alpha = 0.2f)
val GreenGlowSubtle = PrimaryAccent.copy(alpha = 0.1f)
val CardGradientTop = ColorTokens.SurfaceLevel2
val CardGradientBottom = SurfacePrimary

// Legacy semantic colors
val PositiveChange = ColorTokens.Success500
val NegativeChange = ColorTokens.Error500
val SuccessContainer = ColorSuccessContainer
val WarningContainer = ColorWarningContainer
val ErrorContainer = ColorErrorContainer
val InfoContainer = ColorTokens.Info500.copy(alpha = 0.2f)

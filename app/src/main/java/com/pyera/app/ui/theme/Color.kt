package com.pyera.app.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Brand Colors
val NeonYellow = Color(0xFFD4FF00)
val NeonYellowDark = Color(0xFFB8E600)
val NeonYellowLight = Color(0xFFE0FF33)

// Background Colors
val DarkGreen = Color(0xFF0A0E0D)
val SurfaceDark = Color(0xFF1A1F1D)
val SurfaceElevated = Color(0xFF242927)
val SurfaceOverlay = Color(0xFF2F3633)

// Semantic Colors (Enhanced for Accessibility)
val ColorSuccess = Color(0xFF4CAF50)
val ColorSuccessDark = Color(0xFF388E3C)
val ColorSuccessContainer = Color(0xFF1B2A12)
val ColorError = Color(0xFFFF5252)
val ColorErrorContainer = Color(0xFF2A0A0A)
val ColorWarning = Color(0xFFFFB300)
val ColorWarningContainer = Color(0xFF2A1F00)
val ColorInfo = Color(0xFF448AFF)

// Text Colors (WCAG AA Compliant)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B8B4)
val TextTertiary = Color(0xFF8B918F)
val TextDisabled = Color(0xFF5F6663)

// Interactive States
val ColorOverlayPressed = Color(0x1FFFFFFF)
val ColorOverlayHover = Color(0x0FFFFFFF)

// Borders
val ColorBorder = Color(0xFF3A433F)
val ColorBorderFocused = NeonYellow.copy(alpha = 0.5f)

// Currency Colors
val ColorIncome = ColorSuccess
val ColorExpense = ColorError
val ColorDebt = ColorWarning

// Legacy aliases for backward compatibility
val Inchworm = NeonYellow
val Gunmetal = SurfaceDark
val Orange = ColorWarning
val PaleViolet = ColorInfo
val BrightSnow = TextPrimary
val AmericanSilver = TextSecondary
val DeepBackground = DarkGreen
val CardBackground = SurfaceDark
val DarkBackground = DarkGreen
val AccentGreen = NeonYellow
val AccentGreenDim = NeonYellowDark
val PositiveChange = ColorSuccess
val NegativeChange = ColorError
val SuccessContainer = ColorSuccessContainer
val WarningContainer = ColorWarningContainer
val ErrorContainer = ColorErrorContainer
val InfoContainer = Color(0xFF0A1A2A)
val CardBorder = ColorBorder
val GlassOverlay = ColorOverlayHover
val GreenGlow = NeonYellow.copy(alpha = 0.2f)
val GreenGlowSubtle = NeonYellow.copy(alpha = 0.1f)
val CardGradientTop = SurfaceElevated
val CardGradientBottom = SurfaceDark

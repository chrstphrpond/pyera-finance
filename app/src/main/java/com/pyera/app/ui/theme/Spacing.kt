package com.pyera.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing system for Pyera Finance app
 * Provides consistent spacing values across the application
 */
object Spacing {
    val None = 0.dp
    val XXSmall = 2.dp
    val XSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val XLarge = 24.dp
    val XXLarge = 32.dp
    val XXXLarge = 48.dp
    
    // Component-specific spacing
    val CardPadding = 16.dp
    val ScreenPadding = 16.dp
    val DialogPadding = 24.dp
    val ListItemSpacing = 12.dp
    val SectionSpacing = 24.dp
}

/**
 * Border radius values for Pyera Finance app
 * Provides consistent corner radius values across the application
 */
object Radius {
    val none = 0.dp
    val sm = 4.dp
    val md = 8.dp
    val lg = 12.dp
    val xl = 16.dp
    val xxl = 24.dp
    val full = 9999.dp
    
    // Component-specific radius
    val Button = 12.dp
    val Card = 16.dp
    val Input = 12.dp
    val Dialog = 16.dp
    val Chip = 8.dp
}

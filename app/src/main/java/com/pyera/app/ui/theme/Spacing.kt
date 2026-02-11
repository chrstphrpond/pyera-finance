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
    val XLarge = 20.dp
    val XXLarge = 24.dp
    val XXXLarge = 32.dp
    val Huge = 48.dp
    
    // Component-specific spacing
    val CardPadding = 20.dp
    val ScreenPadding = 20.dp
    val DialogPadding = 24.dp
    val ListItemSpacing = 8.dp
    val SectionSpacing = 24.dp
}

/**
 * Border radius values for Pyera Finance app
 * Provides consistent corner radius values across the application
 */
object Radius {
    val none = 0.dp
    val sm = 6.dp
    val md = 10.dp
    val lg = 14.dp
    val xl = 18.dp
    val xxl = 22.dp
    val xxxl = 26.dp
    val full = 9999.dp
    
    // Component-specific radius
    val Button = 14.dp
    val Card = 18.dp
    val Input = 12.dp
    val Dialog = 20.dp
    val Chip = 10.dp
    val BottomBar = 28.dp
}

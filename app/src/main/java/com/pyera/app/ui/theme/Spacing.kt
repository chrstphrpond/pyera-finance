package com.pyera.app.ui.theme

import androidx.compose.ui.unit.dp

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
    
    // Component-specific
    val CardPadding = 16.dp
    val ScreenPadding = 16.dp
    val DialogPadding = 24.dp
    val ListItemSpacing = 12.dp
    val SectionSpacing = 24.dp
    
    // Legacy aliases for backward compatibility
    val xxs = XXSmall
    val xs = XSmall
    val sm = Small
    val ms = Medium
    val md = Large
    val ml = 20.dp
    val lg = XLarge
    val xl = XXLarge
    val xxl = XXXLarge
    val huge = 64.dp
}

/**
 * Pyera corner radius tokens for consistent rounded corners.
 */
object Radius {
    /** 4dp - Subtle rounding */
    val xs = 4.dp
    
    /** 8dp - Small rounding */
    val sm = 8.dp
    
    /** 12dp - Medium rounding */
    val md = 12.dp
    
    /** 16dp - Large rounding */
    val lg = 16.dp
    
    /** 20dp - Extra large rounding */
    val xl = 20.dp
    
    /** 24dp - Pill-like rounding */
    val xxl = 24.dp
    
    /** Full circle (use with equal width/height) */
    val full = 999.dp
}

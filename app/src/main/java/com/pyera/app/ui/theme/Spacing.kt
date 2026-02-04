package com.pyera.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Pyera spacing tokens following an 8dp grid system.
 * Use these tokens for consistent spacing throughout the app.
 */
object Spacing {
    /** 2dp - Extra extra small, for tight spacing */
    val xxs = 2.dp
    
    /** 4dp - Extra small, for dense layouts */
    val xs = 4.dp
    
    /** 8dp - Small, standard internal padding */
    val sm = 8.dp
    
    /** 12dp - Medium small, for moderate spacing */
    val ms = 12.dp
    
    /** 16dp - Medium, standard component spacing */
    val md = 16.dp
    
    /** 20dp - Medium large, for section breaks */
    val ml = 20.dp
    
    /** 24dp - Large, for major sections */
    val lg = 24.dp
    
    /** 32dp - Extra large, for screen padding */
    val xl = 32.dp
    
    /** 48dp - Extra extra large, for major separations */
    val xxl = 48.dp
    
    /** 64dp - Huge, for splash screens or hero sections */
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

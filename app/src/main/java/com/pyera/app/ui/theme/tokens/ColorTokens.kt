package com.pyera.app.ui.theme.tokens

import androidx.compose.ui.graphics.Color

/**
 * Complete color system for Pyera Finance redesign.
 * Uses "Midnight Slate" color palette with Trust Blue primary.
 */
object ColorTokens {

    // ============================================================================
    // PRIMARY PALETTE - Trust Blue
    // ============================================================================
    val Primary50 = Color(0xFFEEF4FF)
    val Primary100 = Color(0xFFD9E6FF)
    val Primary200 = Color(0xFFB3D1FF)
    val Primary300 = Color(0xFF80B3FF)
    val Primary400 = Color(0xFF4D91FF)
    val Primary500 = Color(0xFF3D5AFE)  // MAIN BRAND COLOR
    val Primary600 = Color(0xFF304FFE)  // Pressed state
    val Primary700 = Color(0xFF283593)  // Text on light backgrounds
    val Primary800 = Color(0xFF1A237E)  // Dark surfaces
    val Primary900 = Color(0xFF0D1B3E)  // Deepest primary

    // ============================================================================
    // SEMANTIC COLORS - Actions & Status
    // ============================================================================
    val Success50 = Color(0xFFE8F5E9)
    val Success100 = Color(0xFFC8E6C9)
    val Success500 = Color(0xFF00C853)  // Income, positive, growth
    val Success600 = Color(0xFF00B248)  // Pressed

    val Error50 = Color(0xFFFFEBEE)
    val Error100 = Color(0xFFFFCDD2)
    val Error500 = Color(0xFFFF5252)    // Expense, negative, alerts
    val Error600 = Color(0xFFD32F2F)    // Pressed

    val Warning50 = Color(0xFFFFF8E1)
    val Warning100 = Color(0xFFFFECB3)
    val Warning500 = Color(0xFFFFB300)  // Near limits, caution
    val Warning600 = Color(0xFFFFA000)  // Pressed

    val Info50 = Color(0xFFE3F2FD)
    val Info100 = Color(0xFFBBDEFB)
    val Info500 = Color(0xFF448AFF)     // Actions, links, info
    val Info600 = Color(0xFF2979FF)     // Pressed

    // ============================================================================
    // NEUTRAL PALETTE - Midnight Slate (Dark Mode First)
    // ============================================================================
    val Slate50 = Color(0xFFF8FAFC)     // Light mode backgrounds
    val Slate100 = Color(0xFFF1F5F9)    // Light mode surfaces
    val Slate200 = Color(0xFFE2E8F0)    // Light borders
    val Slate300 = Color(0xFFCBD5E1)    // Light secondary text
    val Slate400 = Color(0xFF94A3B8)    // Light tertiary text
    val Slate500 = Color(0xFF64748B)    // Mid gray
    val Slate600 = Color(0xFF475569)    // Dark secondary text
    val Slate700 = Color(0xFF334155)    // Dark borders, dividers
    val Slate800 = Color(0xFF1E293B)    // Dark elevated surfaces
    val Slate900 = Color(0xFF0F172A)    // Dark card backgrounds
    val Slate950 = Color(0xFF020617)    // Darkest app background

    // ============================================================================
    // SURFACE ELEVATIONS (Material 3 Dark Theme)
    // ============================================================================
    val SurfaceLevel0 = Slate950        // App background
    val SurfaceLevel1 = Slate900        // Cards, primary surfaces
    val SurfaceLevel2 = Slate800        // Elevated cards, dialogs
    val SurfaceLevel3 = Color(0xFF334155)  // Menus, popovers
    val SurfaceLevel4 = Color(0xFF475569)  // Highest elevation

    // ============================================================================
    // LEGACY ALIASES (For backward compatibility during migration)
    // ============================================================================
    @Deprecated("Use Primary500", ReplaceWith("Primary500"))
    val AccentGreen = Primary500

    @Deprecated("Use Primary500", ReplaceWith("Primary500"))
    val NeonYellow = Primary500

    @Deprecated("Use SurfaceLevel0", ReplaceWith("SurfaceLevel0"))
    val DarkGreen = SurfaceLevel0

    @Deprecated("Use SurfaceLevel1", ReplaceWith("SurfaceLevel1"))
    val SurfaceDark = SurfaceLevel1

    @Deprecated("Use SurfaceLevel2", ReplaceWith("SurfaceLevel2"))
    val SurfaceElevated = SurfaceLevel2

    @Deprecated("Use Error500", ReplaceWith("Error500"))
    val ColorError = Error500

    @Deprecated("Use Success500", ReplaceWith("Success500"))
    val ColorSuccess = Success500

    @Deprecated("Use Warning500", ReplaceWith("Warning500"))
    val ColorWarning = Warning500
}


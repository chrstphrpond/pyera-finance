# Pyera Finance Redesign - Complete Implementation Guide for GPT Codex

**Project Root:** `c:\coding\Pyera`  
**Package:** `com.pyera.app`  
**Framework:** Jetpack Compose + Material 3  
**Target:** Complete UI redesign from "Nocturne Ember" to "Midnight Slate"

---

## EXECUTION ORDER - Follow Strictly

Complete each section before moving to the next. Do not skip ahead.

---

## SECTION 1: Foundation (Complete First)

### Step 1.1: Create Directory Structure

```bash
# Create tokens directory
mkdir -p app/src/main/java/com/pyera/app/ui/theme/tokens

# Verify structure exists
ls -la app/src/main/java/com/pyera/app/ui/theme/
```

### Step 1.2: Create ColorTokens.kt

**File:** `app/src/main/java/com/pyera/app/ui/theme/tokens/ColorTokens.kt`

```kotlin
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
    val Primary500 = Color(0xFF3D5AFE)  // ⭐ MAIN BRAND COLOR
    val Primary600 = Color(0xFF304FFE)  // Pressed state
    val Primary700 = Color(0xFF283593)  // Text on light backgrounds
    val Primary800 = Color(0xFF1A237E)  // Dark surfaces
    val Primary900 = Color(0xFF0D1B3E)  // Deepest primary
    
    // ============================================================================
    // SEMANTIC COLORS - Actions & Status
    // ============================================================================
    val Success50 = Color(0xFFE8F5E9)
    val Success100 = Color(0xFFC8E6C9)
    val Success500 = Color(0xFF00C853)  // ⭐ Income, positive, growth
    val Success600 = Color(0xFF00B248)  // Pressed
    
    val Error50 = Color(0xFFFFEBEE)
    val Error100 = Color(0xFFFFCDD2)
    val Error500 = Color(0xFFFF5252)    // ⭐ Expense, negative, alerts
    val Error600 = Color(0xFFD32F2F)    // Pressed
    
    val Warning50 = Color(0xFFFFF8E1)
    val Warning100 = Color(0xFFFFECB3)
    val Warning500 = Color(0xFFFFB300)  // ⭐ Near limits, caution
    val Warning600 = Color(0xFFFFA000)  // Pressed
    
    val Info50 = Color(0xFFE3F2FD)
    val Info100 = Color(0xFFBBDEFB)
    val Info500 = Color(0xFF448AFF)     // ⭐ Actions, links, info
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
    val Slate800 = Color(0xFF1E293B)    // ⭐ Dark elevated surfaces
    val Slate900 = Color(0xFF0F172A)    // ⭐ Dark card backgrounds
    val Slate950 = Color(0xFF020617)    // ⭐ Darkest app background
    
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
```

### Step 1.3: Create SpacingTokens.kt

**File:** `app/src/main/java/com/pyera/app/ui/theme/tokens/SpacingTokens.kt`

```kotlin
package com.pyera.app.ui.theme.tokens

import androidx.compose.ui.unit.dp

/**
 * Spacing system based on 4dp base unit.
 * All spacing values are multiples of 4.
 */
object SpacingTokens {
    val None = 0.dp
    val ExtraSmall = 4.dp      // 1×
    val Small = 8.dp           // 2×
    val MediumSmall = 12.dp    // 3×
    val Medium = 16.dp         // 4× ⭐ Most common
    val MediumLarge = 20.dp    // 5×
    val Large = 24.dp          // 6×
    val ExtraLarge = 32.dp     // 8×
    val XXL = 48.dp            // 12×
    val XXXL = 64.dp           // 16×
}
```

### Step 1.4: Create RadiusTokens.kt

**File:** `app/src/main/java/com/pyera/app/ui/theme/tokens/RadiusTokens.kt`

```kotlin
package com.pyera.app.ui.theme.tokens

import androidx.compose.ui.unit.dp

/**
 * Corner radius system for consistent rounded corners.
 */
object RadiusTokens {
    val None = 0.dp
    val Small = 8.dp       // Chips, small buttons
    val Medium = 12.dp     // Cards, text fields
    val Large = 16.dp      // ⭐ Cards, dialogs, bottom sheets
    val ExtraLarge = 24.dp // Bottom sheets, large dialogs
    val Full = 1000.dp     // Pills, circles
}
```

### Step 1.5: Create ElevationTokens.kt

**File:** `app/src/main/java/com/pyera/app/ui/theme/tokens/ElevationTokens.kt`

```kotlin
package com.pyera.app.ui.theme.tokens

import androidx.compose.ui.unit.dp

/**
 * Elevation system for Material 3 surfaces.
 */
object ElevationTokens {
    val Level0 = 0.dp
    val Level1 = 1.dp   // Cards at rest
    val Level2 = 3.dp   // Elevated cards, FAB
    val Level3 = 6.dp   // Dialogs, menus
    val Level4 = 8.dp   // Navigation drawer
}
```

### Step 1.6: Update Type.kt

**File:** `app/src/main/java/com/pyera/app/ui/theme/Type.kt`

Replace entire file content:

```kotlin
package com.pyera.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material 3 Typography with custom money display styles.
 */
val Typography = Typography(
    // Display styles for large amounts/hero text
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    
    // Headlines for section headers
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    
    // Titles for cards and lists
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    // Body text
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    // Labels for buttons, chips
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * Money display typography with tabular figures for alignment.
 */
val MoneyLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 40.sp,
    lineHeight = 48.sp,
    letterSpacing = 0.sp
)

val MoneyMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.sp
)

val MoneySmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)
```

### Step 1.7: Update Theme.kt

**File:** `app/src/main/java/com/pyera/app/ui/theme/Theme.kt`

Replace entire file content:

```kotlin
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
```

### Step 1.8: Verify Foundation

Run these commands to verify:

```bash
# Check all token files exist
ls app/src/main/java/com/pyera/app/ui/theme/tokens/

# Expected output:
# ColorTokens.kt
# ElevationTokens.kt
# RadiusTokens.kt
# SpacingTokens.kt

# Build the project
./gradlew :app:compileDebugKotlin

# If build succeeds, continue to Section 2
```

---

## SECTION 2: Component Library (Complete After Section 1)

### Step 2.1: Create PyeraCard Component

**File:** `app/src/main/java/com/pyera/app/ui/components/PyeraCard.kt`

```kotlin
package com.pyera.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.ElevationTokens
import com.pyera.app.ui.theme.tokens.RadiusTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

/**
 * Card variants for different visual emphasis levels.
 */
enum class CardVariant {
    /**
     * Standard card with subtle border and low elevation.
     * Use for: Transaction items, settings, lists.
     */
    Default,
    
    /**
     * Elevated card with no border and higher elevation.
     * Use for: Hero sections, featured content, primary cards.
     */
    Elevated,
    
    /**
     * Outlined card with no background or elevation.
     * Use for: Disabled states, placeholders, secondary content.
     */
    Outlined
}

/**
 * Unified card component for Pyera Finance.
 * 
 * @param modifier Modifier for the card
 * @param variant Visual style variant (Default, Elevated, Outlined)
 * @param onClick Optional click handler (makes card clickable with ripple)
 * @param content Content inside the card
 */
@Composable
fun PyeraCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.Default,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val (backgroundColor, border, elevation) = when (variant) {
        CardVariant.Default -> Triple(
            ColorTokens.SurfaceLevel1,
            BorderStroke(1.dp, ColorTokens.Slate800),
            ElevationTokens.Level1
        )
        CardVariant.Elevated -> Triple(
            ColorTokens.SurfaceLevel2,
            null,
            ElevationTokens.Level2
        )
        CardVariant.Outlined -> Triple(
            Color.Transparent,
            BorderStroke(1.dp, ColorTokens.Slate700),
            ElevationTokens.Level0
        )
    }
    
    val shape = RoundedCornerShape(RadiusTokens.Large)
    
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            border = border,
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevation,
                pressedElevation = if (variant == CardVariant.Elevated) 
                    ElevationTokens.Level1 else elevation
            )
        ) {
            content()
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            border = border,
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevation
            )
        ) {
            content()
        }
    }
}
```

### Step 2.2: Create MoneyDisplay Component

**File:** `app/src/main/java/com/pyera/app/ui/components/MoneyDisplay.kt`

```kotlin
package com.pyera.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pyera.app.ui.theme.MoneyLarge
import com.pyera.app.ui.theme.MoneyMedium
import com.pyera.app.ui.theme.MoneySmall
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens
import java.text.NumberFormat
import java.util.Locale

/**
 * Size variants for money display.
 */
enum class MoneySize {
    /** Large size (40sp) for hero sections and total balances. */
    Large,
    
    /** Medium size (28sp) for list items and cards. */
    Medium,
    
    /** Small size (16sp) for compact displays and tables. */
    Small
}

/**
 * Displays monetary amounts with consistent formatting and styling.
 * 
 * Features:
 * - Currency symbol prefix
 * - Decimal part shown smaller
 * - Color coding for positive/negative
 * - Optional count-up animation
 * - Tabular number alignment
 * 
 * @param amount The monetary amount to display
 * @param currency Currency symbol (default: ₱)
 * @param size Size variant (Large, Medium, Small)
 * @param isPositive Optional color hint (true=green, false=red, null=default)
 * @param showSign Whether to show + or - prefix
 * @param animate Whether to animate the amount counting up
 * @param modifier Modifier for the component
 */
@Composable
fun MoneyDisplay(
    amount: Double,
    currency: String = "₱",
    size: MoneySize = MoneySize.Medium,
    isPositive: Boolean? = null,
    showSign: Boolean = false,
    animate: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Animate amount if requested
    val displayAmount = if (animate) {
        val animatedValue by animateFloatAsState(
            targetValue = amount.toFloat(),
            animationSpec = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            ),
            label = "money_animation"
        )
        animatedValue.toDouble()
    } else {
        amount
    }
    
    // Format the number
    val absAmount = kotlin.math.abs(displayAmount)
    val formatted = formatMoney(absAmount, currency)
    
    // Split into whole and decimal parts
    val decimalIndex = formatted.lastIndexOf('.')
    val (wholePart, decimalPart) = if (decimalIndex > 0) {
        formatted.substring(0, decimalIndex) to formatted.substring(decimalIndex)
    } else {
        formatted to ".00"
    }
    
    // Determine color
    val color = when {
        isPositive == true -> ColorTokens.Success500
        isPositive == false -> ColorTokens.Error500
        else -> LocalContentColor.current
    }
    
    // Get text styles based on size
    val (wholeStyle, decimalStyle) = when (size) {
        MoneySize.Large -> MoneyLarge to MoneyLarge.copy(
            fontWeight = FontWeight.Normal,
            fontSize = androidx.compose.ui.unit.sp.valueOf(24f).sp
        )
        MoneySize.Medium -> MoneyMedium to MoneyMedium.copy(
            fontWeight = FontWeight.Normal,
            fontSize = androidx.compose.ui.unit.sp.valueOf(18f).sp
        )
        MoneySize.Small -> MoneySmall to MoneySmall.copy(
            fontWeight = FontWeight.Normal,
            fontSize = androidx.compose.ui.unit.sp.valueOf(14f).sp
        )
    }
    
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
    ) {
        // Sign prefix
        if (showSign) {
            Text(
                text = if (amount >= 0) "+" else "−",
                style = wholeStyle,
                color = color,
                modifier = Modifier.padding(end = SpacingTokens.ExtraSmall)
            )
        }
        
        // Currency symbol (smaller, lighter)
        Text(
            text = currency,
            style = wholeStyle.copy(fontWeight = FontWeight.Normal),
            color = color.copy(alpha = 0.7f)
        )
        
        // Whole number part
        Text(
            text = wholePart.removePrefix(currency),
            style = wholeStyle,
            color = color
        )
        
        // Decimal part (smaller, lighter)
        Text(
            text = decimalPart,
            style = decimalStyle,
            color = color.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

/**
 * Formats a monetary amount with proper decimal places.
 */
private fun formatMoney(amount: Double, symbol: String): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    formatter.maximumFractionDigits = 2
    formatter.minimumFractionDigits = 2
    val formatted = formatter.format(amount)
    
    // Replace default currency symbol with custom one
    return formatted.replace(Regex("[^\\d.,]"), symbol)
}
```

[Content continues with more components and migration instructions...]

---

## Verification Checklist

Before completing, verify:

- [ ] All 4 token files created and compile
- [ ] Theme.kt updated with new color scheme
- [ ] Type.kt has money typography
- [ ] PyeraCard component works in all 3 variants
- [ ] MoneyDisplay animates correctly
- [ ] No old color imports remain in UI code
- [ ] App builds successfully: `./gradlew :app:assembleDebug`
- [ ] Both light and dark themes render correctly
- [ ] All touch targets are minimum 48dp
- [ ] Color contrast ratios meet WCAG AA

---

## Common Issues & Solutions

### Issue: "ColorTokens not found"
**Solution:** Ensure tokens package is imported:
```kotlin
import com.pyera.app.ui.theme.tokens.ColorTokens
```

### Issue: "Theme doesn't apply"
**Solution:** Verify `PyeraTheme` wraps content in MainActivity:
```kotlin
setContent {
    PyeraTheme {
        // Your content
    }
}
```

### Issue: "Colors look wrong in dark mode"
**Solution:** Use `SurfaceLevelX` tokens, not hardcoded hex values

### Issue: "MoneyDisplay doesn't animate"
**Solution:** Ensure `animate = true` and amount changes

---

**STOP:** Do not proceed until all items in Verification Checklist are complete.

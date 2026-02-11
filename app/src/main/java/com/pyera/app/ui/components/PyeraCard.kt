package com.pyera.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pyera.app.ui.theme.OverlayHover
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.ElevationTokens
import com.pyera.app.ui.theme.tokens.RadiusTokens

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
    cornerRadius: Dp = RadiusTokens.Large,
    borderColor: Color = ColorTokens.Slate800,
    borderWidth: Dp = 1.dp,
    containerColor: Color = ColorTokens.SurfaceLevel1,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Dp = ElevationTokens.Level1,
    enableGlassEffect: Boolean = false,
    gradientBrush: Brush? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    variant: CardVariant = CardVariant.Default,
    content: @Composable ColumnScope.() -> Unit
) {
    val (backgroundColor, border, resolvedElevation) = when (variant) {
        CardVariant.Default -> Triple(
            containerColor,
            BorderStroke(borderWidth, borderColor),
            elevation
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

    val effectiveGradient = if (variant == CardVariant.Default) {
        when {
            gradientBrush != null -> gradientBrush
            enableGlassEffect -> Brush.verticalGradient(
                colors = listOf(
                    OverlayHover,
                    Color.Transparent
                )
            )
            else -> null
        }
    } else {
        null
    }

    val cardColors = if (effectiveGradient != null) {
        CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = contentColor
        )
    } else {
        CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    }

    val shape = if (variant == CardVariant.Default) {
        RoundedCornerShape(cornerRadius)
    } else {
        RoundedCornerShape(RadiusTokens.Large)
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            shape = shape,
            colors = cardColors,
            border = border,
            elevation = CardDefaults.cardElevation(
                defaultElevation = resolvedElevation,
                pressedElevation = if (variant == CardVariant.Elevated) {
                    ElevationTokens.Level1
                } else {
                    resolvedElevation
                }
            )
        ) {
            if (effectiveGradient != null) {
                Box(
                    modifier = Modifier
                        .background(brush = effectiveGradient)
                        .background(backgroundColor)
                ) {
                    Column {
                        content()
                    }
                }
            } else {
                Column {
                    content()
                }
            }
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = cardColors,
            border = border,
            elevation = CardDefaults.cardElevation(
                defaultElevation = resolvedElevation
            )
        ) {
            if (effectiveGradient != null) {
                Box(
                    modifier = Modifier
                        .background(brush = effectiveGradient)
                        .background(backgroundColor)
                ) {
                    Column {
                        content()
                    }
                }
            } else {
                Column {
                    content()
                }
            }
        }
    }
}

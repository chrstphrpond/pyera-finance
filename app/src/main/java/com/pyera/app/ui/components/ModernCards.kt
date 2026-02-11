package com.pyera.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pyera.app.ui.theme.*
import com.pyera.app.ui.theme.tokens.SpacingTokens

/**
 * Modern card component with large rounded corners and subtle glassmorphism effect
 */
@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Radius.Card,
    backgroundColor: Color = SurfacePrimary,
    borderColor: Color = ColorBorderSubtle,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 0.dp,
    gradientBrush: Brush? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "card_scale"
    )

    val cardModifier = modifier
        .scale(scale)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    enabled = enabled,
                    onClick = onClick,
                    interactionSource = interactionSource,
                    indication = null
                )
            } else Modifier
        )

    PyeraCard(
        modifier = cardModifier,
        cornerRadius = cornerRadius,
        containerColor = backgroundColor,
        contentColor = MaterialTheme.colorScheme.onBackground,
        borderColor = borderColor,
        borderWidth = borderWidth,
        elevation = elevation
    ) {
        if (gradientBrush != null) {
            Box(
                modifier = Modifier.background(brush = gradientBrush)
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

/**
 * Glassmorphism card with subtle blur effect simulation
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Radius.Card,
    backgroundColor: Color = GlassBackground,
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    ModernCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        borderWidth = borderWidth,
        onClick = onClick,
        enabled = enabled,
        content = content
    )
}

/**
 * Accent card with a colored accent border and subtle gradient
 */
@Composable
fun AccentCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Radius.Card,
    accentColor: Color = PrimaryAccent,
    backgroundColor: Color = SurfacePrimary,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    ModernCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        backgroundColor = backgroundColor,
        borderColor = accentColor.copy(alpha = 0.3f),
        borderWidth = borderWidth,
        gradientBrush = Brush.verticalGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.05f),
                backgroundColor
            )
        ),
        onClick = onClick,
        enabled = enabled,
        content = content
    )
}

/**
 * Elevated card with a colored top accent line
 */
@Composable
fun ElevatedAccentCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Radius.Card,
    accentColor: Color = PrimaryAccent,
    backgroundColor: Color = SurfaceSecondary,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    PyeraCard(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .then(
                if (onClick != null) {
                    Modifier.clickable(enabled = enabled, onClick = onClick)
                } else Modifier
            ),
        cornerRadius = cornerRadius,
        containerColor = backgroundColor,
        contentColor = MaterialTheme.colorScheme.onBackground,
        borderWidth = 0.dp,
        elevation = 4.dp
    ) {
        Column {
            // Accent line at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(accentColor)
            )
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * Compact stat card for dashboard stats
 */
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    accentColor: Color = PrimaryAccent,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ModernCard(
        modifier = modifier,
        cornerRadius = Radius.lg,
        backgroundColor = SurfaceSecondary,
        borderColor = accentColor.copy(alpha = 0.2f),
        gradientBrush = Brush.verticalGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.08f),
                SurfaceSecondary
            )
        ),
        onClick = onClick,
        content = content
    )
}

/**
 * Transaction item card with subtle styling
 */
@Composable
fun TransactionCard(
    modifier: Modifier = Modifier,
    isIncome: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ModernCard(
        modifier = modifier,
        cornerRadius = Radius.md,
        backgroundColor = SurfaceSecondary,
        borderColor = ColorBorderSubtle,
        onClick = onClick,
        content = content
    )
}

/**
 * Quick action button card - square with icon and label
 */
@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    accentColor: Color = PrimaryAccent,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "quick_action_scale"
    )

    PyeraCard(
        modifier = modifier
            .scale(scale)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            ),
        cornerRadius = Radius.lg,
        containerColor = SurfaceSecondary,
        contentColor = MaterialTheme.colorScheme.onBackground,
        borderColor = accentColor.copy(alpha = 0.2f),
        borderWidth = 1.dp,
        elevation = 0.dp
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.1f),
                        SurfaceSecondary
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(SpacingTokens.Medium)
            ) {
                content()
            }
        }
    }
}

/**
 * Main FAB card - prominent action button
 */
@Composable
fun MainActionCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        label = "main_action_scale"
    )

    PyeraCard(
        modifier = modifier
            .scale(scale)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            ),
        cornerRadius = Radius.xl,
        containerColor = PrimaryAccentDark,
        contentColor = BackgroundPrimary,
        borderWidth = 0.dp,
        elevation = 8.dp
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryAccentLight,
                        PrimaryAccentDark
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                content()
            }
        }
    }
}

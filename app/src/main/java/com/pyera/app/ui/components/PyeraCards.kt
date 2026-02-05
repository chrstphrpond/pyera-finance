package com.pyera.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.pyera.app.ui.theme.*

@Composable
fun PyeraCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = ColorBorder,
    borderWidth: Dp = 1.dp,
    containerColor: Color = SurfaceElevated,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Dp = 0.dp,
    enableGlassEffect: Boolean = false,
    gradientBrush: Brush? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val effectiveGradient = when {
        gradientBrush != null -> gradientBrush
        enableGlassEffect -> Brush.verticalGradient(
            colors = listOf(
                ColorOverlayHover,
                Color.Transparent
            )
        )
        else -> null
    }

    val cardColors = if (effectiveGradient != null) {
        CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = contentColor
        )
    } else {
        CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    }

    val cardModifier = modifier
        .then(
            if (onClick != null) {
                Modifier.clickable(enabled = enabled, onClick = onClick)
            } else Modifier
        )

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = cardColors,
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        if (effectiveGradient != null) {
            Box(
                modifier = Modifier.background(
                    brush = effectiveGradient
                ).background(containerColor)
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

@Composable
fun PyeraFeatureCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    gradientColors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        SurfaceElevated
    ),
    borderColor: Color = ColorBorder,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    PyeraCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        borderColor = borderColor,
        gradientBrush = Brush.verticalGradient(colors = gradientColors),
        onClick = onClick,
        enabled = enabled
    ) {
        content()
    }
}

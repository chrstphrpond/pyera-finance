package com.pyera.app.ui.components
import com.pyera.app.ui.theme.tokens.ColorTokens

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pyera.app.ui.theme.ColorBorder

@Composable
fun PyeraFeatureCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    gradientColors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        ColorTokens.SurfaceLevel2
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




package com.pyera.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pyera.app.ui.theme.CardBackground
import com.pyera.app.ui.theme.CardBorder
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens
import com.pyera.app.ui.util.bounceClick

@Composable
fun CircularActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    containerSize: Dp = 56.dp,
    iconSize: Dp = SpacingTokens.Large,
    onClick: () -> Unit = {}
) {
    val containerColor = if (isHighlighted) ColorTokens.Primary500 else CardBackground
    val iconColor = if (isHighlighted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
    val labelColor = if (isHighlighted) ColorTokens.Primary500 else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier.width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(containerSize)
                .bounceClick(onClick = onClick)
                .clip(CircleShape)
                .then(
                    if (!isHighlighted) {
                        Modifier.border(1.dp, CardBorder, CircleShape)
                    } else {
                        Modifier
                    }
                )
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(iconSize)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = if (isHighlighted) FontWeight.Medium else FontWeight.Normal
            ),
            color = labelColor
        )
    }
}




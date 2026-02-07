package com.pyera.app.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pyera.app.ui.theme.*

enum class ButtonVariant { Primary, Secondary, Tertiary, Destructive }
enum class ButtonSize { Small, Medium, Large }

internal object PyeraButtonTokens {
    val shape = RoundedCornerShape(Radius.lg)

    fun height(size: ButtonSize) = when (size) {
        ButtonSize.Small -> 32.dp
        ButtonSize.Medium -> 48.dp
        ButtonSize.Large -> 56.dp
    }

    fun colors(variant: ButtonVariant) = when (variant) {
        ButtonVariant.Primary -> ButtonDefaults.buttonColors(
            containerColor = NeonYellow,
            contentColor = DarkGreen,
            disabledContainerColor = NeonYellow.copy(alpha = 0.38f),
            disabledContentColor = DarkGreen.copy(alpha = 0.38f)
        )
        ButtonVariant.Secondary -> ButtonDefaults.buttonColors(
            containerColor = SurfaceElevated,
            contentColor = TextPrimary
        )
        ButtonVariant.Tertiary -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = NeonYellow
        )
        ButtonVariant.Destructive -> ButtonDefaults.buttonColors(
            containerColor = ColorErrorContainer,
            contentColor = ColorError
        )
    }

    fun contentColor(variant: ButtonVariant) = when (variant) {
        ButtonVariant.Primary -> DarkGreen
        ButtonVariant.Secondary -> TextPrimary
        ButtonVariant.Tertiary -> NeonYellow
        ButtonVariant.Destructive -> ColorError
    }
}

@Composable
fun PyeraButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val colors = PyeraButtonTokens.colors(variant)
    val height = PyeraButtonTokens.height(size)
    val contentColor = PyeraButtonTokens.contentColor(variant)
    
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        colors = colors,
        enabled = enabled && !isLoading,
        shape = PyeraButtonTokens.shape
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = contentColor,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            content()
        }
    }
}

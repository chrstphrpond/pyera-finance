package com.pyera.app.ui.components
import com.pyera.app.ui.theme.tokens.ColorTokens

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
import androidx.compose.material3.MaterialTheme

enum class ButtonVariant { Primary, Secondary, Tertiary, Destructive }
enum class ButtonSize { Small, Medium, Large }

internal object PyeraButtonTokens {
    val shape = RoundedCornerShape(Radius.Button)

    fun height(size: ButtonSize) = when (size) {
        ButtonSize.Small -> 36.dp
        ButtonSize.Medium -> 48.dp
        ButtonSize.Large -> 56.dp
    }

    @Composable
    fun colors(variant: ButtonVariant) = when (variant) {
        ButtonVariant.Primary -> ButtonDefaults.buttonColors(
            containerColor = PrimaryAccent,
            contentColor = BackgroundPrimary,
            disabledContainerColor = PrimaryAccentDark.copy(alpha = 0.35f),
            disabledContentColor = BackgroundPrimary.copy(alpha = 0.45f)
        )
        ButtonVariant.Secondary -> ButtonDefaults.buttonColors(
            containerColor = SurfaceSecondary,
            contentColor = MaterialTheme.colorScheme.onBackground,
            disabledContainerColor = SurfaceSecondary.copy(alpha = 0.6f),
            disabledContentColor = TextMuted
        )
        ButtonVariant.Tertiary -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = PrimaryAccent
        )
        ButtonVariant.Destructive -> ButtonDefaults.buttonColors(
            containerColor = ColorErrorContainer,
            contentColor = ColorTokens.Error500
        )
    }

    @Composable
    fun contentColor(variant: ButtonVariant) = when (variant) {
        ButtonVariant.Primary -> BackgroundPrimary
        ButtonVariant.Secondary -> MaterialTheme.colorScheme.onBackground
        ButtonVariant.Tertiary -> PrimaryAccent
        ButtonVariant.Destructive -> ColorTokens.Error500
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



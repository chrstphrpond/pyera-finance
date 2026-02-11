package com.pyera.app.ui.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.pyera.app.ui.theme.BackgroundPrimary
import com.pyera.app.ui.theme.GradientEnd
import com.pyera.app.ui.theme.GradientStart

enum class ButtonState { Pressed, Idle }

fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    onClick: () -> Unit = {}
) = composed {
    var buttonState by remember { mutableStateOf(ButtonState.Idle) }
    val scale by animateFloatAsState(
        if (buttonState == ButtonState.Pressed) scaleDown else 1f,
        label = "Bounce Scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
        .pointerInput(Unit) {
            awaitPointerEventScope {
                buttonState = if (buttonState == ButtonState.Pressed) {
                    waitForUpOrCancellation()
                    ButtonState.Idle
                } else {
                    awaitFirstDown(false)
                    ButtonState.Pressed
                }
            }
        }
}

fun Modifier.pyeraBackground(forceDark: Boolean = false) = composed {
    val colorScheme = MaterialTheme.colorScheme
    val gradient = remember(
        forceDark,
        colorScheme.surface,
        colorScheme.background,
        colorScheme.surfaceVariant
    ) {
        val colors = if (forceDark) {
            listOf(GradientStart, BackgroundPrimary, GradientEnd)
        } else {
            listOf(colorScheme.surface, colorScheme.background, colorScheme.surfaceVariant)
        }
        Brush.verticalGradient(colors = colors)
    }
    this.background(gradient)
}

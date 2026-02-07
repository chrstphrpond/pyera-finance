package com.pyera.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pyera.app.ui.theme.*

/**
 * Reusable authentication button component for Pyera auth screens.
 * Supports loading state with smooth animation transition.
 */
@Composable
fun PyeraAuthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val contentColor = PyeraButtonTokens.contentColor(ButtonVariant.Primary)

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(PyeraButtonTokens.height(ButtonSize.Large)),
        enabled = enabled && !isLoading,
        colors = PyeraButtonTokens.colors(ButtonVariant.Primary),
        shape = PyeraButtonTokens.shape
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
            },
            label = "button_content"
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }
        }
    }
}

/**
 * Secondary authentication button for alternative actions.
 * Used for "Sign in with Google" and other social login options.
 */
@Composable
fun PyeraAuthSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(PyeraButtonTokens.height(ButtonSize.Medium)),
        enabled = enabled,
        colors = PyeraButtonTokens.colors(ButtonVariant.Secondary),
        shape = PyeraButtonTokens.shape
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    icon()
                }
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

/**
 * Social login button with icon support.
 */
@Composable
fun PyeraSocialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(PyeraButtonTokens.height(ButtonSize.Medium)),
        enabled = enabled,
        colors = PyeraButtonTokens.colors(ButtonVariant.Secondary),
        shape = PyeraButtonTokens.shape
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.align(Alignment.CenterStart)) {
                icon()
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

/**
 * Text button for auth screens (e.g., "Forgot Password?", "Sign Up").
 */
@Composable
fun PyeraAuthTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) AccentGreen else TextTertiary,
            fontWeight = FontWeight.Medium
        )
    }
}

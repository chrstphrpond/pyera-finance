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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = NeonYellow,
            contentColor = DarkGreen,
            disabledContainerColor = NeonYellow.copy(alpha = 0.38f),
            disabledContentColor = DarkGreen.copy(alpha = 0.38f)
        ),
        shape = RoundedCornerShape(Radius.lg)
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
                    color = DarkGreen,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkGreen
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
            .height(48.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceElevated,
            contentColor = TextPrimary
        ),
        shape = RoundedCornerShape(Radius.lg)
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
            .height(48.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceElevated,
            contentColor = TextPrimary
        ),
        shape = RoundedCornerShape(Radius.lg)
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

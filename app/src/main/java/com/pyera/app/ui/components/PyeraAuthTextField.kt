package com.pyera.app.ui.components
import com.pyera.app.ui.theme.tokens.ColorTokens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.pyera.app.ui.theme.*

/**
 * Reusable authentication text field component for Pyera auth screens.
 * Supports password visibility toggle, inline error messages, and proper keyboard handling.
 */
@Composable
fun PyeraAuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordVisibilityChange: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    placeholder: String? = null,
    singleLine: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)) } },
            leadingIcon = leadingIcon?.let {
                { Icon(it, contentDescription = "$label icon", tint = if (isError) ColorTokens.Error500 else MaterialTheme.colorScheme.onSurfaceVariant) }
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { onPasswordVisibilityChange?.invoke() }) {
                        Icon(
                            imageVector = if (isPasswordVisible)
                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible)
                                "Hide password" else "Show password",
                            tint = if (isError) ColorTokens.Error500 else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                }
            } else null,
            isError = isError,
            singleLine = singleLine,
            visualTransformation = if (isPassword && !isPasswordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onImeAction?.invoke() },
                onDone = { onImeAction?.invoke() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorTokens.Primary500,
                unfocusedBorderColor = if (isError) ColorTokens.Error500 else ColorBorder,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedLabelColor = if (isError) ColorTokens.Error500 else ColorTokens.Primary500,
                unfocusedLabelColor = if (isError) ColorTokens.Error500 else MaterialTheme.colorScheme.onSurfaceVariant,
                errorBorderColor = ColorTokens.Error500,
                errorLabelColor = ColorTokens.Error500,
                errorSupportingTextColor = ColorTokens.Error500,
                focusedContainerColor = ColorTokens.SurfaceLevel2.copy(alpha = 0.5f),
                unfocusedContainerColor = ColorTokens.SurfaceLevel2.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(Radius.lg),
            supportingText = if (isError && errorMessage != null) {
                { Text(errorMessage) }
            } else null
        )
    }
}

/**
 * Enhanced password text field with strength indicator and visibility toggle.
 */
@Composable
fun PyeraPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPasswordVisible: Boolean = false,
    onPasswordVisibilityChange: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
    showStrengthIndicator: Boolean = false,
    passwordStrength: PasswordStrength = PasswordStrength.EMPTY,
    leadingIcon: ImageVector? = null,
    placeholder: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PyeraAuthTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            isPassword = true,
            isPasswordVisible = isPasswordVisible,
            onPasswordVisibilityChange = onPasswordVisibilityChange,
            isError = isError,
            errorMessage = errorMessage,
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
            onImeAction = onImeAction,
            leadingIcon = leadingIcon ?: Icons.Default.Lock,
            placeholder = placeholder
        )

        // Password strength indicator
        AnimatedVisibility(
            visible = showStrengthIndicator && value.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                PasswordStrengthBar(strength = passwordStrength)
            }
        }
    }
}

/**
 * Password strength indicator bar with label.
 */
@Composable
private fun PasswordStrengthBar(strength: PasswordStrength) {
    val progress = when (strength) {
        PasswordStrength.EMPTY -> 0f
        PasswordStrength.WEAK -> 0.33f
        PasswordStrength.MEDIUM -> 0.66f
        PasswordStrength.STRONG -> 1f
    }

    val color = when (strength) {
        PasswordStrength.EMPTY -> CardBorder
        PasswordStrength.WEAK -> ColorTokens.Error500
        PasswordStrength.MEDIUM -> ColorTokens.Warning500
        PasswordStrength.STRONG -> ColorTokens.Success500
    }

    val label = when (strength) {
        PasswordStrength.EMPTY -> ""
        PasswordStrength.WEAK -> "Weak"
        PasswordStrength.MEDIUM -> "Medium"
        PasswordStrength.STRONG -> "Strong"
    }

    Column {
        // Progress bar background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(CardBorder.copy(alpha = 0.3f))
        ) {
            // Progress fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }

        if (label.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Strength: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Enum representing password strength levels.
 */
enum class PasswordStrength {
    EMPTY, WEAK, MEDIUM, STRONG
}

/**
 * Calculate password strength based on various criteria.
 */
fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.EMPTY

    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when (score) {
        0, 1, 2 -> PasswordStrength.WEAK
        3 -> PasswordStrength.MEDIUM
        else -> PasswordStrength.STRONG
    }
}



package com.pyera.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.pyera.app.R
import com.pyera.app.ui.theme.*
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

/**
 * A reusable confirmation dialog with customizable title, message, and button colors.
 * Typically used for destructive actions like delete operations.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = stringResource(R.string.dialog_confirm_button),
    dismissText: String = stringResource(R.string.dialog_cancel_button),
    confirmColor: Color = ColorTokens.Error500,
    icon: ImageVector? = Icons.Default.Warning,
    iconTint: Color = confirmColor,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorTokens.SurfaceLevel2,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        icon = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = stringResource(R.string.dialog_warning_content_desc),
                    tint = iconTint,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = confirmColor,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = confirmText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = dismissText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}

/**
 * A reusable error dialog to display error messages to the user.
 * Contains a single dismiss button.
 */
@Composable
fun ErrorDialog(
    title: String = stringResource(R.string.dialog_error_title),
    message: String,
    dismissText: String = stringResource(R.string.dialog_ok_button),
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorTokens.SurfaceLevel2,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        icon = {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = stringResource(R.string.dialog_error_content_desc),
                tint = ColorTokens.Error500,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorTokens.Error500,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = dismissText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}

/**
 * A reusable success dialog to confirm successful operations.
 * Automatically dismisses or provides a dismiss button.
 */
@Composable
fun SuccessDialog(
    title: String = stringResource(R.string.dialog_success_title),
    message: String,
    dismissText: String = "OK",
    autoDismissDelay: Long? = null,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorTokens.SurfaceLevel2,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.dialog_success_content_desc),
                tint = ColorTokens.Success500,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorTokens.Success500,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = dismissText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )

    // Auto-dismiss functionality if delay is provided
    autoDismissDelay?.let { delay ->
        androidx.compose.runtime.LaunchedEffect(delay, onDismiss) {
            kotlinx.coroutines.delay(delay)
            onDismiss()
        }
    }
}

/**
 * Info dialog for general informational messages.
 */
@Composable
fun InfoDialog(
    title: String,
    message: String,
    dismissText: String = stringResource(R.string.dialog_info_got_it),
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorTokens.SurfaceLevel2,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorTokens.Primary500,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = dismissText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}

/**
 * A reusable error container component to display error states inline.
 * Features an error icon, title, and optional message in a styled card.
 *
 * @param title The error title text
 * @param message Optional detailed error message
 * @param modifier Modifier for customizing the layout
 */
@Composable
fun ErrorContainer(
    title: String,
    message: String? = null,
    modifier: Modifier = Modifier
) {
    PyeraCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = ColorTokens.Error500.copy(alpha = 0.1f),
        borderColor = ColorTokens.Error500.copy(alpha = 0.3f),
        borderWidth = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = stringResource(R.string.dialog_error_content_desc),
                tint = ColorTokens.Error500,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = ColorTokens.Error500,
                textAlign = TextAlign.Center
            )

            message?.let {
                Spacer(modifier = Modifier.height(SpacingTokens.Small))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTokens.Error500.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * A reusable error container component for displaying error states with optional retry.
 * Used when content fails to load.
 */
@Composable
fun ErrorContainer(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = stringResource(R.string.dialog_error_content_desc),
            tint = ColorTokens.Error500,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(SpacingTokens.Small))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = ColorTokens.Error500,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            PyeraButton(
                onClick = onRetry,
                variant = ButtonVariant.Primary
            ) {
                Text(stringResource(R.string.dialog_error_retry))
            }
        }
    }
}

/**
 * A standardized confirmation dialog with support for destructive actions.
 * Use this for consistent confirmation dialogs across the app.
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = stringResource(R.string.dialog_confirm_button),
    dismissText: String = stringResource(R.string.dialog_cancel_button),
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val confirmColor = if (isDestructive) ColorTokens.Error500 else ColorTokens.Primary500
    val confirmTextColor = if (isDestructive) Color.White else MaterialTheme.colorScheme.onPrimary
    val icon = if (isDestructive) Icons.Default.Warning else null
    val iconTint = if (isDestructive) ColorTokens.Error500 else ColorTokens.Primary500

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorTokens.SurfaceLevel2,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        icon = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = stringResource(R.string.dialog_warning_content_desc),
                    tint = iconTint,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = confirmColor,
                    contentColor = confirmTextColor
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = confirmText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = dismissText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}

/**
 * A loading dialog that blocks user interaction while showing a loading indicator.
 * Use this for operations that require the user to wait.
 */
@Composable
fun LoadingDialog(
    message: String = stringResource(R.string.dialog_loading_message)
) {
    Dialog(
        onDismissRequest = { /* Cannot dismiss by clicking outside */ }
    ) {
        PyeraCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = ColorTokens.SurfaceLevel2,
            borderWidth = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingTokens.Large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = ColorTokens.Primary500,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(SpacingTokens.Medium))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}



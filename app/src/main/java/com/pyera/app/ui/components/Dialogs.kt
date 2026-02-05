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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    confirmColor: Color = ColorError,
    icon: ImageVector? = Icons.Default.Warning,
    iconTint: Color = confirmColor,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
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
                    color = TextSecondary,
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
        containerColor = SurfaceElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        icon = {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = stringResource(R.string.dialog_error_content_desc),
                tint = ColorError,
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
                    containerColor = ColorError,
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
        containerColor = SurfaceElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.dialog_success_content_desc),
                tint = ColorSuccess,
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
                    containerColor = ColorSuccess,
                    contentColor = DarkGreen
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
        containerColor = SurfaceElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
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
                    containerColor = NeonYellow,
                    contentColor = DarkGreen
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
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ColorError.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = ColorError.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = stringResource(R.string.dialog_error_content_desc),
                tint = ColorError,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = ColorError,
                textAlign = TextAlign.Center
            )

            message?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorError.copy(alpha = 0.8f),
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = stringResource(R.string.dialog_error_content_desc),
            tint = ColorError,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = ColorError,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
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
    val confirmColor = if (isDestructive) ColorError else NeonYellow
    val confirmTextColor = if (isDestructive) Color.White else DarkGreen
    val icon = if (isDestructive) Icons.Default.Warning else null
    val iconTint = if (isDestructive) ColorError else NeonYellow

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
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
                    color = TextSecondary,
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
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = NeonYellow,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

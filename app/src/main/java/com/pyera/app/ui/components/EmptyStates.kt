package com.pyera.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pyera.app.ui.theme.*

/**
 * A reusable empty state component that displays when there's no data to show.
 * Features a large icon, title, optional subtitle, and optional action button.
 *
 * @param icon The icon to display (64.dp, tinted with NeonYellow)
 * @param title The main title text (white, 18.sp)
 * @param subtitle Optional subtitle text (gray, 14.sp)
 * @param actionLabel Optional label for the action button
 * @param onAction Optional callback when the action button is clicked
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    PyeraCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = SurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Large icon with NeonYellow tint
            Icon(
                imageVector = icon,
                contentDescription = "$title icon",
                tint = NeonYellow,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title text (white, 18.sp)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                textAlign = TextAlign.Center
            )

            // Optional subtitle (gray, 14.sp)
            subtitle?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Optional action button
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(20.dp))
                PyeraButton(
                    onClick = onAction,
                    variant = ButtonVariant.Primary
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

/**
 * A compact empty state for use in smaller spaces like list items.
 */
@Composable
fun CompactEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$title icon",
            tint = NeonYellow.copy(alpha = 0.7f),
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        subtitle?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PyeraEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.XXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(Spacing.Large))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Spacing.Small))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(Spacing.XLarge))
            PyeraButton(
                onClick = onAction,
                variant = ButtonVariant.Primary
            ) {
                Text(actionLabel)
            }
        }
    }
}

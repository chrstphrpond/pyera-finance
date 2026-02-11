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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.ui.res.stringResource
import com.pyera.app.R
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

/**
 * A reusable empty state component that displays when there's no data to show.
 * Features a large icon, title, optional subtitle, and optional action button.
 *
 * @param icon The icon to display (64.dp, tinted with ColorTokens.Primary500)
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
        containerColor = ColorTokens.SurfaceLevel2
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.ExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Large icon with ColorTokens.Primary500 tint
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ColorTokens.Primary500,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

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
                Spacer(modifier = Modifier.height(SpacingTokens.Small))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Optional action button
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(SpacingTokens.MediumLarge))
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
            .padding(SpacingTokens.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ColorTokens.Primary500.copy(alpha = 0.7f),
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        subtitle?.let {
            Spacer(modifier = Modifier.height(SpacingTokens.ExtraSmall))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * A flexible empty state component that can be used anywhere.
 *
 * @param icon The icon to display
 * @param title The main title text
 * @param description The description text
 * @param modifier Modifier for layout
 * @param actionLabel Optional label for the action button
 * @param onAction Optional callback when the action button is clicked
 */
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
            .padding(SpacingTokens.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = ColorTokens.Primary500
        )
        
        Spacer(modifier = Modifier.height(SpacingTokens.Medium))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(SpacingTokens.Small))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(SpacingTokens.MediumLarge))
            PyeraButton(
                onClick = onAction,
                variant = ButtonVariant.Primary
            ) {
                Text(actionLabel)
            }
        }
    }
}

// ============================================================================
// Pre-configured Empty States for Specific Features
// ============================================================================

/**
 * Empty state for the Transactions screen.
 */
@Composable
fun EmptyTransactions(
    onAddClick: () -> Unit
) {
    PyeraEmptyState(
        icon = Icons.Default.AttachMoney,
        title = stringResource(R.string.empty_state_transactions_title),
        description = stringResource(R.string.empty_state_transactions_description),
        actionLabel = stringResource(R.string.empty_state_transactions_button),
        onAction = onAddClick
    )
}

/**
 * Empty state for the Debt screen (I Owe tab).
 */
@Composable
fun EmptyDebt(
    onAddClick: () -> Unit,
    isIOwe: Boolean = true
) {
    val title = if (isIOwe) {
        stringResource(R.string.empty_state_no_debts_owe_title)
    } else {
        stringResource(R.string.empty_state_no_debts_owed_title)
    }
    val description = if (isIOwe) {
        stringResource(R.string.empty_state_no_debts_owe_description)
    } else {
        stringResource(R.string.empty_state_no_debts_owed_description)
    }
    val icon = if (isIOwe) Icons.Default.MoneyOff else Icons.Default.AttachMoney
    
    PyeraEmptyState(
        icon = icon,
        title = title,
        description = description,
        actionLabel = stringResource(R.string.empty_state_debt_button_first),
        onAction = onAddClick
    )
}

/**
 * Empty state for the Budget screen.
 */
@Composable
fun EmptyBudget(
    onCreateClick: () -> Unit
) {
    PyeraEmptyState(
        icon = Icons.Default.AccountBalanceWallet,
        title = stringResource(R.string.empty_state_budget_title),
        description = stringResource(R.string.empty_state_budget_description),
        actionLabel = stringResource(R.string.empty_state_budget_button),
        onAction = onCreateClick
    )
}

/**
 * Empty state for the Savings screen.
 */
@Composable
fun EmptySavings(
    onAddClick: () -> Unit
) {
    PyeraEmptyState(
        icon = Icons.Default.Savings,
        title = stringResource(R.string.empty_state_savings_title),
        description = stringResource(R.string.empty_state_savings_description),
        actionLabel = stringResource(R.string.empty_state_savings_button),
        onAction = onAddClick
    )
}

/**
 * Empty state for search results.
 */
@Composable
fun EmptySearch(
    query: String,
    onClearSearch: (() -> Unit)? = null
) {
    PyeraEmptyState(
        icon = Icons.Default.Search,
        title = if (query.isBlank()) stringResource(R.string.empty_state_search_blank_title) else stringResource(R.string.empty_state_search_title),
        description = if (query.isBlank()) {
            stringResource(R.string.empty_state_search_blank_description)
        } else {
            stringResource(R.string.empty_state_search_no_results_description, query)
        },
        actionLabel = if (onClearSearch != null && query.isNotBlank()) "Clear Search" else null,
        onAction = onClearSearch
    )
}

/**
 * Empty state for the Investments screen.
 */
@Composable
fun EmptyInvestments(
    onAddClick: () -> Unit
) {
    PyeraEmptyState(
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        title = stringResource(R.string.empty_state_investments_title),
        description = stringResource(R.string.empty_state_investments_description),
        actionLabel = stringResource(R.string.empty_state_investments_button),
        onAction = onAddClick
    )
}



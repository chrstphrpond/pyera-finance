package com.pyera.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.RadiusTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * A swipeable transaction list item with edit/delete actions.
 * Displays transaction details including category icon, description,
 * amount, account name, and date.
 *
 * @param transaction The transaction entity to display
 * @param category The associated category (null if not found)
 * @param account The associated account (null if not found)
 * @param onClick Callback when the item is clicked
 * @param onEdit Callback when edit action is triggered
 * @param onDelete Callback when delete action is triggered
 * @param modifier Modifier for customizing the layout
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionListItem(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    account: AccountEntity?,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            when (dismissValue) {
                DismissValue.DismissedToStart -> {
                    onDelete()
                    false
                }
                DismissValue.DismissedToEnd -> {
                    onEdit()
                    false
                }
                DismissValue.Default -> false
            }
        }
    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
        background = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    DismissValue.DismissedToStart -> ColorTokens.Error500.copy(alpha = 0.9f)
                    DismissValue.DismissedToEnd -> ColorTokens.Info500.copy(alpha = 0.9f)
                    DismissValue.Default -> ColorTokens.SurfaceLevel2
                },
                label = "swipe_background_color"
            )
            val alignment = when (direction) {
                DismissDirection.StartToEnd -> Alignment.CenterStart
                DismissDirection.EndToStart -> Alignment.CenterEnd
                null -> Alignment.Center
            }
            val icon = when (direction) {
                DismissDirection.StartToEnd -> Icons.Default.Edit
                DismissDirection.EndToStart -> Icons.Default.Delete
                null -> Icons.Default.Edit
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = SpacingTokens.ExtraSmall)
                    .clip(RoundedCornerShape(RadiusTokens.Large))
                    .background(color)
                    .padding(horizontal = SpacingTokens.Large),
                contentAlignment = alignment
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (direction == DismissDirection.StartToEnd) "Edit" else "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(SpacingTokens.Large)
                )
            }
        },
        dismissContent = {
            TransactionItemContent(
                transaction = transaction,
                category = category,
                account = account,
                onClick = onClick,
                modifier = modifier
            )
        }
    )
}

/**
 * Internal composable for the transaction item content.
 * Separated for better organization and reusability.
 */
@Composable
private fun TransactionItemContent(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    account: AccountEntity?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == "INCOME"
    val signedAmount = if (isIncome) transaction.amount else -transaction.amount

    PyeraCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SpacingTokens.ExtraSmall),
        variant = CardVariant.Default,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(
                category = category,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(SpacingTokens.Medium))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.ExtraSmall)
            ) {
                Text(
                    text = transaction.note.takeIf { it.isNotBlank() }
                        ?: category?.name
                        ?: "Transaction",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Small)
                ) {
                    Text(
                        text = category?.name ?: "Uncategorized",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (account != null) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )
                        )

                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = formatTransactionDate(transaction.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }

            Spacer(modifier = Modifier.width(SpacingTokens.Small))

            MoneyDisplay(
                amount = signedAmount,
                isPositive = isIncome,
                size = MoneySize.Small,
                showSign = true
            )
        }
    }
}

/**
 * Category icon with colored circular background.
 *
 * @param category The category entity (null shows default)
 * @param modifier Modifier for the icon container
 */
@Composable
private fun CategoryIcon(
    category: CategoryEntity?,
    modifier: Modifier = Modifier
) {
    val fallbackColor = ColorTokens.Primary500
    val backgroundColor = category?.color?.let { Color(it) } ?: fallbackColor
    val iconText = category?.name?.take(1)?.uppercase() ?: "?"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.2f))
    ) {
        Text(
            text = iconText,
            color = backgroundColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Formats a timestamp into a readable date string.
 *
 * @param timestamp Epoch timestamp in milliseconds
 * @return Formatted date string (e.g., "Today, 2:30 PM" or "Jan 15, 2:30 PM")
 */
private fun formatTransactionDate(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val transactionDate = Calendar.getInstance().apply { time = date }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    return when {
        isSameDay(transactionDate, now) -> "Today, ${timeFormat.format(date)}"
        isYesterday(transactionDate, now) -> "Yesterday, ${timeFormat.format(date)}"
        else -> dateFormat.format(date)
    }
}

/**
 * Checks if two calendar instances represent the same day.
 */
private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

/**
 * Checks if the first calendar is yesterday relative to the second.
 */
private fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
    val yesterday = cal2.clone() as Calendar
    yesterday.add(Calendar.DAY_OF_YEAR, -1)
    return isSameDay(cal1, yesterday)
}

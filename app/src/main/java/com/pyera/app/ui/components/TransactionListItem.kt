package com.pyera.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

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
@OptIn(ExperimentalMaterial3Api::class)
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
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                DismissValue.DismissedToStart -> {
                    onDelete()
                    false // Don't actually dismiss, let the dialog handle it
                }
                DismissValue.DismissedToEnd -> {
                    onEdit()
                    false // Don't actually dismiss
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
                    DismissValue.DismissedToStart -> ColorError.copy(alpha = 0.9f)
                    DismissValue.DismissedToEnd -> ColorInfo.copy(alpha = 0.9f)
                    DismissValue.Default -> SurfaceElevated
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
            val contentColor = when (direction) {
                DismissDirection.StartToEnd -> TextPrimary
                DismissDirection.EndToStart -> TextPrimary
                null -> TextPrimary
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (direction == DismissDirection.StartToEnd) "Edit" else "Delete",
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        dismissContent = {
            TransactionItemContent(
                transaction = transaction,
                category = category,
                account = account,
                onClick = onClick
            )
        },
        modifier = modifier
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
    onClick: () -> Unit
) {
    val isIncome = transaction.type == "INCOME"
    val amountColor = if (isIncome) ColorIncome else ColorExpense
    val amountPrefix = if (isIncome) "+" else "-"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceElevated
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon with colored background
            CategoryIcon(
                category = category,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Transaction Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Description
                Text(
                    text = transaction.note.takeIf { it.isNotBlank() } 
                        ?: category?.name 
                        ?: "Transaction",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Category and Account info row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category name
                    Text(
                        text = category?.name ?: "Uncategorized",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    // Dot separator
                    if (account != null) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(TextTertiary)
                        )

                        // Account name
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Date
                Text(
                    text = formatTransactionDate(transaction.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$amountPrefix ₱${String.format("%,.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp
                    ),
                    color = amountColor,
                    fontWeight = FontWeight.Bold
                )
            }
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
    val backgroundColor = category?.color?.let { Color(it) } ?: TextTertiary
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
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
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

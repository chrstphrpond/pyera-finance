package com.pyera.app.ui.transaction

import androidx.compose.animation.AnimatedVisibility

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.ui.components.*
import androidx.compose.ui.res.stringResource
import com.pyera.app.R
import com.pyera.app.ui.theme.*


/**
 * Transaction List Screen with enhanced search, filter, sort, and swipe actions.
 * Features:
 * - Real-time search by description, category, or amount
 * - Filter chips for type (All/Income/Expense), category, and date range
 * - Sort dropdown (date, amount, category)
 * - Date-grouped list with sticky-style headers
 * - Pull-to-refresh
 * - Swipe to edit/delete with confirmation
 * - Empty state with CTA
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onAddTransaction: () -> Unit = {},
    onEditTransaction: (TransactionEntity) -> Unit = {},
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDeleteDialogId by rememberSaveable { mutableStateOf<Long?>(null) }
    val showDeleteDialog = showDeleteDialogId?.let { id -> transactions.find { it.id == id } }
    }

    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = state.isRefreshing
    )

    // Delete confirmation dialog
    showDeleteDialog?.let { transaction ->
        ConfirmationDialog(
            title = stringResource(R.string.transaction_list_delete_dialog_title),
            message = stringResource(R.string.transaction_list_delete_dialog_message),
            confirmText = stringResource(R.string.transaction_list_delete_button),
            dismissText = stringResource(R.string.transaction_list_cancel_button),
            isDestructive = true,
            onConfirm = {
                viewModel.deleteTransaction(transaction)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        // Top App Bar with Search and Filters
        Surface(
            color = DeepBackground,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header Row with Title and Sort
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.transaction_list_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )

                    // Sort Dropdown
                    SortDropdown(
                        selectedSort = state.selectedSort,
                        onSortSelected = viewModel::sortTransactions
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = viewModel::searchTransactions,
                    placeholder = stringResource(R.string.transaction_list_search_placeholder)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Type Filter Chips
                TransactionFilterChips(
                    selectedFilter = state.selectedFilter,
                    onFilterSelected = viewModel::filterTransactions
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date Range and Category Filters Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date Range Chips
                    DateRangeFilterChips(
                        selectedDateRange = state.dateRangeFilter,
                        onDateRangeSelected = viewModel::filterByDateRange,
                        modifier = Modifier.weight(1f)
                    )

                    // Category Filter
                    CategoryFilterChip(
                        categories = state.categories,
                        selectedCategoryId = state.selectedCategoryId,
                        onCategorySelected = viewModel::filterByCategory
                    )

                    // Clear Filters Button
                    ClearFiltersChip(
                        visible = state.searchQuery.isNotBlank() ||
                                state.selectedFilter != TransactionFilter.ALL ||
                                state.dateRangeFilter != DateRangeFilter.ALL ||
                                state.selectedCategoryId != null,
                        onClear = viewModel::clearFilters
                    )
                }
            }
        }

        // Content with SwipeRefresh
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = viewModel::refreshTransactions,
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    backgroundColor = SurfaceElevated,
                    contentColor = NeonYellow
                )
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    // Initial loading state
                    state.isLoading && state.transactions.isEmpty() -> {
                        LoadingIndicator(
                            message = stringResource(R.string.transaction_list_loading_message),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    // Empty state after filters applied
                    state.filteredTransactions.isEmpty() && 
                        (state.searchQuery.isNotBlank() ||
                         state.selectedFilter != TransactionFilter.ALL ||
                         state.dateRangeFilter != DateRangeFilter.ALL ||
                         state.selectedCategoryId != null) -> {
                        EmptyStateWithFilters(
                            onClearFilters = viewModel::clearFilters,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    // Empty state - no transactions at all
                    state.filteredTransactions.isEmpty() -> {
                        EmptyStateNoTransactions(
                            onAddTransaction = onAddTransaction,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    // Transaction list
                    else -> {
                        TransactionGroupedList(
                            groupedTransactions = viewModel.getGroupedTransactions(),
                            categories = state.categories,
                            accounts = state.accounts,
                            onDelete = { showDeleteDialog = it },
                            onEdit = onEditTransaction
                        )
                    }
                }
            }
        }
    }
}

/**
 * Search bar with clear button.
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String
) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                    singleLine = true
                )
            }

            AnimatedVisibility(
                visible = query.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.transaction_list_clear_search_content_desc),
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Grouped transaction list with date headers.
 */
@Composable
private fun TransactionGroupedList(
    groupedTransactions: Map<String, List<TransactionEntity>>,
    categories: List<CategoryEntity>,
    accounts: List<com.pyera.app.data.local.entity.AccountEntity>,
    onDelete: (TransactionEntity) -> Unit,
    onEdit: (TransactionEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        groupedTransactions.forEach { (dateHeader, transactions) ->
            // Date Header
            item(key = "header_$dateHeader") {
                DateHeader(date = dateHeader)
            }

            // Transactions for this date
            items(
                items = transactions,
                key = { it.id }
            ) { transaction ->
                val category = categories.find { it.id == transaction.categoryId }
                val account = accounts.find { it.id == transaction.accountId }

                TransactionListItem(
                    transaction = transaction,
                    category = category,
                    account = account,
                    onClick = { onEdit(transaction) },
                    onEdit = { onEdit(transaction) },
                    onDelete = { onDelete(transaction) }
                )
            }
        }

        // Bottom spacing for FAB if needed
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Date header for transaction groups.
 */
@Composable
private fun DateHeader(date: String) {
    Text(
        text = date.uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(
            fontSize = 14.sp
        ),
        color = TextSecondary,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

/**
 * Empty state when no transactions exist at all.
 */
@Composable
private fun EmptyStateNoTransactions(
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmptyTransactions(onAddClick = onAddTransaction)
    }
}

/**
 * Empty state when filters return no results.
 */
@Composable
private fun EmptyStateWithFilters(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmptySearch(
            query = "",
            onClearSearch = onClearFilters
        )
    }
}

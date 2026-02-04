package com.pyera.app.ui.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.pyera.app.ui.components.EmptyState
import com.pyera.app.ui.components.LoadingIndicator
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.AccentGreen
import com.pyera.app.ui.theme.ColorError
import com.pyera.app.ui.theme.ColorSuccess
import com.pyera.app.ui.theme.DeepBackground
import com.pyera.app.ui.theme.SurfaceElevated
import com.pyera.app.ui.theme.TextPrimary
import com.pyera.app.ui.theme.TextSecondary
import com.pyera.app.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    onAddTransaction: () -> Unit = {},
    onEditTransaction: (TransactionEntity) -> Unit = {},
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<TransactionEntity?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = state.isLoading
    )

    // Delete confirmation dialog
    showDeleteDialog?.let { transaction ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Transaction") },
            text = { 
                Text("Are you sure you want to delete this transaction? This action cannot be undone.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(transaction)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ColorError)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            },
            containerColor = SurfaceElevated,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        // Top App Bar with Search
        Surface(
            color = DeepBackground,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search Bar
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = viewModel::setSearchQuery,
                    placeholder = "Search transactions..."
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Filter Chips Row
                FilterChipsRow(
                    state = state,
                    onTypeFilterChange = viewModel::setTypeFilter,
                    onDateRangeFilterChange = viewModel::setDateRangeFilter,
                    onCategoryFilterChange = viewModel::setCategoryFilter,
                    onClearFilters = viewModel::clearFilters
                )
            }
        }

        // Content with SwipeRefresh
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                viewModel.refresh()
            },
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = AccentGreen
                )
            },
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                state.isLoading && state.transactions.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator(message = "Loading transactions...")
                    }
                }
                state.filteredTransactions.isEmpty() -> {
                    EmptyStateContent(
                        hasFilters = state.searchQuery.isNotBlank() || 
                                    state.typeFilter != TransactionTypeFilter.ALL ||
                                    state.dateRangeFilter != DateRangeFilter.ALL ||
                                    state.selectedCategoryFilter != null,
                        onAddTransaction = onAddTransaction,
                        onClearFilters = viewModel::clearFilters
                    )
                }
                else -> {
                    TransactionGroupedList(
                        groupedTransactions = viewModel.getGroupedTransactions(),
                        categories = state.categories,
                        onDelete = { showDeleteDialog = it },
                        onEdit = onEditTransaction
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String
) {
    Surface(
        color = SurfaceElevated,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
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
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    state: TransactionState,
    onTypeFilterChange: (TransactionTypeFilter) -> Unit,
    onDateRangeFilterChange: (DateRangeFilter) -> Unit,
    onCategoryFilterChange: (Int?) -> Unit,
    onClearFilters: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showCategoryDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Type Filter Chips
        FilterChip(
            selected = state.typeFilter == TransactionTypeFilter.ALL,
            onClick = { onTypeFilterChange(TransactionTypeFilter.ALL) },
            label = { Text("All") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AccentGreen,
                selectedLabelColor = DeepBackground
            )
        )
        
        FilterChip(
            selected = state.typeFilter == TransactionTypeFilter.INCOME,
            onClick = { onTypeFilterChange(TransactionTypeFilter.INCOME) },
            label = { Text("Income") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ColorSuccess.copy(alpha = 0.3f),
                selectedLabelColor = ColorSuccess
            )
        )
        
        FilterChip(
            selected = state.typeFilter == TransactionTypeFilter.EXPENSE,
            onClick = { onTypeFilterChange(TransactionTypeFilter.EXPENSE) },
            label = { Text("Expense") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ColorError.copy(alpha = 0.3f),
                selectedLabelColor = ColorError
            )
        )

        // Date Range Chips
        FilterChip(
            selected = state.dateRangeFilter == DateRangeFilter.THIS_WEEK,
            onClick = { 
                onDateRangeFilterChange(
                    if (state.dateRangeFilter == DateRangeFilter.THIS_WEEK) 
                        DateRangeFilter.ALL 
                    else 
                        DateRangeFilter.THIS_WEEK
                )
            },
            label = { Text("This Week") },
            leadingIcon = if (state.dateRangeFilter == DateRangeFilter.THIS_WEEK) {
                { Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp)) }
            } else null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AccentGreen.copy(alpha = 0.2f),
                selectedLabelColor = AccentGreen
            )
        )
        
        FilterChip(
            selected = state.dateRangeFilter == DateRangeFilter.THIS_MONTH,
            onClick = { 
                onDateRangeFilterChange(
                    if (state.dateRangeFilter == DateRangeFilter.THIS_MONTH) 
                        DateRangeFilter.ALL 
                    else 
                        DateRangeFilter.THIS_MONTH
                )
            },
            label = { Text("This Month") },
            leadingIcon = if (state.dateRangeFilter == DateRangeFilter.THIS_MONTH) {
                { Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp)) }
            } else null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AccentGreen.copy(alpha = 0.2f),
                selectedLabelColor = AccentGreen
            )
        )

        // Category Filter Dropdown
        Box {
            FilterChip(
                selected = state.selectedCategoryFilter != null,
                onClick = { showCategoryDropdown = true },
                label = { 
                    val categoryName = state.categories
                        .find { it.id == state.selectedCategoryFilter }?.name ?: "Category"
                    Text(categoryName) 
                },
                leadingIcon = if (state.selectedCategoryFilter != null) {
                    { Icon(Icons.Default.Inbox, null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentGreen.copy(alpha = 0.2f),
                    selectedLabelColor = AccentGreen
                )
            )
            
            DropdownMenu(
                expanded = showCategoryDropdown,
                onDismissRequest = { showCategoryDropdown = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All Categories") },
                    onClick = {
                        onCategoryFilterChange(null)
                        showCategoryDropdown = false
                    }
                )
                
                Divider()
                
                state.categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(category.color))
                            )
                        },
                        onClick = {
                            onCategoryFilterChange(category.id)
                            showCategoryDropdown = false
                        }
                    )
                }
            }
        }

        // Clear Filters
        if (state.searchQuery.isNotBlank() || 
            state.typeFilter != TransactionTypeFilter.ALL ||
            state.dateRangeFilter != DateRangeFilter.ALL ||
            state.selectedCategoryFilter != null) {
            
            AssistChip(
                onClick = onClearFilters,
                label = { Text("Clear") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Clear, 
                        null, 
                        modifier = Modifier.size(16.dp),
                        tint = TextSecondary
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = TextSecondary
                )
            )
        }
    }
}

@Composable
fun TransactionGroupedList(
    groupedTransactions: Map<String, List<TransactionEntity>>,
    categories: List<CategoryEntity>,
    onDelete: (TransactionEntity) -> Unit,
    onEdit: (TransactionEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        groupedTransactions.forEach { (dateHeader, transactions) ->
            item(key = "header_$dateHeader") {
                DateHeader(date = dateHeader)
            }
            
            items(
                items = transactions,
                key = { it.id }
            ) { transaction ->
                val category = categories.find { it.id == transaction.categoryId }
                TransactionItemCard(
                    transaction = transaction,
                    category = category,
                    onClick = { onEdit(transaction) }
                )
            }
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Text(
        text = date,
        style = MaterialTheme.typography.titleSmall,
        color = TextSecondary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    onClick: () -> Unit
) {
    PyeraCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        borderColor = TextTertiary.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(category?.color ?: TextTertiary.hashCode()))
            ) {
                Text(
                    text = category?.name?.take(1)?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (transaction.note.isNotBlank()) transaction.note else (category?.name ?: "Transaction"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = category?.name ?: "Uncategorized",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(TextTertiary)
                    )
                    
                    Text(
                        text = formatTime(transaction.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount
            val isIncome = transaction.type == "INCOME"
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncome) "+" else "-"} ₱${String.format("%,.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isIncome) ColorSuccess else ColorError,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyStateContent(
    hasFilters: Boolean,
    onAddTransaction: () -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (hasFilters) {
            EmptyState(
                icon = Icons.Default.Search,
                title = "No results found",
                subtitle = "Try adjusting your filters or search query"
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClearFilters,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
            ) {
                Text("Clear Filters", color = DeepBackground)
            }
        } else {
            EmptyState(
                icon = Icons.Default.Add,
                title = "No transactions yet",
                subtitle = "Start tracking your finances by adding your first transaction"
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddTransaction,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = DeepBackground)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Transaction", color = DeepBackground)
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

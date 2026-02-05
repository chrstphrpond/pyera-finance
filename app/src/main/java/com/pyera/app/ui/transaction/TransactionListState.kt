package com.pyera.app.ui.transaction

import androidx.compose.runtime.Immutable
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.TransactionEntity

/**
 * Filter options for transaction type
 */
enum class TransactionFilter {
    ALL, INCOME, EXPENSE
}

/**
 * Sort options for transaction list
 */
enum class TransactionSort {
    DATE_DESC,
    DATE_ASC,
    AMOUNT_DESC,
    AMOUNT_ASC,
    CATEGORY_ASC
}

/**
 * Enhanced state class for Transaction List screen
 */
@Immutable
data class TransactionListState(
    val transactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    
    // Search
    val searchQuery: String = "",
    
    // Filter
    val selectedFilter: TransactionFilter = TransactionFilter.ALL,
    val selectedCategoryId: Int? = null,
    val dateRangeFilter: DateRangeFilter = DateRangeFilter.ALL,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    
    // Sort
    val selectedSort: TransactionSort = TransactionSort.DATE_DESC
)

/**
 * Date range filter options
 */
enum class DateRangeFilter {
    ALL, TODAY, THIS_WEEK, THIS_MONTH, CUSTOM
}

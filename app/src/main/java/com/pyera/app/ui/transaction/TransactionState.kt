package com.pyera.app.ui.transaction

import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.TransactionEntity

enum class TransactionTypeFilter {
    ALL, INCOME, EXPENSE
}

enum class DateRangeFilter {
    ALL, THIS_WEEK, THIS_MONTH, CUSTOM
}

data class TransactionState(
    val transactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    
    // Search and filter states
    val searchQuery: String = "",
    val typeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val dateRangeFilter: DateRangeFilter = DateRangeFilter.ALL,
    val selectedCategoryFilter: Int? = null,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null
)

package com.pyera.app.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.domain.repository.AccountRepository
import com.pyera.app.domain.repository.AuthRepository
import com.pyera.app.domain.repository.CategoryRepository
import com.pyera.app.domain.repository.TransactionRepository
import com.pyera.app.domain.repository.TransactionRuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import android.database.sqlite.SQLiteException
import android.net.Uri
import com.pyera.app.domain.repository.OcrRepository
import com.pyera.app.domain.smart.SmartCategorizer
import com.pyera.app.util.ValidationUtils
import java.util.Calendar
import javax.inject.Inject

/**
 * Legacy transaction type filter used by older UI code paths.
 */
enum class TransactionTypeFilter {
    ALL,
    INCOME,
    EXPENSE
}

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository,
    private val ocrRepository: OcrRepository,
    private val smartCategorizer: SmartCategorizer,
    private val transactionRuleRepository: TransactionRuleRepository
) : ViewModel() {
    
    private val currentUserId: String
        get() = authRepository.currentUser?.uid ?: ""

    private val _state = MutableStateFlow(TransactionListState())
    val state: StateFlow<TransactionListState> = _state.asStateFlow()

    init {
        loadData()
    }

    // Cache date formatters
    private val dateFormatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    private val calendar = java.util.Calendar.getInstance()

    private fun loadData() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions()
                .distinctUntilChanged()
                .onStart { _state.update { it.copy(isLoading = true) } }
                .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
                .collect { transactions ->
                    _state.update { 
                        val newState = it.copy(isLoading = false, transactions = transactions)
                        newState.copy(filteredTransactions = applyFiltersAndSort(newState))
                    }
                }
        }

        viewModelScope.launch {
            categoryRepository.getAllCategories()
                .distinctUntilChanged()
                .collect { categories ->
                    if (categories.isNotEmpty()) {
                        _state.update { it.copy(categories = categories) }
                    }
                }
        }
        
        viewModelScope.launch {
            accountRepository.getActiveAccounts()
                .distinctUntilChanged()
                .collect { accounts ->
                    _state.update { 
                        it.copy(
                            accounts = accounts,
                            defaultAccount = accounts.find { acc -> acc.isDefault } ?: accounts.firstOrNull()
                        )
                    }
                }
        }
    }

    /**
     * Refreshes the transaction list.
     * Sets isRefreshing to true during the operation for pull-to-refresh indicators.
     */
    fun refreshTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            try {
                transactionRepository.getAllTransactions()
                    .catch { e -> 
                        _state.update { it.copy(error = e.message, isRefreshing = false) }
                    }
                    .collect { transactions ->
                        val newState = _state.value.copy(
                            transactions = transactions, 
                            isRefreshing = false, 
                            error = null
                        )
                        _state.update { newState.copy(filteredTransactions = applyFiltersAndSort(newState)) }
                    }
            } catch (e: IOException) {
                _state.update { it.copy(error = "Network error: ${e.message}", isRefreshing = false) }
            } catch (e: SQLiteException) {
                _state.update { it.copy(error = "Database error: ${e.message}", isRefreshing = false) }
            }
        }
    }

    /**
     * Clears any error message in the state.
     * Should be called after showing the error (e.g., in a snackbar).
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    // ==================== SEARCH ====================

    /**
     * Search transactions by query string.
     * Searches in description, category name, and amount.
     */
    fun searchTransactions(query: String) {
        _state.update { 
            val newState = it.copy(searchQuery = query)
            newState.copy(filteredTransactions = applyFiltersAndSort(newState))
        }
    }

    // ==================== FILTER ====================

    /**
     * Filter transactions by type (All, Income, Expense).
     */
    fun filterTransactions(filter: TransactionFilter) {
        _state.update { 
            val newState = it.copy(selectedFilter = filter)
            newState.copy(filteredTransactions = applyFiltersAndSort(newState))
        }
    }

    /**
     * Filter transactions by category ID.
     * Pass null to clear category filter.
     */
    fun filterByCategory(categoryId: Int?) {
        _state.update { 
            val newState = it.copy(selectedCategoryId = categoryId)
            newState.copy(filteredTransactions = applyFiltersAndSort(newState))
        }
    }

    /**
     * Filter transactions by date range.
     */
    fun filterByDateRange(dateRange: DateRangeFilter) {
        _state.update { 
            val newState = it.copy(dateRangeFilter = dateRange)
            newState.copy(filteredTransactions = applyFiltersAndSort(newState))
        }
    }

    /**
     * Set custom date range filter.
     */
    fun setCustomDateRange(startDate: Long?, endDate: Long?) {
        _state.update { 
            val newState = it.copy(
                customStartDate = startDate,
                customEndDate = endDate,
                dateRangeFilter = DateRangeFilter.CUSTOM
            )
            newState.copy(filteredTransactions = applyFiltersAndSort(newState))
        }
    }

    /**
     * Clear all filters and search query.
     */
    fun clearFilters() {
        _state.update { 
            val newState = it.copy(
                searchQuery = "",
                selectedFilter = TransactionFilter.ALL,
                dateRangeFilter = DateRangeFilter.ALL,
                selectedCategoryId = null,
                customStartDate = null,
                customEndDate = null,
                selectedSort = TransactionSort.DATE_DESC
            )
            newState.copy(filteredTransactions = applyFiltersAndSort(newState))
        }
    }

    // ==================== SORT ====================

    /**
     * Sort transactions by the specified sort option.
     */
    fun sortTransactions(sort: TransactionSort) {
        _state.update { 
            val newState = it.copy(selectedSort = sort)
            newState.copy(filteredTransactions = applyFiltersAndSort(newState))
        }
    }

    // ==================== DELETE ====================

    /**
     * Delete a transaction by its ID.
     * Should be called after user confirmation.
     */
    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            try {
                val transaction = state.value.transactions.find { it.id == id }
                transaction?.let {
                    transactionRepository.deleteTransaction(it)
                }
            } catch (e: IOException) {
                _state.update { it.copy(error = "Network error: ${e.message}") }
            } catch (e: SQLiteException) {
                _state.update { it.copy(error = "Database error: ${e.message}") }
            }
        }
    }

    /**
     * Delete a transaction entity directly.
     */
    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transaction)
            } catch (e: IOException) {
                _state.update { it.copy(error = "Network error: ${e.message}") }
            } catch (e: SQLiteException) {
                _state.update { it.copy(error = "Database error: ${e.message}") }
            }
        }
    }

    // ==================== PRIVATE HELPERS ====================

    /**
     * Apply all filters and sorting to the transactions list.
     * This combines search, type filter, category filter, date range, and sorting.
     */
    private fun applyFiltersAndSort(state: TransactionListState): List<TransactionEntity> {
        var result = state.transactions

        // Apply search query filter
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            result = result.filter { transaction ->
                transaction.note.lowercase().contains(query) ||
                transaction.amount.toString().contains(query) ||
                state.categories.find { it.id == transaction.categoryId }?.name?.lowercase()?.contains(query) == true
            }
        }

        // Apply type filter
        result = when (state.selectedFilter) {
            TransactionFilter.INCOME -> result.filter { it.type == "INCOME" }
            TransactionFilter.EXPENSE -> result.filter { it.type == "EXPENSE" }
            TransactionFilter.ALL -> result
        }

        // Apply date range filter
        result = applyDateRangeFilter(result, state)

        // Apply category filter
        if (state.selectedCategoryId != null) {
            result = result.filter { it.categoryId == state.selectedCategoryId }
        }

        // Apply sorting
        result = applySorting(result, state.selectedSort)

        return result
    }

    /**
     * Apply date range filter to transactions.
     */
    private fun applyDateRangeFilter(
        transactions: List<TransactionEntity>, 
        state: TransactionListState
    ): List<TransactionEntity> {
        val calendar = Calendar.getInstance()
        
        return when (state.dateRangeFilter) {
            DateRangeFilter.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1
                transactions.filter { it.date in startOfDay..endOfDay }
            }
            DateRangeFilter.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfWeek = calendar.timeInMillis
                transactions.filter { it.date >= startOfWeek }
            }
            DateRangeFilter.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.timeInMillis
                transactions.filter { it.date >= startOfMonth }
            }
            DateRangeFilter.CUSTOM -> {
                val start = state.customStartDate
                val end = state.customEndDate
                if (start != null && end != null) {
                    transactions.filter { it.date in start..end }
                } else {
                    transactions
                }
            }
            DateRangeFilter.ALL -> transactions
        }
    }

    /**
     * Apply sorting to transactions.
     */
    private fun applySorting(
        transactions: List<TransactionEntity>, 
        sort: TransactionSort
    ): List<TransactionEntity> {
        return when (sort) {
            TransactionSort.DATE_DESC -> transactions.sortedByDescending { it.date }
            TransactionSort.DATE_ASC -> transactions.sortedBy { it.date }
            TransactionSort.AMOUNT_DESC -> transactions.sortedByDescending { it.amount }
            TransactionSort.AMOUNT_ASC -> transactions.sortedBy { it.amount }
            TransactionSort.CATEGORY_ASC -> transactions.sortedBy { 
                _state.value.categories.find { cat -> cat.id == it.categoryId }?.name ?: "" 
            }
        }
    }

    // ==================== GROUPING FOR DISPLAY ====================

    /**
     * Group transactions by date for display with date headers.
     * Returns a map of date header string to list of transactions.
     */
    fun getGroupedTransactions(): Map<String, List<TransactionEntity>> {
        val transactions = state.value.filteredTransactions
        if (transactions.isEmpty()) return emptyMap()

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val yesterday = Calendar.getInstance().apply {
            timeInMillis = today
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis

        // Group by date header
        val grouped = transactions.groupBy { transaction ->
            when {
                transaction.date >= today -> "Today"
                transaction.date >= yesterday -> "Yesterday"
                else -> dateFormatter.format(java.util.Date(transaction.date))
            }
        }

        // Sort each group by date (newest first within each group)
        return grouped.mapValues { (_, txs) ->
            txs.sortedByDescending { it.date }
        }
    }

    // ==================== LEGACY COMPATIBILITY ====================

    /**
     * Legacy refresh method - delegates to refreshTransactions.
     */
    fun refresh() = refreshTransactions()

    /**
     * Legacy search method - delegates to searchTransactions.
     */
    fun setSearchQuery(query: String) = searchTransactions(query)

    /**
     * Legacy type filter method - delegates to filterTransactions.
     */
    fun setTypeFilter(filter: TransactionTypeFilter) {
        val newFilter = when (filter) {
            TransactionTypeFilter.ALL -> TransactionFilter.ALL
            TransactionTypeFilter.INCOME -> TransactionFilter.INCOME
            TransactionTypeFilter.EXPENSE -> TransactionFilter.EXPENSE
        }
        filterTransactions(newFilter)
    }

    /**
     * Legacy date range filter method - delegates to filterByDateRange.
     */
    fun setDateRangeFilter(filter: DateRangeFilter) = filterByDateRange(filter)

    /**
     * Legacy category filter method - delegates to filterByCategory.
     */
    fun setCategoryFilter(categoryId: Int?) = filterByCategory(categoryId)

    // ==================== TRANSACTION CRUD ====================

    fun addTransaction(transaction: TransactionEntity) {
        // Validate input
        val noteValidation = ValidationUtils.validateTransactionDescription(transaction.note)
        if (noteValidation is ValidationUtils.ValidationResult.Error) {
            _state.update { it.copy(error = noteValidation.message) }
            return
        }

        val amountValidation = ValidationUtils.validateTransactionAmount(transaction.amount.toString())
        if (amountValidation is ValidationUtils.ValidationResult.Error) {
            _state.update { it.copy(error = amountValidation.message) }
            return
        }
        
        // Proceed with adding transaction
        viewModelScope.launch {
            try {
                var finalTransaction = transaction
                
                // Auto-categorization: Priority = User Rules > SmartCategorizer > Manual
                if (transaction.categoryId == null || transaction.categoryId <= 0) {
                    // 1. Try user-defined rules first (highest priority)
                    var categoryId = transactionRuleRepository.applyRulesToTransaction(
                        userId = currentUserId,
                        description = transaction.note
                    )
                    
                    // 2. Fall back to SmartCategorizer if no rule matches
                    if (categoryId == null) {
                        val predictedCategoryName = smartCategorizer.predict(transaction.note)
                        if (predictedCategoryName != null) {
                            val category = categoryRepository.getCategoryByName(predictedCategoryName)
                            categoryId = category?.id
                        }
                    }
                    
                    // Apply the category if found
                    if (categoryId != null) {
                        finalTransaction = transaction.copy(categoryId = categoryId)
                    }
                }
                
                val categoryValidation = ValidationUtils.validateTransactionCategory(
                    finalTransaction.categoryId?.toLong()
                )
                if (categoryValidation is ValidationUtils.ValidationResult.Error) {
                    _state.update { it.copy(error = categoryValidation.message) }
                    return@launch
                }

                transactionRepository.insertTransaction(finalTransaction)
            } catch (e: IOException) {
                _state.update { it.copy(error = "Network error: ${e.message}") }
            } catch (e: SQLiteException) {
                _state.update { it.copy(error = "Database error: ${e.message}") }
            }
        }
    }
    
    /**
     * Creates a categorization rule from an existing transaction.
     * Used when user manually categorizes a transaction and wants to save it as a rule.
     */
    suspend fun createRuleFromTransaction(
        description: String,
        categoryId: Int
    ): Result<Long> {
        return transactionRuleRepository.createRuleFromTransaction(
            userId = currentUserId,
            description = description,
            categoryId = categoryId
        )
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                transactionRepository.updateTransaction(transaction)
            } catch (e: IOException) {
                _state.update { it.copy(error = "Network error: ${e.message}") }
            } catch (e: SQLiteException) {
                _state.update { it.copy(error = "Database error: ${e.message}") }
            }
        }
    }

    suspend fun processReceipt(uri: Uri): com.pyera.app.domain.ocr.ReceiptParser.ReceiptData {
        return ocrRepository.processReceipt(uri)
    }

    /**
     * Shows an error message that can be displayed in a snackbar.
     * This allows screens to trigger error messages from UI events.
     */
    fun showError(message: String) {
        _state.update { it.copy(error = message) }
    }
}

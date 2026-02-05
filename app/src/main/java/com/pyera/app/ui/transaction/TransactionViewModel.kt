package com.pyera.app.ui.transaction

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.data.repository.CategoryRepository
import com.pyera.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.IOException
import android.database.sqlite.SQLiteException
import android.net.Uri
import com.pyera.app.data.repository.OcrRepository

import com.pyera.app.domain.smart.SmartCategorizer
import com.pyera.app.util.ValidationUtils
import java.util.Calendar

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val ocrRepository: OcrRepository,
    private val smartCategorizer: SmartCategorizer
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionState())
    val state: StateFlow<TransactionState> = _state.asStateFlow()

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
                        newState.copy(filteredTransactions = applyFilters(newState))
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
    }

    /**
     * Refreshes the transaction list.
     * Sets isRefreshing to true during the operation for pull-to-refresh indicators.
     */
    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            try {
                // Re-trigger data collection by reloading
                transactionRepository.getAllTransactions()
                    .catch { e -> 
                        _state.update { it.copy(error = e.message, isRefreshing = false) }
                    }
                    .collect { transactions ->
                        val newState = _state.value.copy(transactions = transactions, isRefreshing = false, error = null)
                        _state.update { newState.copy(filteredTransactions = applyFilters(newState)) }
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

    // Search and Filter Methods
    fun setSearchQuery(query: String) {
        _state.update { 
            val newState = it.copy(searchQuery = query)
            newState.copy(filteredTransactions = applyFilters(newState))
        }
    }

    fun setTypeFilter(filter: TransactionTypeFilter) {
        _state.update { 
            val newState = it.copy(typeFilter = filter)
            newState.copy(filteredTransactions = applyFilters(newState))
        }
    }

    fun setDateRangeFilter(filter: DateRangeFilter) {
        _state.update { 
            val newState = it.copy(dateRangeFilter = filter)
            newState.copy(filteredTransactions = applyFilters(newState))
        }
    }

    fun setCategoryFilter(categoryId: Int?) {
        _state.update { 
            val newState = it.copy(selectedCategoryFilter = categoryId)
            newState.copy(filteredTransactions = applyFilters(newState))
        }
    }

    fun setCustomDateRange(startDate: Long?, endDate: Long?) {
        _state.update { 
            val newState = it.copy(
                customStartDate = startDate,
                customEndDate = endDate,
                dateRangeFilter = DateRangeFilter.CUSTOM
            )
            newState.copy(filteredTransactions = applyFilters(newState))
        }
    }

    fun clearFilters() {
        _state.update { 
            val newState = it.copy(
                searchQuery = "",
                typeFilter = TransactionTypeFilter.ALL,
                dateRangeFilter = DateRangeFilter.ALL,
                selectedCategoryFilter = null,
                customStartDate = null,
                customEndDate = null
            )
            newState.copy(filteredTransactions = applyFilters(newState))
        }
    }

    private fun applyFilters(state: TransactionState): List<TransactionEntity> {
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
        result = when (state.typeFilter) {
            TransactionTypeFilter.INCOME -> result.filter { it.type == "INCOME" }
            TransactionTypeFilter.EXPENSE -> result.filter { it.type == "EXPENSE" }
            TransactionTypeFilter.ALL -> result
        }

        // Apply date range filter
        val calendar = Calendar.getInstance()
        result = when (state.dateRangeFilter) {
            DateRangeFilter.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startOfWeek = calendar.timeInMillis
                result.filter { it.date >= startOfWeek }
            }
            DateRangeFilter.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startOfMonth = calendar.timeInMillis
                result.filter { it.date >= startOfMonth }
            }
            DateRangeFilter.CUSTOM -> {
                val start = state.customStartDate
                val end = state.customEndDate
                if (start != null && end != null) {
                    result.filter { it.date in start..end }
                } else {
                    result
                }
            }
            DateRangeFilter.ALL -> result
        }

        // Apply category filter
        if (state.selectedCategoryFilter != null) {
            result = result.filter { it.categoryId == state.selectedCategoryFilter }
        }

        return result.sortedByDescending { it.date }
    }

    // Group transactions by date for display
    fun getGroupedTransactions(): Map<String, List<TransactionEntity>> {
        val transactions = state.value.filteredTransactions
        if (transactions.isEmpty()) return emptyMap()

        val today = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.timeInMillis

        return transactions.groupBy { transaction ->
            when {
                transaction.date >= today -> "Today"
                transaction.date >= yesterday -> "Yesterday"
                else -> dateFormatter.format(java.util.Date(transaction.date))
            }
        }
    }

    private fun seedCategories() {
        val defaultCategories = listOf(
            CategoryEntity(name = "Food", icon = "fastfood", color = Color(0xFFFF5252).toArgb(), type = "EXPENSE"),
            CategoryEntity(name = "Transport", icon = "directions_car", color = Color(0xFF448AFF).toArgb(), type = "EXPENSE"),
            CategoryEntity(name = "Shopping", icon = "shopping_bag", color = Color(0xFFFFAB40).toArgb(), type = "EXPENSE"),
            CategoryEntity(name = "Salary", icon = "payments", color = Color(0xFF69F0AE).toArgb(), type = "INCOME")
        )
        viewModelScope.launch {
            defaultCategories.forEach { categoryRepository.insertCategory(it) }
        }
    }

    fun addTransaction(transaction: TransactionEntity) {
        // Validate input
        val noteValidation = ValidationUtils.validateTransactionNote(transaction.note)
        if (noteValidation is ValidationUtils.ValidationResult.Error) {
            _state.update { it.copy(error = noteValidation.message) }
            return
        }
        
        val amountValidation = ValidationUtils.validateAmount(transaction.amount)
        if (amountValidation is ValidationUtils.ValidationResult.Error) {
            _state.update { it.copy(error = amountValidation.message) }
            return
        }
        
        // Proceed with adding transaction
        viewModelScope.launch {
            try {
                var finalTransaction = transaction
                // Smart Categorization if category is not selected (assuming 0 or -1 is 'Uncategorized')
                if (transaction.categoryId == null || transaction.categoryId <= 0) {
                     val predictedCategoryName = smartCategorizer.predict(transaction.note)
                     if (predictedCategoryName != null) {
                         val category = categoryRepository.getCategoryByName(predictedCategoryName)
                         if (category != null) {
                             finalTransaction = transaction.copy(categoryId = category.id)
                         }
                     }
                }
                transactionRepository.insertTransaction(finalTransaction)
            } catch (e: IOException) {
                _state.update { it.copy(error = "Network error: ${e.message}") }
            } catch (e: SQLiteException) {
                _state.update { it.copy(error = "Database error: ${e.message}") }
            }
        }
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

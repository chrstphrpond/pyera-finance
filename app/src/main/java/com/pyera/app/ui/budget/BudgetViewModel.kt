package com.pyera.app.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.repository.AuthRepository
import com.pyera.app.data.repository.CategoryRepository
import com.pyera.app.data.local.entity.BudgetEntity
import com.pyera.app.data.local.entity.BudgetPeriod
import com.pyera.app.data.local.entity.BudgetStatus
import com.pyera.app.data.local.entity.BudgetSummary
import com.pyera.app.data.local.entity.BudgetWithSpending
import com.pyera.app.data.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.database.sqlite.SQLiteException
import java.io.IOException
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Current user ID from Firebase Auth
    private val currentUserId = MutableStateFlow(authRepository.currentUser?.uid ?: "")

    // Selected period filter
    private val _selectedPeriod = MutableStateFlow(BudgetPeriod.MONTHLY)
    val selectedPeriod: StateFlow<BudgetPeriod> = _selectedPeriod

    // Selected date for budget period calculation
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())

    // Filter status for budget list
    private val _statusFilter = MutableStateFlow<BudgetStatus?>(null)
    val statusFilter: StateFlow<BudgetStatus?> = _statusFilter

    // Calculate date range based on selected period and date
    private val dateRange = combine(_selectedPeriod, _selectedDate) { period, date ->
        budgetRepository.calculatePeriodDates(period, date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Pair(getMonthStart(), getMonthEnd())
    )

    // Main budgets flow with spending data
    val budgets: StateFlow<List<BudgetWithSpending>> = combine(
        currentUserId,
        dateRange,
        _statusFilter
    ) { userId, range, status ->
        Triple(userId, range, status)
    }.flatMapLatest { (userId, range, status) ->
        if (status != null) {
            budgetRepository.getBudgetsByStatus(userId, status, range.first, range.second)
        } else {
            budgetRepository.getBudgetsWithSpending(userId, range.first, range.second)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Budget summary
    val budgetSummary: StateFlow<BudgetSummary?> = combine(
        currentUserId,
        dateRange
    ) { userId, range ->
        Pair(userId, range)
    }.flatMapLatest { (userId, range) ->
        budgetRepository.getBudgetSummary(userId, range.first, range.second)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Selected budget for detail view
    private val _selectedBudget = MutableStateFlow<BudgetWithSpending?>(null)
    val selectedBudget: StateFlow<BudgetWithSpending?> = _selectedBudget

    // Categories for budget creation
    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI State for create/edit
    private val _createBudgetState = MutableStateFlow(CreateBudgetState())
    val createBudgetState: StateFlow<CreateBudgetState> = _createBudgetState

    // Combined state for BudgetScreen
    val state: StateFlow<BudgetState> = combine(
        budgets,
        _isLoading,
        _selectedPeriod
    ) { budgetsList, loading, period ->
        BudgetState(
            isLoading = loading,
            items = budgetsList.map { budgetWithSpending ->
                BudgetItem(
                    category = CategoryEntity(
                        id = budgetWithSpending.categoryId,
                        name = budgetWithSpending.categoryName,
                        color = budgetWithSpending.categoryColor,
                        icon = budgetWithSpending.categoryIcon ?: "",
                        type = "EXPENSE" // Budgets are for expense categories
                    ),
                    budgetAmount = budgetWithSpending.amount,
                    spentAmount = budgetWithSpending.spentAmount
                )
            },
            currentPeriod = when (period) {
                BudgetPeriod.DAILY -> "Daily"
                BudgetPeriod.WEEKLY -> "Weekly"
                BudgetPeriod.MONTHLY -> "Monthly"
                BudgetPeriod.YEARLY -> "Yearly"
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetState()
    )

    init {
        refreshBudgets()
    }

    // Alias for setBudgetForCategory to match BudgetScreen expectations
    fun setBudget(categoryId: Int, amount: Double) {
        setBudgetForCategory(categoryId, amount)
    }

    // ==================== Public Methods ====================

    fun setPeriod(period: BudgetPeriod) {
        _selectedPeriod.value = period
        updateDateRange()
    }

    fun setStatusFilter(status: BudgetStatus?) {
        _statusFilter.value = status
    }

    fun selectBudget(budget: BudgetWithSpending?) {
        _selectedBudget.value = budget
    }

    fun loadBudgetDetail(budgetId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val range = dateRange.value
                budgetRepository.getBudgetWithSpendingById(budgetId, range.first, range.second)
                    .collect { budget ->
                        _selectedBudget.value = budget
                    }
            } catch (e: IOException) {
                _error.value = "Network error: ${e.message}"
            } catch (e: SQLiteException) {
                _error.value = "Database error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createBudget(
        categoryId: Int,
        amount: Double,
        period: BudgetPeriod = BudgetPeriod.MONTHLY,
        alertThreshold: Float = 0.8f
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val budget = BudgetEntity(
                    userId = currentUserId.value,
                    categoryId = categoryId,
                    amount = amount,
                    period = period,
                    startDate = _selectedDate.value,
                    isActive = true,
                    alertThreshold = alertThreshold
                )
                budgetRepository.createBudget(budget)
                _createBudgetState.value = CreateBudgetState(success = true)
            } catch (e: IOException) {
                _error.value = "Network error: ${e.message}"
                _createBudgetState.value = CreateBudgetState(error = "Network error: ${e.message}")
            } catch (e: SQLiteException) {
                _error.value = "Database error: ${e.message}"
                _createBudgetState.value = CreateBudgetState(error = "Database error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                budgetRepository.updateBudget(budget)
            } catch (e: IOException) {
                _error.value = "Network error: ${e.message}"
            } catch (e: SQLiteException) {
                _error.value = "Database error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                budgetRepository.deleteBudget(budget)
            } catch (e: IOException) {
                _error.value = "Network error: ${e.message}"
            } catch (e: SQLiteException) {
                _error.value = "Database error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deactivateBudget(budgetId: Int) {
        viewModelScope.launch {
            budgetRepository.deactivateBudget(budgetId)
        }
    }

    fun setBudgetForCategory(categoryId: Int, amount: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                budgetRepository.setBudgetForCategory(
                    categoryId = categoryId,
                    amount = amount,
                    period = _selectedPeriod.value,
                    userId = currentUserId.value
                )
            } catch (e: IOException) {
                _error.value = "Network error: ${e.message}"
            } catch (e: SQLiteException) {
                _error.value = "Database error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshBudgets() {
        viewModelScope.launch {
            updateDateRange()
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun resetCreateState() {
        _createBudgetState.value = CreateBudgetState()
    }

    fun updateCreateState(
        categoryId: Int? = null,
        amount: String? = null,
        period: BudgetPeriod? = null,
        alertThreshold: Float? = null
    ) {
        _createBudgetState.value = _createBudgetState.value.copy(
            categoryId = categoryId ?: _createBudgetState.value.categoryId,
            amount = amount ?: _createBudgetState.value.amount,
            period = period ?: _createBudgetState.value.period,
            alertThreshold = alertThreshold ?: _createBudgetState.value.alertThreshold
        )
    }

    // ==================== Private Methods ====================

    private fun updateDateRange() {
        val calendar = Calendar.getInstance()
        _selectedDate.value = calendar.timeInMillis
    }

    private fun getMonthStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getMonthEnd(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}

/**
 * State for creating a new budget
 */
data class CreateBudgetState(
    val categoryId: Int? = null,
    val amount: String = "",
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val alertThreshold: Float = 0.8f,
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

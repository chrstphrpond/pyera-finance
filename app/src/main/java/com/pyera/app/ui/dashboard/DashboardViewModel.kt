package com.pyera.app.ui.dashboard

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.domain.repository.AuthRepository
import com.pyera.app.domain.repository.BudgetRepository
import com.pyera.app.domain.repository.CategoryRepository
import com.pyera.app.domain.repository.SavingsRepository
import com.pyera.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@Immutable
data class DashboardState(
    val userName: String = "User",
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val recentTransactions: List<TransactionUiModel> = emptyList(),
    val transactionCount: Int = 0,
    val activeBudgetsCount: Int = 0,
    val savingsGoalsCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@Immutable
data class TransactionUiModel(
    val id: Long,
    val title: String,
    val category: String,
    val amount: String,
    val isIncome: Boolean,
    val date: String
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val savingsRepository: SavingsRepository,
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private fun loadDashboardData() {
        val userId = authRepository.currentUser?.uid.orEmpty()
        val budgetCountFlow: Flow<Int> = if (userId.isBlank()) {
            flowOf(0)
        } else {
            budgetRepository.getActiveBudgetCount(userId)
        }

        val savingsGoalsCountFlow = savingsRepository.getAllSavingsGoals()
            .map { goals -> goals.size }

        combine(
            transactionRepository.getAllTransactions().distinctUntilChanged(),
            categoryRepository.getAllCategories().distinctUntilChanged(),
            budgetCountFlow.distinctUntilChanged(),
            savingsGoalsCountFlow.distinctUntilChanged()
        ) { transactions, categories, activeBudgetsCount, savingsGoalsCount ->
            DashboardInput(
                transactions = transactions,
                categories = categories,
                activeBudgetsCount = activeBudgetsCount,
                savingsGoalsCount = savingsGoalsCount
            )
        }
            .onStart { _state.value = _state.value.copy(isLoading = true) }
            .onEach { input ->
                val income = input.transactions
                    .filter { t -> t.type == "INCOME" }
                    .sumOf { t -> t.amount }
                
                val expense = input.transactions
                    .filter { t -> t.type == "EXPENSE" }
                    .sumOf { t -> t.amount }
                
                val balance = income - expense
                
                val categoryMap = input.categories.associateBy { it.id }
                val recentTransactions = input.transactions
                    .sortedByDescending { t -> t.date }
                    .take(5)
                    .map { t ->
                        val categoryName = t.categoryId?.let { id -> categoryMap[id]?.name }
                        t.toUiModel(categoryName)
                    }

                val userName = authRepository.currentUser?.displayName
                    ?.takeIf { it.isNotBlank() }
                    ?: authRepository.currentUser?.email
                    ?: "User"
                
                _state.value = DashboardState(
                    userName = userName,
                    totalBalance = balance,
                    totalIncome = income,
                    totalExpense = expense,
                    recentTransactions = recentTransactions,
                    transactionCount = input.transactions.size,
                    activeBudgetsCount = input.activeBudgetsCount,
                    savingsGoalsCount = input.savingsGoalsCount,
                    isLoading = false,
                    error = null
                )
            }
            .catch { e ->
                val errorMessage = when (e) {
                    is IOException -> "Network error: ${e.message}"
                    else -> e.message ?: "Failed to load dashboard data"
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        loadDashboardData()
    }

    // Cache date formatters - don't recreate on each mapping
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    private val todayFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    init {
        loadDashboardData()
    }

    private fun TransactionEntity.toUiModel(categoryName: String?): TransactionUiModel {
        val today = todayFormat.format(System.currentTimeMillis())
        val transactionDate = todayFormat.format(this.date)

        val dateString = when (transactionDate) {
            today -> "Today"
            else -> dateFormat.format(this.date)
        }

        return TransactionUiModel(
            id = this.id,
            title = this.note.takeIf { it.isNotBlank() } ?: "Transaction",
            category = categoryName ?: "Uncategorized",
            amount = String.format("%,.2f", this.amount),
            isIncome = this.type == "INCOME",
            date = dateString
        )
    }
}

private data class DashboardInput(
    val transactions: List<TransactionEntity>,
    val categories: List<CategoryEntity>,
    val activeBudgetsCount: Int,
    val savingsGoalsCount: Int
)

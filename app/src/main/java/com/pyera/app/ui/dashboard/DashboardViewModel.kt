package com.pyera.app.ui.dashboard

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
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
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val recentTransactions: List<TransactionUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@Immutable
data class TransactionUiModel(
    val id: Int,
    val title: String,
    val category: String,
    val amount: String,
    val isIncome: Boolean,
    val date: String
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private fun loadDashboardData() {
        transactionRepository.getAllTransactions()
            .distinctUntilChanged()
            .onStart { _state.value = _state.value.copy(isLoading = true) }
            .onEach { transactions ->
                val income = transactions
                    .filter { t -> t.type == "INCOME" }
                    .sumOf { t -> t.amount }
                
                val expense = transactions
                    .filter { t -> t.type == "EXPENSE" }
                    .sumOf { t -> t.amount }
                
                val balance = income - expense
                
                val recentTransactions = transactions
                    .sortedByDescending { t -> t.date }
                    .take(5)
                    .map { t -> t.toUiModel() }
                
                _state.value = DashboardState(
                    totalBalance = balance,
                    totalIncome = income,
                    totalExpense = expense,
                    recentTransactions = recentTransactions,
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

    private fun TransactionEntity.toUiModel(): TransactionUiModel {
        val today = todayFormat.format(System.currentTimeMillis())
        val transactionDate = todayFormat.format(this.date)
        
        val dateString = when (transactionDate) {
            today -> "Today"
            else -> dateFormat.format(this.date)
        }
        
        return TransactionUiModel(
            id = this.id,
            title = this.note.takeIf { it.isNotBlank() } ?: "Transaction",
            category = "General", // TODO: Get category name from categoryId
            amount = String.format("%,.2f", this.amount),
            isIncome = this.type == "INCOME",
            date = dateString
        )
    }
}

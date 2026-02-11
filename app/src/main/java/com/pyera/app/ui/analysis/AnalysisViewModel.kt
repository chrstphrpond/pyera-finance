package com.pyera.app.ui.analysis

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.domain.repository.AnalysisRepository
import com.pyera.app.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.pyera.app.domain.smart.PredictiveBudgetUseCase
import com.pyera.app.domain.repository.TransactionRepository

@Immutable
data class ExpenseByCategory(
    val categoryName: String,
    val amount: Double,
    val color: Int
)

@Immutable
data class AnalysisState(
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val expensesByCategory: List<ExpenseByCategory> = emptyList(),
    val predictedExpense: Double = 0.0,
    val isLoading: Boolean = false,
    val exportMessage: String? = null
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository,
    private val categoryRepository: CategoryRepository,
    private val predictiveBudgetUseCase: PredictiveBudgetUseCase,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnalysisState())
    val state = _state.asStateFlow()

    init {
        loadAnalysis()
        loadPrediction()
    }

    private fun loadPrediction() {
        viewModelScope.launch {
            val prediction = predictiveBudgetUseCase.predictNextMonthExpense()
            _state.value = _state.value.copy(predictedExpense = prediction)
        }
    }
    
    fun exportData() {
        viewModelScope.launch {
             val transactions = transactionRepository.getTransactionsForExport()
             // In a real app, this would write to a CSV file and share it via Intent.
             // For now, we'll just simulate success
             _state.value = _state.value.copy(exportMessage = "Exported ${transactions.size} records (Mock)")
        }
    }

    private fun loadAnalysis() {
        viewModelScope.launch {
            combine(
                analysisRepository.getAllTransactions(),
                categoryRepository.getAllCategories()
            ) { transactions, categories ->
                val categoryMap = categories.associateBy { it.id }
                
                val expenses = transactions.filter { it.type == "EXPENSE" }
                val income = transactions.filter { it.type == "INCOME" }

                val expenseGrouped = expenses.groupBy { it.categoryId }
                    .map { (categoryId, txs) ->
                        val category = categoryMap[categoryId]
                        val total = txs.sumOf { it.amount }
                        ExpenseByCategory(
                            categoryName = category?.name ?: "Unknown",
                            amount = total,
                            color = category?.color ?: -7829368 // Gray default
                        )
                    }
                    .sortedByDescending { it.amount }

                AnalysisState(
                    totalExpense = expenses.sumOf { it.amount },
                    totalIncome = income.sumOf { it.amount },
                    expensesByCategory = expenseGrouped,
                    isLoading = false
                )
            }.collect { newState ->
                _state.value = newState.copy(predictedExpense = _state.value.predictedExpense)
            }
        }
    }
}

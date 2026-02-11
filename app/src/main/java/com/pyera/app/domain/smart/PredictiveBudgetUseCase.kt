package com.pyera.app.domain.smart

import com.pyera.app.domain.repository.SpendingDataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PredictiveBudgetUseCase @Inject constructor(
    private val spendingDataRepository: SpendingDataRepository
) {
    suspend fun predictNextMonthExpense(): Double {
        val transactions = spendingDataRepository.getAllTransactions().first()
        if (transactions.isEmpty()) return 0.0

        // Simple algorithm: Average total expense per month
        // 1. Group by Month-Year
        // 2. Sum expenses for each month
        // 3. Average the sums

        val expenseTransactions = transactions.filter { it.type == "EXPENSE" }
        if (expenseTransactions.isEmpty()) return 0.0

        // Group by YYYY-MM
        val expensesByMonth = expenseTransactions.groupBy { 
             val date = java.util.Date(it.date)
             val sdf = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
             sdf.format(date)
        }

        val monthlyTotals = expensesByMonth.map { (_, txs) -> 
            txs.sumOf { it.amount } 
        }

        return if (monthlyTotals.isNotEmpty()) {
            monthlyTotals.average()
        } else {
            0.0
        }
    }
}

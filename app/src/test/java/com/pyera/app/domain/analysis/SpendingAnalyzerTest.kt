package com.pyera.app.domain.analysis

import app.cash.turbine.test
import com.pyera.app.domain.model.BudgetWithSpending
import com.pyera.app.domain.model.Transaction
import com.pyera.app.domain.repository.SpendingDataRepository
import com.pyera.app.test.budgetWithSpending
import com.pyera.app.test.transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpendingAnalyzerTest {

    private class FakeSpendingDataRepository(
        private val transactions: List<Transaction>
    ) : SpendingDataRepository {
        override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(transactions)
    }

    @Test
    fun `analyzeSpendingPatterns computes totals and trend`() = runTest {
        val now = System.currentTimeMillis()
        val start = now - (7 * 24 * 60 * 60 * 1000L)
        val end = now

        val transactions = listOf(
            transaction(id = 1, amount = 50.0, date = start + 1, type = "EXPENSE"),
            transaction(id = 2, amount = 150.0, date = start + 2, type = "EXPENSE"),
            transaction(id = 3, amount = 200.0, date = start + 3, type = "INCOME")
        )

        val analyzer = SpendingAnalyzer(FakeSpendingDataRepository(transactions))

        val insights = analyzer.analyzeSpendingPatterns(
            userId = "user",
            period = AnalysisPeriod.CUSTOM,
            customDateRange = DateRange(start, end)
        )

        assertEquals(200.0, insights.totalSpending, 0.001)
        assertEquals(2, insights.transactionCount)
        assertTrue(insights.averageDaily > 0)
    }

    @Test
    fun `detectAnomalies flags unusually large transaction`() = runTest {
        val now = System.currentTimeMillis()
        val small = listOf(10.0, 12.0, 9.0, 11.0, 10.5)
            .mapIndexed { index, amount ->
                transaction(id = index + 1L, amount = amount, date = now - index * 1000L)
            }
        val outlier = transaction(id = 99, amount = 1000.0, date = now - 10_000L)

        val analyzer = SpendingAnalyzer(FakeSpendingDataRepository(small + outlier))
        val anomalies = analyzer.detectAnomalies(userId = "user", lookbackDays = 30)

        assertTrue(anomalies.any { it.anomalyType == AnomalyType.UNUSUAL_AMOUNT })
    }

    @Test
    fun `calculateBudgetAdherence aggregates budgets`() {
        val budgets: List<BudgetWithSpending> = listOf(
            budgetWithSpending(amount = 1000.0, spentAmount = 200.0, remainingAmount = 800.0, progressPercentage = 0.2f),
            budgetWithSpending(id = 2, amount = 500.0, spentAmount = 600.0, remainingAmount = -100.0, progressPercentage = 1.2f)
        )

        val analyzer = SpendingAnalyzer(FakeSpendingDataRepository(emptyList()))
        val adherence = analyzer.calculateBudgetAdherence(budgets)

        assertEquals(2, adherence.totalBudgets)
        assertEquals(1500.0, adherence.totalBudgetAmount, 0.001)
        assertEquals(800.0, adherence.totalSpent, 0.001)
        assertTrue(adherence.overBudgetCategories.isNotEmpty())
    }

    @Test
    fun `spending data repository emits transactions`() = runTest {
        val tx = listOf(transaction(id = 1, amount = 25.0))
        val repo = FakeSpendingDataRepository(tx)

        repo.getAllTransactions().test {
            val item = awaitItem()
            assertEquals(1, item.size)
            awaitComplete()
        }
    }
}

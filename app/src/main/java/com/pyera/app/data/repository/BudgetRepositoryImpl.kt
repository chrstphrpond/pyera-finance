package com.pyera.app.data.repository

import com.pyera.app.data.local.dao.BudgetDao
import com.pyera.app.data.local.dao.TransactionDao
import com.pyera.app.data.local.entity.BudgetEntity
import com.pyera.app.data.local.entity.BudgetPeriod
import com.pyera.app.data.local.entity.BudgetStatus
import com.pyera.app.data.local.entity.BudgetSummary
import com.pyera.app.data.local.entity.BudgetWithSpending
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao
) : BudgetRepository {

    // ==================== CRUD Operations ====================

    override suspend fun createBudget(budget: BudgetEntity): Long {
        return budgetDao.insertBudget(budget.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun updateBudget(budget: BudgetEntity) {
        budgetDao.updateBudget(budget.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteBudget(budget: BudgetEntity) {
        budgetDao.deleteBudget(budget)
    }

    override suspend fun deleteBudgetById(budgetId: Int) {
        budgetDao.deleteBudgetById(budgetId)
    }

    // ==================== Query Operations ====================

    override fun getBudgetById(budgetId: Int): Flow<BudgetEntity?> {
        return budgetDao.getBudgetById(budgetId)
    }

    override fun getAllBudgetsForUser(userId: String): Flow<List<BudgetEntity>> {
        return budgetDao.getAllBudgetsForUser(userId)
    }

    override fun getActiveBudgetsForUser(userId: String): Flow<List<BudgetEntity>> {
        return budgetDao.getActiveBudgetsForUser(userId)
    }

    override fun getBudgetsByPeriod(period: BudgetPeriod, userId: String): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsByPeriod(period, userId)
    }

    override fun getBudgetsForCategory(categoryId: Int): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsForCategory(categoryId)
    }

    override fun getActiveBudgetForCategory(categoryId: Int): Flow<BudgetEntity?> {
        return budgetDao.getActiveBudgetForCategory(categoryId)
    }

    // ==================== Budget with Spending ====================

    override fun getBudgetsWithSpending(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<BudgetWithSpending>> {
        return budgetDao.getBudgetsWithSpending(userId, startDate, endDate)
            .map { budgets ->
                budgets.map { budget ->
                    budget.copy(daysRemaining = calculateDaysRemaining(budget.period, endDate))
                }
            }
    }

    override fun getBudgetWithSpendingById(
        budgetId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<BudgetWithSpending?> {
        return budgetDao.getBudgetWithSpendingById(budgetId, startDate, endDate)
            .map { budget ->
                budget?.copy(daysRemaining = calculateDaysRemaining(budget.period, endDate))
            }
    }

    override fun getBudgetsByStatus(
        userId: String,
        status: BudgetStatus,
        startDate: Long,
        endDate: Long
    ): Flow<List<BudgetWithSpending>> {
        return getBudgetsWithSpending(userId, startDate, endDate)
            .map { budgets ->
                budgets.filter { it.status == status }
            }
    }

    // ==================== Summary & Analytics ====================

    override fun getBudgetSummary(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<BudgetSummary> {
        return budgetDao.getBudgetSummary(userId, startDate, endDate)
    }

    override fun getActiveBudgetCount(userId: String): Flow<Int> {
        return budgetDao.getActiveBudgetCount(userId)
    }

    override fun getOverBudgetCount(userId: String, startDate: Long, endDate: Long): Flow<Int> {
        return budgetDao.getOverBudgetCount(userId, startDate, endDate)
    }

    // ==================== Utility Methods ====================

    override fun calculatePeriodDates(period: BudgetPeriod, startDate: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate

        val start = calendar.timeInMillis

        when (period) {
            BudgetPeriod.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            BudgetPeriod.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            BudgetPeriod.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            BudgetPeriod.YEARLY -> calendar.add(Calendar.YEAR, 1)
        }

        // Subtract 1 millisecond to get the end of the period
        val end = calendar.timeInMillis - 1
        return Pair(start, end)
    }

    override suspend fun deactivateBudget(budgetId: Int) {
        budgetDao.deactivateBudget(budgetId)
    }

    override suspend fun activateBudget(budgetId: Int) {
        budgetDao.activateBudget(budgetId)
    }

    override suspend fun setBudgetForCategory(
        categoryId: Int,
        amount: Double,
        period: BudgetPeriod,
        userId: String
    ) {
        withContext(Dispatchers.IO) {
            val existingBudget = budgetDao.getActiveBudgetForCategory(categoryId).first()

            if (existingBudget != null) {
                // Update existing budget
                budgetDao.updateBudget(
                    existingBudget.copy(
                        amount = amount,
                        period = period,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                // Create new budget
                budgetDao.insertBudget(
                    BudgetEntity(
                        userId = userId,
                        categoryId = categoryId,
                        amount = amount,
                        period = period,
                        startDate = System.currentTimeMillis(),
                        isActive = true
                    )
                )
            }
        }
    }

    override suspend fun hasActiveBudget(categoryId: Int): Boolean = withContext(Dispatchers.IO) {
        budgetDao.getActiveBudgetForCategory(categoryId).first() != null
    }

    override suspend fun getCategorySpendingProgress(
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): Float = withContext(Dispatchers.IO) {
        val budget = budgetDao.getActiveBudgetForCategory(categoryId).first()
            ?: return@withContext 0f

        val transactions = transactionDao.getTransactionsBetweenDates(startDate, endDate).first()
        val spent = transactions
            .filter { it.categoryId == categoryId && it.type == "EXPENSE" }
            .sumOf { it.amount }

        if (budget.amount > 0) {
            (spent / budget.amount).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    // ==================== Private Helpers ====================

    private fun calculateDaysRemaining(period: BudgetPeriod, periodEndDate: Long): Int {
        val now = System.currentTimeMillis()
        val diff = periodEndDate - now
        return if (diff > 0) {
            (diff / (1000 * 60 * 60 * 24)).toInt()
        } else {
            0
        }
    }
}

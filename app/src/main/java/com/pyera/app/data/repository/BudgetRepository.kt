package com.pyera.app.data.repository

import com.pyera.app.data.local.entity.BudgetEntity
import com.pyera.app.data.local.entity.BudgetPeriod
import com.pyera.app.data.local.entity.BudgetStatus
import com.pyera.app.data.local.entity.BudgetSummary
import com.pyera.app.data.local.entity.BudgetWithSpending
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for budget operations.
 * Provides methods to manage budgets, calculate spending, and track budget health.
 */
interface BudgetRepository {

    // ==================== CRUD Operations ====================

    suspend fun createBudget(budget: BudgetEntity): Long
    suspend fun updateBudget(budget: BudgetEntity)
    suspend fun deleteBudget(budget: BudgetEntity)
    suspend fun deleteBudgetById(budgetId: Int)

    // ==================== Query Operations ====================

    fun getBudgetById(budgetId: Int): Flow<BudgetEntity?>
    fun getAllBudgetsForUser(userId: String): Flow<List<BudgetEntity>>
    fun getActiveBudgetsForUser(userId: String): Flow<List<BudgetEntity>>
    fun getBudgetsByPeriod(period: BudgetPeriod, userId: String): Flow<List<BudgetEntity>>
    fun getBudgetsForCategory(categoryId: Int): Flow<List<BudgetEntity>>
    fun getActiveBudgetForCategory(categoryId: Int): Flow<BudgetEntity?>

    // ==================== Budget with Spending ====================

    /**
     * Get all active budgets with calculated spending data
     */
    fun getBudgetsWithSpending(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<BudgetWithSpending>>

    /**
     * Get a single budget with spending data
     */
    fun getBudgetWithSpendingById(
        budgetId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<BudgetWithSpending?>

    /**
     * Get budgets filtered by status
     */
    fun getBudgetsByStatus(
        userId: String,
        status: BudgetStatus,
        startDate: Long,
        endDate: Long
    ): Flow<List<BudgetWithSpending>>

    // ==================== Summary & Analytics ====================

    /**
     * Get overall budget summary
     */
    fun getBudgetSummary(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<BudgetSummary>

    /**
     * Get count of active budgets
     */
    fun getActiveBudgetCount(userId: String): Flow<Int>

    /**
     * Get count of over-budget categories
     */
    fun getOverBudgetCount(userId: String, startDate: Long, endDate: Long): Flow<Int>

    // ==================== Utility Methods ====================

    /**
     * Calculate date range for a budget period
     */
    fun calculatePeriodDates(period: BudgetPeriod, startDate: Long): Pair<Long, Long>

    /**
     * Deactivate a budget (soft delete)
     */
    suspend fun deactivateBudget(budgetId: Int)

    /**
     * Activate a budget
     */
    suspend fun activateBudget(budgetId: Int)

    /**
     * Set or update budget for a category
     */
    suspend fun setBudgetForCategory(
        categoryId: Int,
        amount: Double,
        period: BudgetPeriod,
        userId: String
    )

    /**
     * Check if a category has an active budget
     */
    suspend fun hasActiveBudget(categoryId: Int): Boolean

    /**
     * Get spending progress for a category
     */
    suspend fun getCategorySpendingProgress(
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): Float
}

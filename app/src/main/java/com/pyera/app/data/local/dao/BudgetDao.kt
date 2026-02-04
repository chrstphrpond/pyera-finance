package com.pyera.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pyera.app.data.local.entity.BudgetEntity
import com.pyera.app.data.local.entity.BudgetPeriod
import com.pyera.app.data.local.entity.BudgetStatus
import com.pyera.app.data.local.entity.BudgetSummary
import com.pyera.app.data.local.entity.BudgetWithSpending
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    // ==================== CRUD Operations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :budgetId")
    suspend fun deleteBudgetById(budgetId: Int)

    // ==================== Single Budget Queries ====================

    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    fun getBudgetById(budgetId: Int): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    suspend fun getBudgetByIdSync(budgetId: Int): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND isActive = 1 LIMIT 1")
    fun getActiveBudgetForCategory(categoryId: Int): Flow<BudgetEntity?>

    // ==================== List Queries ====================

    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllBudgetsForUser(userId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND isActive = 1 ORDER BY amount DESC")
    fun getActiveBudgetsForUser(userId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE period = :period AND userId = :userId")
    fun getBudgetsByPeriod(period: BudgetPeriod, userId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId")
    fun getBudgetsForCategory(categoryId: Int): Flow<List<BudgetEntity>>

    // ==================== Budget with Spending Queries ====================

    /**
     * Get all budgets with spending data for a user within a date range
     */
    @Transaction
    @Query("""
        SELECT 
            b.id,
            b.userId,
            b.categoryId,
            c.name as categoryName,
            c.color as categoryColor,
            c.icon as categoryIcon,
            b.amount,
            b.period,
            b.startDate,
            b.isActive,
            b.alertThreshold,
            COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as spentAmount,
            b.amount - COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as remainingAmount,
            CASE 
                WHEN b.amount > 0 THEN 
                    (COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) / b.amount * 100)
                ELSE 0 
            END as progressPercentage,
            CASE 
                WHEN COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) > b.amount THEN 1 
                ELSE 0 
            END as isOverBudget,
            0 as daysRemaining
        FROM budgets b
        LEFT JOIN categories c ON b.categoryId = c.id
        LEFT JOIN transactions t ON b.categoryId = t.categoryId 
            AND t.date >= :startDate 
            AND t.date <= :endDate
        WHERE b.userId = :userId AND b.isActive = 1
        GROUP BY b.id, b.userId, b.categoryId, c.name, c.color, c.icon, b.amount, b.period, b.startDate, b.isActive, b.alertThreshold
        ORDER BY progressPercentage DESC
    """)
    fun getBudgetsWithSpending(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<BudgetWithSpending>>

    /**
     * Get budget with spending for a specific category
     */
    @Transaction
    @Query("""
        SELECT 
            b.id,
            b.userId,
            b.categoryId,
            c.name as categoryName,
            c.color as categoryColor,
            c.icon as categoryIcon,
            b.amount,
            b.period,
            b.startDate,
            b.isActive,
            b.alertThreshold,
            COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as spentAmount,
            b.amount - COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as remainingAmount,
            CASE 
                WHEN b.amount > 0 THEN 
                    (COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) / b.amount * 100)
                ELSE 0 
            END as progressPercentage,
            CASE 
                WHEN COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) > b.amount THEN 1 
                ELSE 0 
            END as isOverBudget,
            0 as daysRemaining
        FROM budgets b
        LEFT JOIN categories c ON b.categoryId = c.id
        LEFT JOIN transactions t ON b.categoryId = t.categoryId 
            AND t.date >= :startDate 
            AND t.date <= :endDate
        WHERE b.id = :budgetId
        GROUP BY b.id, b.userId, b.categoryId, c.name, c.color, c.icon, b.amount, b.period, b.startDate, b.isActive, b.alertThreshold
    """)
    fun getBudgetWithSpendingById(
        budgetId: Int,
        startDate: Long,
        endDate: Long
    ): Flow<BudgetWithSpending?>

    // ==================== Summary Queries ====================

    /**
     * Get overall budget summary for a user
     */
    @Query("""
        SELECT 
            COUNT(*) as totalBudgets,
            COALESCE(SUM(b.amount), 0) as totalBudgetAmount,
            COALESCE(SUM(spent.spentAmount), 0) as totalSpent,
            COALESCE(SUM(b.amount), 0) - COALESCE(SUM(spent.spentAmount), 0) as totalRemaining,
            CASE 
                WHEN COALESCE(SUM(b.amount), 0) > 0 THEN 
                    (COALESCE(SUM(spent.spentAmount), 0) / COALESCE(SUM(b.amount), 0) * 100)
                ELSE 0 
            END as overallProgress,
            SUM(CASE WHEN spent.spentAmount > b.amount THEN 1 ELSE 0 END) as overBudgetCount,
            SUM(CASE WHEN spent.spentAmount >= b.amount * b.alertThreshold AND spent.spentAmount <= b.amount THEN 1 ELSE 0 END) as warningCount,
            SUM(CASE WHEN spent.spentAmount < b.amount * b.alertThreshold THEN 1 ELSE 0 END) as healthyCount
        FROM budgets b
        LEFT JOIN (
            SELECT 
                categoryId,
                SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as spentAmount
            FROM transactions
            WHERE date >= :startDate AND date <= :endDate
            GROUP BY categoryId
        ) spent ON b.categoryId = spent.categoryId
        WHERE b.userId = :userId AND b.isActive = 1
    """)
    fun getBudgetSummary(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<BudgetSummary>

    // ==================== Statistics Queries ====================

    @Query("SELECT COUNT(*) FROM budgets WHERE userId = :userId AND isActive = 1")
    fun getActiveBudgetCount(userId: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM budgets WHERE userId = :userId AND isActive = 1 AND id IN (
            SELECT b.id FROM budgets b
            LEFT JOIN transactions t ON b.categoryId = t.categoryId 
                AND t.date >= :startDate AND t.date <= :endDate AND t.type = 'EXPENSE'
            GROUP BY b.id
            HAVING COALESCE(SUM(t.amount), 0) > b.amount
        )
    """)
    fun getOverBudgetCount(userId: String, startDate: Long, endDate: Long): Flow<Int>

    // ==================== Bulk Operations ====================

    @Query("UPDATE budgets SET isActive = 0 WHERE id = :budgetId")
    suspend fun deactivateBudget(budgetId: Int)

    @Query("UPDATE budgets SET isActive = 1 WHERE id = :budgetId")
    suspend fun activateBudget(budgetId: Int)

    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun deleteAllBudgetsForUser(userId: String)

    @Query("UPDATE budgets SET updatedAt = :timestamp WHERE id = :budgetId")
    suspend fun updateTimestamp(budgetId: Int, timestamp: Long = System.currentTimeMillis())
}

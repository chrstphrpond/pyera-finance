package com.pyera.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pyera.app.data.local.dao.BillDao
import com.pyera.app.data.local.dao.BudgetDao
import com.pyera.app.data.local.dao.CategoryDao
import com.pyera.app.data.local.dao.DebtDao
import com.pyera.app.data.local.dao.InvestmentDao
import com.pyera.app.data.local.dao.SavingsGoalDao
import com.pyera.app.data.local.dao.TransactionDao
import com.pyera.app.data.local.entity.BillEntity
import com.pyera.app.data.local.entity.BudgetEntity
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.DebtEntity
import com.pyera.app.data.local.entity.InvestmentEntity
import com.pyera.app.data.local.entity.SavingsGoalEntity
import com.pyera.app.data.local.entity.TransactionEntity

/**
 * Main Room database for the Pyera Finance app.
 * 
 * Version History:
 * - v1: Initial database with basic entities
 * - v2: Added BudgetEntity and updated relationships
 * - v3: Added BillEntity and InvestmentEntity
 * - v4: Added database indexes for performance optimization
 *   - TransactionEntity: idx_transactions_date, idx_transactions_type, idx_transactions_category
 *   - BudgetEntity: idx_budgets_category, idx_budgets_user, idx_budgets_active, idx_budgets_dates
 *   - DebtEntity: idx_debts_due_date, idx_debts_status, idx_debts_type
 *   - SavingsGoalEntity: idx_savings_deadline, idx_savings_target
 *   - CategoryEntity: idx_categories_type, idx_categories_name
 */
@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        DebtEntity::class,
        SavingsGoalEntity::class,
        BillEntity::class,
        InvestmentEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class PyeraDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun debtDao(): DebtDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun billDao(): BillDao
    abstract fun investmentDao(): InvestmentDao
}

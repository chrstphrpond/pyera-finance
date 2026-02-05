package com.pyera.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pyera.app.data.local.dao.AccountDao
import com.pyera.app.data.local.dao.BillDao
import com.pyera.app.data.local.dao.BudgetDao
import com.pyera.app.data.local.dao.CategoryDao
import com.pyera.app.data.local.dao.DebtDao
import com.pyera.app.data.local.dao.InvestmentDao
import com.pyera.app.data.local.dao.NetWorthDao
import com.pyera.app.data.local.dao.RecurringTransactionDao
import com.pyera.app.data.local.dao.SavingsGoalDao
import com.pyera.app.data.local.dao.TransactionDao
import com.pyera.app.data.local.dao.TransactionRuleDao
import com.pyera.app.data.local.dao.TransactionTemplateDao
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.BillEntity
import com.pyera.app.data.local.entity.BudgetEntity
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.DebtEntity
import com.pyera.app.data.local.entity.InvestmentEntity
import com.pyera.app.data.local.entity.RecurringTransactionEntity
import com.pyera.app.data.local.entity.SavingsGoalEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.data.local.entity.TransactionRuleEntity
import com.pyera.app.data.local.entity.TransactionTemplateEntity

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
 * - v5: Added AccountEntity and updated TransactionEntity with account support
 *   - Added AccountEntity for multi-account management
 *   - Updated TransactionEntity with accountId, userId, isTransfer, transferAccountId
 *   - Added account-related indexes
 * - v6: Added RecurringTransactionEntity for recurring transactions feature
 * - v7: Added receipt attachment support to TransactionEntity
 *   - Added receiptImagePath, receiptCloudUrl, hasReceipt fields
 * - v8: Added TransactionRuleEntity for auto-categorization rules
 *   - User-defined rules to automatically categorize transactions based on patterns
 * - v9: Added TransactionTemplateEntity for quick transaction templates
 *   - Save frequent transactions as templates for one-tap entry
 */
@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        DebtEntity::class,
        SavingsGoalEntity::class,
        BillEntity::class,
        InvestmentEntity::class,
        AccountEntity::class,
        RecurringTransactionEntity::class,
        TransactionRuleEntity::class,
        TransactionTemplateEntity::class
    ],
    version = 9,
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
    abstract fun accountDao(): AccountDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun netWorthDao(): NetWorthDao
    abstract fun transactionRuleDao(): TransactionRuleDao
    abstract fun transactionTemplateDao(): TransactionTemplateDao
}

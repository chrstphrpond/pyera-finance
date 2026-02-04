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
    version = 3,
    exportSchema = true
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

package com.pyera.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pyera.app.data.local.PyeraDatabase
import com.pyera.app.data.local.dao.BillDao
import com.pyera.app.data.local.dao.BudgetDao
import com.pyera.app.data.local.dao.CategoryDao
import com.pyera.app.data.local.dao.DebtDao
import com.pyera.app.data.local.dao.InvestmentDao
import com.pyera.app.data.local.dao.SavingsGoalDao
import com.pyera.app.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePyeraDatabase(
        @ApplicationContext context: Context
    ): PyeraDatabase {
        return Room.databaseBuilder(
            context,
            PyeraDatabase::class.java,
            "pyera_database"
        )
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .build()
    }

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add migrations as needed for schema changes
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add migrations as needed for schema changes
        }
    }

    @Provides
    fun provideCategoryDao(database: PyeraDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideTransactionDao(database: PyeraDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideBudgetDao(database: PyeraDatabase): BudgetDao {
        return database.budgetDao()
    }

    @Provides
    fun provideDebtDao(database: PyeraDatabase): DebtDao {
        return database.debtDao()
    }

    @Provides
    fun provideSavingsGoalDao(database: PyeraDatabase): SavingsGoalDao {
        return database.savingsGoalDao()
    }

    @Provides
    fun provideBillDao(database: PyeraDatabase): BillDao {
        return database.billDao()
    }

    @Provides
    fun provideInvestmentDao(database: PyeraDatabase): InvestmentDao {
        return database.investmentDao()
    }
}

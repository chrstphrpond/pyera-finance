package com.pyera.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pyera.app.data.local.PyeraDatabase
import com.pyera.app.data.security.SecurePassphraseManager
import net.sqlcipher.database.SupportFactory
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

/**
 * Database module providing Room database and DAO instances.
 * Includes database migrations and SQLCipher encryption.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePyeraDatabase(
        @ApplicationContext context: Context
    ): PyeraDatabase {
        val passphraseManager = SecurePassphraseManager(context)
        val passphrase = passphraseManager.getOrCreatePassphrase()
        
        val factory = SupportFactory(passphrase)
        
        return Room.databaseBuilder(
            context,
            PyeraDatabase::class.java,
            DATABASE_NAME
        )
        .openHelperFactory(factory)
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        .build()
    }

    /**
     * Migration from v1 to v2 - Initial schema updates
     */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add migrations as needed for schema changes
        }
    }

    /**
     * Migration from v2 to v3 - Added Bill and Investment entities
     */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add migrations as needed for schema changes
        }
    }
    
    /**
     * Migration from v3 to v4 - Added performance indexes
     * Creates indexes for faster queries on frequently accessed columns
     */
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add endDate column to budgets table (new field for budget periods)
            database.execSQL("ALTER TABLE budgets ADD COLUMN endDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L}")
            
            // Transaction indexes
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(date)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_category ON transactions(categoryId)")
            
            // Budget indexes (recreate with named indexes)
            database.execSQL("DROP INDEX IF EXISTS index_budgets_categoryId")
            database.execSQL("DROP INDEX IF EXISTS index_budgets_userId")
            database.execSQL("DROP INDEX IF EXISTS index_budgets_isActive")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_budgets_category ON budgets(categoryId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_budgets_user ON budgets(userId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_budgets_active ON budgets(isActive)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_budgets_dates ON budgets(startDate, endDate)")
            
            // Debt indexes
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_debts_due_date ON debts(dueDate)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_debts_status ON debts(isPaid)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_debts_type ON debts(type)")
            
            // Savings goal indexes
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_savings_deadline ON savings_goals(deadline)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_savings_target ON savings_goals(targetAmount)")
            
            // Category indexes
            database.execSQL("DROP INDEX IF EXISTS index_categories_type")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_categories_type ON categories(type)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_categories_name ON categories(name)")
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
    
    private const val DATABASE_NAME = "pyera_database"
}

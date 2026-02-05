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
import com.pyera.app.data.local.dao.RecurringTransactionDao
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
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
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

    /**
     * Migration from v4 to v5 - Added AccountEntity and updated TransactionEntity
     * Creates accounts table and updates transactions with account support
     */
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create accounts table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS accounts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    userId TEXT NOT NULL,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    balance REAL NOT NULL DEFAULT 0.0,
                    currency TEXT NOT NULL DEFAULT 'PHP',
                    color INTEGER NOT NULL,
                    icon TEXT NOT NULL,
                    isDefault INTEGER NOT NULL DEFAULT 0,
                    isArchived INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """.trimIndent())
            
            // Create account indexes
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_accounts_user ON accounts(userId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_accounts_default ON accounts(isDefault)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_accounts_archived ON accounts(isArchived)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_accounts_type ON accounts(type)")
            
            // Backup existing transactions
            database.execSQL("ALTER TABLE transactions RENAME TO transactions_backup")
            
            // Create new transactions table with updated schema
            database.execSQL("""
                CREATE TABLE transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    amount REAL NOT NULL,
                    note TEXT NOT NULL,
                    date INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    categoryId INTEGER,
                    accountId INTEGER NOT NULL DEFAULT 0,
                    userId TEXT NOT NULL DEFAULT '',
                    isTransfer INTEGER NOT NULL DEFAULT 0,
                    transferAccountId INTEGER,
                    createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                    updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                    FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE SET NULL,
                    FOREIGN KEY(accountId) REFERENCES accounts(id) ON DELETE RESTRICT
                )
            """.trimIndent())
            
            // Migrate data from backup
            database.execSQL("""
                INSERT INTO transactions (id, amount, note, date, type, categoryId)
                SELECT id, amount, note, date, type, categoryId FROM transactions_backup
            """.trimIndent())
            
            // Drop backup table
            database.execSQL("DROP TABLE transactions_backup")
            
            // Create transaction indexes
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(date)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_category ON transactions(categoryId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_account ON transactions(accountId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_user ON transactions(userId)")
        }
    }

    /**
     * Migration from v5 to v6 - Added RecurringTransactionEntity
     * Creates recurring_transactions table with indexes
     */
    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create recurring_transactions table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS recurring_transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    amount REAL NOT NULL,
                    type TEXT NOT NULL,
                    categoryId INTEGER,
                    description TEXT NOT NULL,
                    frequency TEXT NOT NULL,
                    startDate INTEGER NOT NULL,
                    endDate INTEGER,
                    nextDueDate INTEGER NOT NULL,
                    isActive INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                    FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE SET NULL
                )
            """.trimIndent())
            
            // Create recurring transaction indexes
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_recurring_category ON recurring_transactions(categoryId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_recurring_next_due ON recurring_transactions(nextDueDate)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_recurring_active ON recurring_transactions(isActive)")
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

    @Provides
    fun provideAccountDao(database: PyeraDatabase): AccountDao {
        return database.accountDao()
    }

    @Provides
    fun provideRecurringTransactionDao(database: PyeraDatabase): RecurringTransactionDao {
        return database.recurringTransactionDao()
    }
    
    private const val DATABASE_NAME = "pyera_database"
}

package com.pyera.app.di

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pyera.app.data.local.PyeraDatabase
import com.pyera.app.data.security.SecurePassphraseManager
import net.sqlcipher.database.SupportFactory
import net.sqlcipher.database.SQLiteDatabase
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
import com.pyera.app.data.local.dao.TransactionTemplateDao
import com.pyera.app.data.local.dao.TransactionRuleDao
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

        val encryptedBuilder = Room.databaseBuilder(
            context,
            PyeraDatabase::class.java,
            DATABASE_NAME_ENCRYPTED
        )
        .openHelperFactory(factory)
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11
        )
        
        val legacyBuilder = Room.databaseBuilder(
            context,
            PyeraDatabase::class.java,
            DATABASE_NAME_LEGACY
        )
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11
        )

        val legacyDbExists = context.getDatabasePath(DATABASE_NAME_LEGACY).exists()
        val encryptedDbExists = context.getDatabasePath(DATABASE_NAME_ENCRYPTED).exists()

        if (legacyDbExists && !encryptedDbExists) {
            val migrated = migrateLegacyDatabaseIfNeeded(context, passphrase)
            if (!migrated) {
                Log.w(TAG, "Encrypted migration failed; falling back to legacy database.")
                return legacyBuilder.build()
            }
        }

        return try {
            encryptedBuilder.build()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open encrypted database, falling back to legacy.", e)
            legacyBuilder.build()
        }
    }

    /**
     * Migration from v1 to v2 - Initial schema updates
     */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add migrations as needed for schema changes
        }
    }

    /**
     * Migration from v2 to v3 - Added Bill and Investment entities
     */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add migrations as needed for schema changes
        }
    }
    
    /**
     * Migration from v3 to v4 - Added performance indexes
     * Creates indexes for faster queries on frequently accessed columns
     */
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add endDate column to budgets table (new field for budget periods)
            db.execSQL("ALTER TABLE budgets ADD COLUMN endDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L}")
            
            // Transaction indexes
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(date)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_category ON transactions(categoryId)")
            
            // Budget indexes (recreate with named indexes)
            db.execSQL("DROP INDEX IF EXISTS index_budgets_categoryId")
            db.execSQL("DROP INDEX IF EXISTS index_budgets_userId")
            db.execSQL("DROP INDEX IF EXISTS index_budgets_isActive")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_budgets_category ON budgets(categoryId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_budgets_user ON budgets(userId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_budgets_active ON budgets(isActive)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_budgets_dates ON budgets(startDate, endDate)")
            
            // Debt indexes
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_debts_due_date ON debts(dueDate)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_debts_status ON debts(isPaid)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_debts_type ON debts(type)")
            
            // Savings goal indexes
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_savings_deadline ON savings_goals(deadline)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_savings_target ON savings_goals(targetAmount)")
            
            // Category indexes
            db.execSQL("DROP INDEX IF EXISTS index_categories_type")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_categories_type ON categories(type)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_categories_name ON categories(name)")
        }
    }

    /**
     * Migration from v4 to v5 - Added AccountEntity and updated TransactionEntity
     * Creates accounts table and updates transactions with account support
     */
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create accounts table
            db.execSQL("""
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
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_accounts_user ON accounts(userId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_accounts_default ON accounts(isDefault)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_accounts_archived ON accounts(isArchived)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_accounts_type ON accounts(type)")
            
            // Backup existing transactions
            db.execSQL("ALTER TABLE transactions RENAME TO transactions_backup")
            
            // Create new transactions table with updated schema
            db.execSQL("""
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
            db.execSQL("""
                INSERT INTO transactions (id, amount, note, date, type, categoryId)
                SELECT id, amount, note, date, type, categoryId FROM transactions_backup
            """.trimIndent())
            
            // Drop backup table
            db.execSQL("DROP TABLE transactions_backup")
            
            // Create transaction indexes
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(date)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_category ON transactions(categoryId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_account ON transactions(accountId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_user ON transactions(userId)")
        }
    }

    /**
     * Migration from v5 to v6 - Added RecurringTransactionEntity
     * Creates recurring_transactions table with indexes
     */
    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create recurring_transactions table
            db.execSQL("""
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
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_recurring_category ON recurring_transactions(categoryId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_recurring_next_due ON recurring_transactions(nextDueDate)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_recurring_active ON recurring_transactions(isActive)")
        }
    }

    /**
     * Migration from v6 to v7 - Added NetWorthSnapshot entity
     * Creates net_worth_snapshots table with indexes for tracking net worth history
     */
    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create net_worth_snapshots table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS net_worth_snapshots (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    userId TEXT NOT NULL,
                    date INTEGER NOT NULL,
                    totalAssets REAL NOT NULL,
                    totalLiabilities REAL NOT NULL,
                    netWorth REAL NOT NULL,
                    accountsBreakdown TEXT NOT NULL,
                    createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                )
            """.trimIndent())
            
            // Create indexes for efficient queries
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_networth_user ON net_worth_snapshots(userId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_networth_date ON net_worth_snapshots(date)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_networth_user_date ON net_worth_snapshots(userId, date)")
        }
    }

    /**
     * Migration from v7 to v8 - Added TransactionRuleEntity
     * Creates transaction_rules table with indexes for auto-categorization
     */
    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS transaction_rules (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    userId TEXT NOT NULL,
                    pattern TEXT NOT NULL,
                    matchType TEXT NOT NULL,
                    categoryId INTEGER NOT NULL,
                    priority INTEGER NOT NULL DEFAULT 0,
                    isActive INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_rules_user ON transaction_rules(userId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_rules_active ON transaction_rules(isActive)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_rules_category ON transaction_rules(categoryId)")
        }
    }

    /**
     * Migration from v8 to v9 - Added TransactionTemplateEntity
     * Creates transaction_templates table for quick transaction templates
     */
    private val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS transaction_templates (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    userId TEXT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT NOT NULL,
                    amount REAL,
                    type TEXT NOT NULL,
                    categoryId INTEGER,
                    accountId INTEGER,
                    icon TEXT,
                    color INTEGER,
                    displayOrder INTEGER NOT NULL DEFAULT 0,
                    isActive INTEGER NOT NULL DEFAULT 1,
                    useCount INTEGER NOT NULL DEFAULT 0,
                    lastUsedAt INTEGER,
                    createdAt INTEGER NOT NULL,
                    FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE SET NULL,
                    FOREIGN KEY(accountId) REFERENCES accounts(id) ON DELETE SET NULL
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_templates_user ON transaction_templates(userId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_templates_active ON transaction_templates(isActive)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_templates_order ON transaction_templates(displayOrder)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_templates_category ON transaction_templates(categoryId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_templates_account ON transaction_templates(accountId)")
        }
    }

    /**
     * Migration from v9 to v10 - Ensure NetWorthSnapshot table exists
     * Creates net_worth_snapshots table and indexes if missing (fresh v9 installs).
     */
    private val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS net_worth_snapshots (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    userId TEXT NOT NULL,
                    date INTEGER NOT NULL,
                    totalAssets REAL NOT NULL,
                    totalLiabilities REAL NOT NULL,
                    netWorth REAL NOT NULL,
                    accountsBreakdown TEXT NOT NULL,
                    createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                )
            """.trimIndent())

            db.execSQL("CREATE INDEX IF NOT EXISTS idx_networth_user ON net_worth_snapshots(userId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_networth_date ON net_worth_snapshots(date)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_networth_user_date ON net_worth_snapshots(userId, date)")
        }
    }

    /**
     * Migration from v10 to v11 - Add accountId to recurring transactions.
     * Recreates recurring_transactions with account support and indexes.
     */
    private val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS recurring_transactions_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    amount REAL NOT NULL,
                    type TEXT NOT NULL,
                    categoryId INTEGER,
                    accountId INTEGER,
                    description TEXT NOT NULL,
                    frequency TEXT NOT NULL,
                    startDate INTEGER NOT NULL,
                    endDate INTEGER,
                    nextDueDate INTEGER NOT NULL,
                    isActive INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                    FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE SET NULL,
                    FOREIGN KEY(accountId) REFERENCES accounts(id) ON DELETE SET NULL
                )
            """.trimIndent())

            db.execSQL("""
                INSERT INTO recurring_transactions_new (
                    id, amount, type, categoryId, accountId, description, frequency,
                    startDate, endDate, nextDueDate, isActive, createdAt
                )
                SELECT
                    id, amount, type, categoryId,
                    (SELECT id FROM accounts WHERE isDefault = 1 LIMIT 1),
                    description, frequency, startDate, endDate, nextDueDate, isActive, createdAt
                FROM recurring_transactions
            """.trimIndent())

            db.execSQL("DROP TABLE recurring_transactions")
            db.execSQL("ALTER TABLE recurring_transactions_new RENAME TO recurring_transactions")

            db.execSQL("CREATE INDEX IF NOT EXISTS idx_recurring_category ON recurring_transactions(categoryId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_recurring_account ON recurring_transactions(accountId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_recurring_next_due ON recurring_transactions(nextDueDate)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_recurring_active ON recurring_transactions(isActive)")
        }
    }

    @Provides
    @Singleton
    fun provideCategoryDao(db: PyeraDatabase): CategoryDao {
        return db.categoryDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(db: PyeraDatabase): TransactionDao {
        return db.transactionDao()
    }

    @Provides
    @Singleton
    fun provideBudgetDao(db: PyeraDatabase): BudgetDao {
        return db.budgetDao()
    }

    @Provides
    @Singleton
    fun provideDebtDao(db: PyeraDatabase): DebtDao {
        return db.debtDao()
    }

    @Provides
    @Singleton
    fun provideSavingsGoalDao(db: PyeraDatabase): SavingsGoalDao {
        return db.savingsGoalDao()
    }

    @Provides
    @Singleton
    fun provideBillDao(db: PyeraDatabase): BillDao {
        return db.billDao()
    }

    @Provides
    @Singleton
    fun provideInvestmentDao(db: PyeraDatabase): InvestmentDao {
        return db.investmentDao()
    }

    @Provides
    @Singleton
    fun provideAccountDao(db: PyeraDatabase): AccountDao {
        return db.accountDao()
    }

    @Provides
    @Singleton
    fun provideRecurringTransactionDao(db: PyeraDatabase): RecurringTransactionDao {
        return db.recurringTransactionDao()
    }

    @Provides
    @Singleton
    fun provideNetWorthDao(db: PyeraDatabase): NetWorthDao {
        return db.netWorthDao()
    }

    @Provides
    @Singleton
    fun provideTransactionRuleDao(db: PyeraDatabase): TransactionRuleDao {
        return db.transactionRuleDao()
    }

    @Provides
    @Singleton
    fun provideTransactionTemplateDao(db: PyeraDatabase): TransactionTemplateDao {
        return db.transactionTemplateDao()
    }
    
    private const val DATABASE_NAME_LEGACY = "pyera_database"
    private const val DATABASE_NAME_ENCRYPTED = "pyera_database_encrypted.db"
    private const val TAG = "DatabaseModule"

    /**
     * Best-effort migration from legacy unencrypted DB to encrypted SQLCipher DB.
     * Returns true if migration succeeded or was not needed.
     */
    private fun migrateLegacyDatabaseIfNeeded(
        context: Context,
        passphrase: ByteArray
    ): Boolean {
        val legacyFile = context.getDatabasePath(DATABASE_NAME_LEGACY)
        val encryptedFile = context.getDatabasePath(DATABASE_NAME_ENCRYPTED)

        if (!legacyFile.exists() || encryptedFile.exists()) return true

        return try {
            SQLiteDatabase.loadLibs(context)
            val passphraseString = Base64.encodeToString(passphrase, Base64.NO_WRAP)
            val legacyPath = legacyFile.absolutePath.replace("'", "''")
            val encryptedPath = encryptedFile.absolutePath.replace("'", "''")

            val legacyDb = SQLiteDatabase.openDatabase(
                legacyPath,
                "",
                null,
                SQLiteDatabase.OPEN_READWRITE
            )
            legacyDb.rawExecSQL(
                "ATTACH DATABASE '$encryptedPath' AS encrypted KEY '$passphraseString';"
            )
            legacyDb.rawExecSQL("SELECT sqlcipher_export('encrypted');")
            legacyDb.rawExecSQL("DETACH DATABASE encrypted;")
            legacyDb.close()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to migrate legacy database to encrypted database.", e)
            false
        }
    }
}

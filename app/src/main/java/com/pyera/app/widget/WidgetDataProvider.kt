package com.pyera.app.widget

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pyera.app.MainActivity
import com.pyera.app.data.local.PyeraDatabase
import com.pyera.app.data.security.SecurePassphraseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import net.sqlcipher.database.SupportFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Data class for widget transaction display
 */
data class WidgetTransaction(
    val id: Int,
    val amount: Double,
    val note: String,
    val categoryName: String,
    val categoryIcon: String?,
    val type: String,
    val date: String
)

/**
 * Data class for balance information
 */
data class BalanceData(
    val totalBalance: Double,
    val monthlyIncome: Double,
    val monthlyExpense: Double
)

/**
 * Provider for widget data
 */
class WidgetDataProvider private constructor(
    private val database: PyeraDatabase
) {
    companion object {
        private var instance: WidgetDataProvider? = null
        private const val DATABASE_NAME = "pyera_database"
        
        fun getInstance(context: Context): WidgetDataProvider {
            return instance ?: synchronized(this) {
                instance ?: createInstance(context).also { instance = it }
            }
        }
        
        private fun createInstance(context: Context): WidgetDataProvider {
            val passphraseManager = SecurePassphraseManager(context)
            val passphrase = passphraseManager.getOrCreatePassphrase()
            val factory = SupportFactory(passphrase)
            
            val db = Room.databaseBuilder(
                context.applicationContext,
                PyeraDatabase::class.java,
                DATABASE_NAME
            )
            .openHelperFactory(factory)
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .build()
            
            return WidgetDataProvider(db).also { instance = it }
        }
        
        suspend fun getBalanceData(context: Context, accountId: String? = null): BalanceData {
            return getInstance(context).fetchBalanceData(accountId)
        }
        
        suspend fun getRecentTransactions(
            context: Context, 
            accountId: String? = null,
            limit: Int = 5
        ): List<WidgetTransaction> {
            return getInstance(context).fetchRecentTransactions(accountId, limit)
        }
        
        fun formatCurrency(amount: Double): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            return formatter.format(amount)
        }
        
        fun openApp(context: Context) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }
        
        fun openAddTransaction(context: Context, type: String) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                data = "pyera://add_transaction?type=$type".toUri()
                putExtra("widget_action", "add_transaction")
                putExtra("transaction_type", type)
            }
            context.startActivity(intent)
        }
        
        fun openTransactionsList(context: Context) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                data = "pyera://transactions".toUri()
                putExtra("widget_action", "view_transactions")
            }
            context.startActivity(intent)
        }
    }
    
    suspend fun fetchBalanceData(accountId: String? = null): BalanceData = withContext(Dispatchers.IO) {
        val transactions = database.transactionDao().getAllTransactions().first()
        
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val monthStart = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        val monthEnd = calendar.timeInMillis
        
        var totalBalance = 0.0
        var monthlyIncome = 0.0
        var monthlyExpense = 0.0
        
        transactions.forEach { transaction ->
            val amount = if (transaction.type == "INCOME") transaction.amount else -transaction.amount
            totalBalance += amount
            
            if (transaction.date in monthStart until monthEnd) {
                if (transaction.type == "INCOME") {
                    monthlyIncome += transaction.amount
                } else {
                    monthlyExpense += transaction.amount
                }
            }
        }
        
        BalanceData(
            totalBalance = totalBalance,
            monthlyIncome = monthlyIncome,
            monthlyExpense = monthlyExpense
        )
    }
    
    suspend fun fetchRecentTransactions(
        accountId: String? = null,
        limit: Int = 5
    ): List<WidgetTransaction> = withContext(Dispatchers.IO) {
        val transactions = database.transactionDao().getAllTransactions().first()
            .sortedByDescending { it.date }
            .take(limit)
        
        val categories = database.categoryDao().getAllCategories().first()
            .associateBy { it.id }
        
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        
        transactions.map { transaction ->
            val category = transaction.categoryId?.let { categories[it] }
            
            WidgetTransaction(
                id = transaction.id,
                amount = transaction.amount,
                note = transaction.note,
                categoryName = category?.name ?: "Uncategorized",
                categoryIcon = category?.icon,
                type = transaction.type,
                date = dateFormat.format(transaction.date)
            )
        }
    }
}

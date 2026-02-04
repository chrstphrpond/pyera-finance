package com.pyera.app.data.local

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.TransactionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: PyeraDatabase
) {
    companion object {
        private const val PREFS_NAME = "pyera_prefs"
        private const val KEY_DATA_SEEDED = "data_seeded"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isDataSeeded(): Boolean {
        return prefs.getBoolean(KEY_DATA_SEEDED, false)
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun markOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }

    suspend fun seedInitialData() {
        if (isDataSeeded()) return

        seedCategories()
        seedSampleTransactions()

        prefs.edit().putBoolean(KEY_DATA_SEEDED, true).apply()
    }

    private suspend fun seedCategories() {
        val categoryDao = database.categoryDao()

        // Check if categories already exist
        val existingCategories = categoryDao.getAllCategories().first()
        if (existingCategories.isNotEmpty()) return

        val defaultCategories = listOf(
            // Expense Categories
            CategoryEntity(
                name = "Food & Dining",
                icon = "restaurant",
                color = Color(0xFFFF6B6B).toArgb(), // Red
                type = "EXPENSE"
            ),
            CategoryEntity(
                name = "Transportation",
                icon = "directions_car",
                color = Color(0xFF4ECDC4).toArgb(), // Teal
                type = "EXPENSE"
            ),
            CategoryEntity(
                name = "Shopping",
                icon = "shopping_bag",
                color = Color(0xFFFFBE0B).toArgb(), // Yellow
                type = "EXPENSE"
            ),
            CategoryEntity(
                name = "Entertainment",
                icon = "movie",
                color = Color(0xFF9B5DE5).toArgb(), // Purple
                type = "EXPENSE"
            ),
            CategoryEntity(
                name = "Bills & Utilities",
                icon = "receipt",
                color = Color(0xFFFF006E).toArgb(), // Pink
                type = "EXPENSE"
            ),
            CategoryEntity(
                name = "Health",
                icon = "local_hospital",
                color = Color(0xFF00BBF9).toArgb(), // Blue
                type = "EXPENSE"
            ),
            CategoryEntity(
                name = "Education",
                icon = "school",
                color = Color(0xFF00F5D4).toArgb(), // Cyan
                type = "EXPENSE"
            ),
            CategoryEntity(
                name = "Personal Care",
                icon = "spa",
                color = Color(0xFFFB5607).toArgb(), // Orange
                type = "EXPENSE"
            ),
            // Income Categories
            CategoryEntity(
                name = "Salary",
                icon = "payments",
                color = Color(0xFF06FFA5).toArgb(), // Green
                type = "INCOME"
            ),
            CategoryEntity(
                name = "Freelance",
                icon = "computer",
                color = Color(0xFF3A86FF).toArgb(), // Blue
                type = "INCOME"
            ),
            CategoryEntity(
                name = "Investments",
                icon = "trending_up",
                color = Color(0xFF8338EC).toArgb(), // Violet
                type = "INCOME"
            ),
            CategoryEntity(
                name = "Gifts",
                icon = "card_giftcard",
                color = Color(0xFFFF006E).toArgb(), // Pink
                type = "INCOME"
            ),
            CategoryEntity(
                name = "Other Income",
                icon = "attach_money",
                color = Color(0xFFFFBE0B).toArgb(), // Yellow
                type = "INCOME"
            )
        )

        defaultCategories.forEach { category ->
            categoryDao.insertCategory(category)
        }
    }

    private suspend fun seedSampleTransactions() {
        val transactionDao = database.transactionDao()
        val categoryDao = database.categoryDao()

        // Check if transactions already exist
        val existingTransactions = transactionDao.getAllTransactions().first()
        if (existingTransactions.isNotEmpty()) return

        // Get categories for reference
        val categories = categoryDao.getAllCategories().first()
        val expenseCategories = categories.filter { it.type == "EXPENSE" }
        val incomeCategories = categories.filter { it.type == "INCOME" }

        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000

        val sampleTransactions = listOf(
            // Recent transactions (today and yesterday)
            TransactionEntity(
                amount = 450.00,
                note = "Grocery shopping",
                date = now - (2 * 60 * 60 * 1000), // 2 hours ago
                type = "EXPENSE",
                categoryId = expenseCategories.find { it.name == "Food & Dining" }?.id
            ),
            TransactionEntity(
                amount = 120.00,
                note = "Grab ride to work",
                date = now - (5 * 60 * 60 * 1000), // 5 hours ago
                type = "EXPENSE",
                categoryId = expenseCategories.find { it.name == "Transportation" }?.id
            ),
            TransactionEntity(
                amount = 25000.00,
                note = "Monthly salary",
                date = now - oneDay, // Yesterday
                type = "INCOME",
                categoryId = incomeCategories.find { it.name == "Salary" }?.id
            ),
            TransactionEntity(
                amount = 899.00,
                note = "New shoes",
                date = now - oneDay,
                type = "EXPENSE",
                categoryId = expenseCategories.find { it.name == "Shopping" }?.id
            ),
            // Earlier this week
            TransactionEntity(
                amount = 350.00,
                note = "Dinner with friends",
                date = now - (3 * oneDay),
                type = "EXPENSE",
                categoryId = expenseCategories.find { it.name == "Food & Dining" }?.id
            ),
            TransactionEntity(
                amount = 1500.00,
                note = "Electric bill",
                date = now - (4 * oneDay),
                type = "EXPENSE",
                categoryId = expenseCategories.find { it.name == "Bills & Utilities" }?.id
            ),
            TransactionEntity(
                amount = 5000.00,
                note = "Freelance project",
                date = now - (5 * oneDay),
                type = "INCOME",
                categoryId = incomeCategories.find { it.name == "Freelance" }?.id
            ),
            TransactionEntity(
                amount = 600.00,
                note = "Movie and popcorn",
                date = now - (6 * oneDay),
                type = "EXPENSE",
                categoryId = expenseCategories.find { it.name == "Entertainment" }?.id
            ),
            // Last week
            TransactionEntity(
                amount = 200.00,
                note = "Medicine",
                date = now - (8 * oneDay),
                type = "EXPENSE",
                categoryId = expenseCategories.find { it.name == "Health" }?.id
            ),
            TransactionEntity(
                amount = 1200.00,
                note = "Online course",
                date = now - (10 * oneDay),
                type = "EXPENSE",
                categoryId = expenseCategories.find { it.name == "Education" }?.id
            ),
            TransactionEntity(
                amount = 350.00,
                note = "Haircut",
                date = now - (12 * oneDay),
                type = "EXPENSE",
                categoryId = expenseCategories.find { it.name == "Personal Care" }?.id
            ),
            TransactionEntity(
                amount = 1500.00,
                note = "Stock dividends",
                date = now - (14 * oneDay),
                type = "INCOME",
                categoryId = incomeCategories.find { it.name == "Investments" }?.id
            )
        )

        sampleTransactions.forEach { transaction ->
            transactionDao.insertTransaction(transaction)
        }
    }

    suspend fun clearAllData() {
        database.clearAllTables()
        prefs.edit()
            .putBoolean(KEY_DATA_SEEDED, false)
            .apply()
    }

    fun resetOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, false).apply()
    }
}

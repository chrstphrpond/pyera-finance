package com.pyera.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Enum representing different types of financial accounts
 */
enum class AccountType {
    BANK,       // Traditional bank accounts (savings, checking)
    EWALLET,    // E-wallets (GCash, PayMaya, etc.)
    CASH,       // Physical cash
    CREDIT_CARD,// Credit cards
    INVESTMENT, // Investment accounts (stocks, mutual funds, etc.)
    OTHER       // Other account types
}

/**
 * Entity representing a financial account in the database.
 * Used to track multiple accounts/bank accounts for the user.
 */
@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["userId"], name = "idx_accounts_user"),
        Index(value = ["isDefault"], name = "idx_accounts_default"),
        Index(value = ["isArchived"], name = "idx_accounts_archived"),
        Index(value = ["type"], name = "idx_accounts_type")
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val name: String, // e.g., "BPI Savings", "GCash", "Cash"
    val type: AccountType,
    val balance: Double = 0.0,
    val currency: String = "PHP",
    val color: Int, // For UI color coding (stored as Color.toArgb())
    val icon: String, // Icon identifier (emoji or resource name)
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Extension function to format account balance as currency string
 */
fun AccountEntity.formattedBalance(): String {
    return when (currency) {
        "PHP" -> "₱${String.format("%,.2f", balance)}"
        "USD" -> "$${String.format("%,.2f", balance)}"
        "EUR" -> "€${String.format("%,.2f", balance)}"
        else -> "$currency ${String.format("%,.2f", balance)}"
    }
}

/**
 * Extension function to get display name for account type
 */
fun AccountType.displayName(): String {
    return when (this) {
        AccountType.BANK -> "Bank Account"
        AccountType.EWALLET -> "E-Wallet"
        AccountType.CASH -> "Cash"
        AccountType.CREDIT_CARD -> "Credit Card"
        AccountType.INVESTMENT -> "Investment"
        AccountType.OTHER -> "Other"
    }
}

/**
 * Extension function to get default icon for account type
 */
fun AccountType.defaultIcon(): String {
    return when (this) {
        AccountType.BANK -> "🏦"
        AccountType.EWALLET -> "📱"
        AccountType.CASH -> "💵"
        AccountType.CREDIT_CARD -> "💳"
        AccountType.INVESTMENT -> "📈"
        AccountType.OTHER -> "💰"
    }
}

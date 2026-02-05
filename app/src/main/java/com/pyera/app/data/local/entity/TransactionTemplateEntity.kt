package com.pyera.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a transaction template for quick entry.
 * Users can save frequent transactions as templates for one-tap entry.
 */
@Entity(
    tableName = "transaction_templates",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["userId"], name = "idx_templates_user"),
        Index(value = ["isActive"], name = "idx_templates_active"),
        Index(value = ["displayOrder"], name = "idx_templates_order"),
        Index(value = ["categoryId"], name = "idx_templates_category"),
        Index(value = ["accountId"], name = "idx_templates_account")
    ]
)
data class TransactionTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val name: String,              // Template name (e.g., "Morning Coffee")
    val description: String,       // Default description/note
    val amount: Double?,           // Optional fixed amount (null for variable)
    val type: String,              // "INCOME" or "EXPENSE"
    val categoryId: Int?,          // Optional category
    val accountId: Long?,          // Optional account
    val icon: String?,             // Emoji icon for template
    val color: Int?,               // Custom color
    val displayOrder: Int = 0,     // For custom ordering
    val isActive: Boolean = true,
    val useCount: Int = 0,         // Track usage for smart sorting
    val lastUsedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Check if this template has a fixed amount
     */
    fun hasFixedAmount(): Boolean = amount != null && amount > 0

    /**
     * Get display icon with fallback
     */
    fun getDisplayIcon(): String = icon ?: getDefaultIcon()

    private fun getDefaultIcon(): String = when (type) {
        "INCOME" -> "💰"
        else -> "💳"
    }

    /**
     * Get formatted amount string or placeholder for variable amount
     */
    fun getAmountDisplay(): String {
        return if (hasFixedAmount()) {
            String.format("₱%.2f", amount)
        } else {
            "Variable"
        }
    }
}

/**
 * Data class for using a template to create a transaction
 */
data class TemplateTransactionData(
    val description: String,
    val amount: Double?,
    val type: String,
    val categoryId: Int?,
    val accountId: Long?
)

/**
 * Convert template to transaction data
 */
fun TransactionTemplateEntity.toTransactionData(): TemplateTransactionData {
    return TemplateTransactionData(
        description = description,
        amount = amount,
        type = type,
        categoryId = categoryId,
        accountId = accountId
    )
}

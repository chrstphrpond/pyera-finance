package com.pyera.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Frequency options for recurring transactions
 */
enum class RecurringFrequency {
    DAILY, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, YEARLY
}

/**
 * Transaction type for recurring transactions
 */
enum class TransactionType {
    INCOME, EXPENSE
}

/**
 * Entity representing a recurring transaction.
 * Used to automatically create transactions at specified intervals.
 */
@Entity(
    tableName = "recurring_transactions",
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
        Index(value = ["categoryId"], name = "idx_recurring_category"),
        Index(value = ["accountId"], name = "idx_recurring_account"),
        Index(value = ["nextDueDate"], name = "idx_recurring_next_due"),
        Index(value = ["isActive"], name = "idx_recurring_active")
    ]
)
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType, // INCOME or EXPENSE
    val categoryId: Long?,
    val accountId: Long?,
    val description: String,
    val frequency: RecurringFrequency, // DAILY, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, YEARLY
    val startDate: Long, // Epoch timestamp
    val endDate: Long?, // Epoch timestamp, null = never ends
    val nextDueDate: Long, // Epoch timestamp
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

package com.pyera.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a financial transaction.
 * Links to both CategoryEntity and AccountEntity.
 */
@Entity(
    tableName = "transactions",
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
            onDelete = ForeignKey.RESTRICT // Prevent deleting accounts with transactions
        )
    ],
    indices = [
        Index(value = ["categoryId"], name = "idx_transactions_category"),
        Index(value = ["date"], name = "idx_transactions_date"),
        Index(value = ["type"], name = "idx_transactions_type"),
        Index(value = ["accountId"], name = "idx_transactions_account"),
        Index(value = ["userId"], name = "idx_transactions_user")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val note: String,
    val date: Long, // Epoch timestamp
    val type: String, // "INCOME" or "EXPENSE"
    val categoryId: Int? = null, // Reference to CategoryEntity (kept as Int for consistency)
    val accountId: Long, // Link to the account this transaction belongs to
    val userId: String, // User who owns this transaction
    val isTransfer: Boolean = false, // Flag for transfer transactions
    val transferAccountId: Long? = null, // For transfers, the destination account
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Receipt attachment fields
    val receiptImagePath: String? = null, // Local file path
    val receiptCloudUrl: String? = null,  // Firebase Storage URL
    val hasReceipt: Boolean = false
)

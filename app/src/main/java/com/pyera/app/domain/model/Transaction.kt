package com.pyera.app.domain.model

/**
 * Domain model representing a financial transaction.
 */
data class Transaction(
    val id: Long,
    val amount: Double,
    val note: String,
    val date: Long,
    val type: String,
    val categoryId: Int?,
    val accountId: Long,
    val userId: String,
    val isTransfer: Boolean,
    val transferAccountId: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val receiptImagePath: String?,
    val receiptCloudUrl: String?,
    val hasReceipt: Boolean
)

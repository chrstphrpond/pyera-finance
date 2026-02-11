package com.pyera.app.domain.repository

import com.pyera.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>
    suspend fun insertTransaction(transaction: TransactionEntity)
    suspend fun insertTransactionAndReturnId(transaction: TransactionEntity): Long
    suspend fun deleteTransaction(transaction: TransactionEntity)
    suspend fun updateTransaction(transaction: TransactionEntity)
    suspend fun getTransactionsForExport(): List<TransactionEntity>
    
    /**
     * Sync pending transactions to cloud
     * Called by SyncWorker for background synchronization
     */
    suspend fun syncPendingTransactions(): Result<Unit>
}

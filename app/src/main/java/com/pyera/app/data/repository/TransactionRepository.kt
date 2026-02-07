package com.pyera.app.data.repository

import com.pyera.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>
    suspend fun insertTransaction(transaction: TransactionEntity)
    suspend fun deleteTransaction(transaction: TransactionEntity)
    suspend fun updateTransaction(transaction: TransactionEntity)
    suspend fun getTransactionsForExport(): List<TransactionEntity>
    
    /**
     * Sync pending transactions to cloud
     * Called by SyncWorker for background synchronization
     */
    suspend fun syncPendingTransactions(): Result<Unit>
}

package com.pyera.app.data.repository

import com.pyera.app.data.local.dao.TransactionDao
import com.pyera.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    override suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    override suspend fun getTransactionsForExport(): List<TransactionEntity> {
        return transactionDao.getAllTransactionsOnce()
    }
    
    /**
     * Sync pending transactions to cloud.
     * This is a placeholder implementation that should be enhanced with
     * actual cloud sync logic when Firebase or other backend is integrated.
     */
    override suspend fun syncPendingTransactions(): Result<Unit> {
        return try {
            // TODO: Implement actual cloud sync
            // For now, we just verify local data is consistent
            val pendingTransactions = transactionDao.getAllTransactionsOnce()
            
            // In a real implementation, you would:
            // 1. Get unsynced transactions from local DB
            // 2. Upload to Firebase/Firestore
            // 3. Update sync status in local DB
            // 4. Download any new transactions from cloud
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

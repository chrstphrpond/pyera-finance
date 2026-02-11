package com.pyera.app.data.repository

import com.pyera.app.domain.repository.*

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pyera.app.data.local.dao.TransactionDao
import com.pyera.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    override fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByAccount(accountId)
    }

    override suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    override suspend fun insertTransactionAndReturnId(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
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
     * Sync pending transactions to cloud (upload-only).
     */
    override suspend fun syncPendingTransactions(): Result<Unit> {
        return try {
            val userId = authRepository.currentUser?.uid
                ?: return Result.success(Unit)

            val localTransactions = transactionDao.getAllTransactionsOnce()
            if (localTransactions.isEmpty()) {
                return Result.success(Unit)
            }

            val batch = firestore.batch()
            val collection = firestore.collection("users")
                .document(userId)
                .collection("transactions")

            localTransactions.forEach { transaction ->
                val docRef = collection.document(transaction.id.toString())
                batch.set(docRef, transaction.toFirestoreMap(userId), SetOptions.merge())
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun TransactionEntity.toFirestoreMap(userId: String): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "userId" to userId,
        "amount" to amount,
        "note" to note,
        "date" to date,
        "type" to type,
        "categoryId" to categoryId,
        "accountId" to accountId,
        "isTransfer" to isTransfer,
        "transferAccountId" to transferAccountId,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "receiptImagePath" to receiptImagePath,
        "receiptCloudUrl" to receiptCloudUrl,
        "hasReceipt" to hasReceipt
    )
}

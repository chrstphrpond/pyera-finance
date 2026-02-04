package com.pyera.app.data.repository

import com.pyera.app.data.local.dao.TransactionDao
import com.pyera.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnalysisRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : AnalysisRepository {

    override fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }
}

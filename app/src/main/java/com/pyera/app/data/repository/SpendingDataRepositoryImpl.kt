package com.pyera.app.data.repository

import com.pyera.app.data.local.dao.TransactionDao
import com.pyera.app.data.mapper.toDomain
import com.pyera.app.domain.repository.SpendingDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SpendingDataRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : SpendingDataRepository {

    override fun getAllTransactions(): Flow<List<com.pyera.app.domain.model.Transaction>> {
        return transactionDao.getAllTransactions()
            .map { transactions -> transactions.map { it.toDomain() } }
    }
}

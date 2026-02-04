package com.pyera.app.data.repository

import com.pyera.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface AnalysisRepository {
    fun getAllTransactions(): Flow<List<TransactionEntity>>
}

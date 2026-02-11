package com.pyera.app.domain.repository

import com.pyera.app.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Domain-facing access to transaction data for analytics and prediction.
 */
interface SpendingDataRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
}

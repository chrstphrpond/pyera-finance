package com.pyera.app.data.repository

import com.pyera.app.data.local.entity.DebtEntity
import kotlinx.coroutines.flow.Flow

interface DebtRepository {
    fun getAllDebts(): Flow<List<DebtEntity>>
    suspend fun addDebt(debt: DebtEntity)
    suspend fun updateDebt(debt: DebtEntity)
    suspend fun deleteDebt(debt: DebtEntity)
}

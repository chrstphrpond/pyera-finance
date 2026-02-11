package com.pyera.app.domain.repository

import com.pyera.app.data.local.entity.InvestmentEntity
import kotlinx.coroutines.flow.Flow

interface InvestmentRepository {
    fun getAllInvestments(): Flow<List<InvestmentEntity>>
    suspend fun addInvestment(investment: InvestmentEntity)
    suspend fun updateInvestment(investment: InvestmentEntity)
    suspend fun deleteInvestment(investment: InvestmentEntity)
}

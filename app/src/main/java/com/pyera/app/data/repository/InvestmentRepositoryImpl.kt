package com.pyera.app.data.repository

import com.pyera.app.domain.repository.*

import com.pyera.app.data.local.dao.InvestmentDao
import com.pyera.app.data.local.entity.InvestmentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InvestmentRepositoryImpl @Inject constructor(
    private val investmentDao: InvestmentDao
) : InvestmentRepository {

    override fun getAllInvestments(): Flow<List<InvestmentEntity>> {
        return investmentDao.getAllInvestments()
    }

    override suspend fun addInvestment(investment: InvestmentEntity) {
        investmentDao.insertInvestment(investment)
    }

    override suspend fun updateInvestment(investment: InvestmentEntity) {
        investmentDao.updateInvestment(investment)
    }

    override suspend fun deleteInvestment(investment: InvestmentEntity) {
        investmentDao.deleteInvestment(investment)
    }
}

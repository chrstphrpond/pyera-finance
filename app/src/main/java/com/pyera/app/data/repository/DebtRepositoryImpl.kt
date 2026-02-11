package com.pyera.app.data.repository

import com.pyera.app.domain.repository.*

import com.pyera.app.data.local.dao.DebtDao
import com.pyera.app.data.local.entity.DebtEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DebtRepositoryImpl @Inject constructor(
    private val debtDao: DebtDao
) : DebtRepository {

    override fun getAllDebts(): Flow<List<DebtEntity>> {
        return debtDao.getAllDebts()
    }

    override suspend fun addDebt(debt: DebtEntity) {
        debtDao.insertDebt(debt)
    }

    override suspend fun updateDebt(debt: DebtEntity) {
        debtDao.updateDebt(debt)
    }

    override suspend fun deleteDebt(debt: DebtEntity) {
        debtDao.deleteDebt(debt)
    }
}

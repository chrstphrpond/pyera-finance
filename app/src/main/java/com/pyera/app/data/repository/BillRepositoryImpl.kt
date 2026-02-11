package com.pyera.app.data.repository

import com.pyera.app.domain.repository.*

import com.pyera.app.data.local.dao.BillDao
import com.pyera.app.data.local.entity.BillEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BillRepositoryImpl @Inject constructor(
    private val billDao: BillDao
) : BillRepository {

    override fun getAllBills(): Flow<List<BillEntity>> {
        return billDao.getAllBills()
    }

    override suspend fun addBill(bill: BillEntity) {
        billDao.insertBill(bill)
    }

    override suspend fun updateBill(bill: BillEntity) {
        billDao.updateBill(bill)
    }

    override suspend fun deleteBill(bill: BillEntity) {
        billDao.deleteBill(bill)
    }
}

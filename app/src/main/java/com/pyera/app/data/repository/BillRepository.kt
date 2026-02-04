package com.pyera.app.data.repository

import com.pyera.app.data.local.entity.BillEntity
import kotlinx.coroutines.flow.Flow

interface BillRepository {
    fun getAllBills(): Flow<List<BillEntity>>
    suspend fun addBill(bill: BillEntity)
    suspend fun updateBill(bill: BillEntity)
    suspend fun deleteBill(bill: BillEntity)
}

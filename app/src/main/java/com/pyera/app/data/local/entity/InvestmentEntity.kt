package com.pyera.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: String, // "STOCK", "CRYPTO", "REAL_ESTATE", "OTHER"
    val amountInvested: Double,
    val currentValue: Double
)

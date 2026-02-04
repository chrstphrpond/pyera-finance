package com.pyera.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String, // Person or entity name
    val amount: Double,
    val dueDate: Long,
    val type: String, // "PAYABLE" (I owe) or "RECEIVABLE" (Owed to me)
    val isPaid: Boolean = false
)

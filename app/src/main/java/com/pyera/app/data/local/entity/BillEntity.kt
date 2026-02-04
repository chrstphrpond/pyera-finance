package com.pyera.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val amount: Double,
    val dueDate: Long,
    val frequency: String, // "MONTHLY", "YEARLY", "ONE_TIME"
    val isPaid: Boolean = false
)

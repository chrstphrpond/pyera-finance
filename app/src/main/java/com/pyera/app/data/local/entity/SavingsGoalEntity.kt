package com.pyera.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "savings_goals",
    indices = [
        Index(value = ["deadline"], name = "idx_savings_deadline"),
        Index(value = ["targetAmount"], name = "idx_savings_target")
    ]
)
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: Long,
    val icon: Int,
    val color: Int
)

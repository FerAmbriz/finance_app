package com.passiveincome.tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_balances")
data class MonthlyBalance(
    @PrimaryKey val monthYear: String, // Format: "MM-YYYY"
    val totalBalance: Double,
    val timestamp: Long = System.currentTimeMillis()
)

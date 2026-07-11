package com.passiveincome.tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movements")
data class Movement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sourceId: Int, // Refers to the IncomeSource id
    val sourceName: String, // Cached source name for easy listing
    val amount: Double,
    val type: String, // "Depósito", "Retiro", "Rendimiento"
    val timestamp: Long = System.currentTimeMillis(),
    val description: String
)

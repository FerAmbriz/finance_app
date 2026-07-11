package com.passiveincome.tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "income_sources")
data class IncomeSource(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // e.g. "SOFIPO", "Banco", "Cetes", "Otro"
    val balance: Double,
    val annualRate: Double, // e.g. 0.15 for 15%
    val colorHex: String // Hex color to display in the donut chart (e.g. "#FF6D00")
)

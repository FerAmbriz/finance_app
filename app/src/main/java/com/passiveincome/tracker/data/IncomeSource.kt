package com.passiveincome.tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "income_sources")
data class IncomeSource(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String,
    val colorHex: String,
    val balance: Double = 0.0,
    val annualRate: Double = 0.0,
    val hasLimit: Boolean = false,
    val limitAmount: Double = 0.0,
    val hasSecondaryRate: Boolean = false,
    val secondaryRate: Double = 0.0,
    val hasTertiaryRate: Boolean = false,
    val limitAmount2: Double = 0.0,
    val tertiaryRate: Double = 0.0,
    val hasHardCap: Boolean = false,
    val hardCapAmount: Double = 0.0,
    val lastUpdateMillis: Long = System.currentTimeMillis()
) {
    // Computed properties for compatibility with existing UI components
    val totalBalance: Double get() = balance
    
    val rate1: Double get() = annualRate
    val rate2: Double get() = secondaryRate
    val rate3: Double get() = tertiaryRate

    val hasTier2: Boolean get() = hasLimit && hasSecondaryRate
    val hasTier3: Boolean get() = hasTier2 && hasTertiaryRate

    val balance1: Double get() = if (hasLimit && limitAmount > 0) minOf(balance, limitAmount) else balance
    val balance2: Double get() {
        if (!hasTier2) return 0.0
        val l2 = if (hasTertiaryRate && limitAmount2 > 0) limitAmount2 else Double.MAX_VALUE
        return minOf(maxOf(0.0, balance - limitAmount), l2 - limitAmount)
    }
    val balance3: Double get() {
        if (!hasTier3) return 0.0
        return maxOf(0.0, balance - limitAmount2)
    }
}

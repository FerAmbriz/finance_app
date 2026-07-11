package com.passiveincome.tracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.passiveincome.tracker.data.AppDatabase
import com.passiveincome.tracker.data.IncomeRepository
import com.passiveincome.tracker.data.IncomeSource
import com.passiveincome.tracker.data.Movement
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IncomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: IncomeRepository

    val allSources: StateFlow<List<IncomeSource>>
    val allMovements: StateFlow<List<Movement>>

    init {
        val database = AppDatabase.getDatabase(application)
        val dao = database.incomeDao()
        repository = IncomeRepository(dao)

        allSources = repository.allSources.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allMovements = repository.allMovements.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed default passive income sources if the database is brand new
        viewModelScope.launch {
            try {
                val currentSources = repository.allSources.first()
                if (currentSources.isEmpty()) {
                    insertSource(IncomeSource(name = "Nu México", type = "SOFIPO", balance = 25000.0, annualRate = 0.1475, colorHex = "#8B5CF6"))
                    insertSource(IncomeSource(name = "Cetes Directo", type = "Cetes", balance = 50000.0, annualRate = 0.1100, colorHex = "#10B981"))
                    insertSource(IncomeSource(name = "Kubo Financiero", type = "SOFIPO", balance = 15000.0, annualRate = 0.1460, colorHex = "#F59E0B"))
                    insertSource(IncomeSource(name = "Finsus", type = "SOFIPO", balance = 10000.0, annualRate = 0.1507, colorHex = "#06B6D4"))
                }
            } catch (e: Exception) {
                // Ignore seeding errors
            }
        }
    }

    fun insertSource(source: IncomeSource) = viewModelScope.launch {
        val id = repository.insertSource(source)
        if (source.balance > 0.0) {
            repository.insertMovement(
                Movement(
                    sourceId = id.toInt(),
                    sourceName = source.name,
                    amount = source.balance,
                    type = "Depósito",
                    description = "Depósito inicial"
                )
            )
        }
    }

    fun transact(source: IncomeSource, amount: Double, type: String, description: String) = viewModelScope.launch {
        val updatedSource = source.copy(balance = source.balance + amount)
        repository.updateSource(updatedSource)
        repository.insertMovement(
            Movement(
                sourceId = source.id,
                sourceName = source.name,
                amount = amount,
                type = type,
                description = description
            )
        )
    }

    fun addYield(source: IncomeSource, yieldAmount: Double, rateApplied: Double, description: String) = viewModelScope.launch {
        val updatedSource = source.copy(balance = source.balance + yieldAmount)
        repository.updateSource(updatedSource)
        repository.insertMovement(
            Movement(
                sourceId = source.id,
                sourceName = source.name,
                amount = yieldAmount,
                type = "Rendimiento",
                description = description
            )
        )
    }

    fun deleteSource(source: IncomeSource) = viewModelScope.launch {
        repository.deleteSource(source)
    }

    fun updateSource(source: IncomeSource) = viewModelScope.launch {
        repository.updateSource(source)
    }
}

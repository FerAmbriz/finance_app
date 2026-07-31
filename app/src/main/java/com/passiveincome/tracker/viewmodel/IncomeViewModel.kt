package com.passiveincome.tracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.passiveincome.tracker.data.AppDatabase
import com.passiveincome.tracker.data.IncomeRepository
import com.passiveincome.tracker.data.IncomeSource
import com.passiveincome.tracker.data.Movement
import com.passiveincome.tracker.data.MonthlyBalance
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class IncomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: IncomeRepository

    val allSources: StateFlow<List<IncomeSource>>
    val allMovements: StateFlow<List<Movement>>
    val allMonthlyBalances: StateFlow<List<MonthlyBalance>>

    init {
        val database = AppDatabase.getDatabase(application)
        val dao = database.incomeDao()
        repository = IncomeRepository(dao)

        allSources = repository.allSources.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allMonthlyBalances = repository.allMonthlyBalances.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allMovements = combine(repository.allMovements, repository.allMonthlyBalances) { movements, monthly ->
            val monthlyAsMovements = monthly.map { 
                Movement(
                    sourceId = -1,
                    sourceName = "Balance Total",
                    amount = it.totalBalance,
                    type = "Cierre Mensual",
                    timestamp = it.timestamp,
                    description = "Cierre de mes"
                )
            }
            (movements + monthlyAsMovements).sortedByDescending { it.timestamp }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed default passive income sources if the database is brand new
        viewModelScope.launch {
            try {
                val currentSources = repository.allSources.first()
                if (currentSources.isEmpty()) {
                    insertSource(IncomeSource(
                        name = "Nu México",
                        type = "SOFIPO",
                        balance = 25000.0,
                        annualRate = 0.15,
                        hasLimit = true,
                        limitAmount = 20000.0,
                        hasSecondaryRate = true,
                        secondaryRate = 0.09,
                        colorHex = "#3B82F6"
                    ))
                    insertSource(IncomeSource(
                        name = "Cetes Directo",
                        type = "Cetes",
                        balance = 50000.0,
                        annualRate = 0.11,
                        colorHex = "#10B981"
                    ))
                } else {
                    // Check and apply daily yields for existing sources
                    applyDailyYields(currentSources)
                    
                    // Check and record monthly balance snapshot
                    checkAndRecordMonthlyBalance(currentSources)
                }
            } catch (e: Exception) {
                // Ignore seeding errors
            }
        }
    }

    private suspend fun checkAndRecordMonthlyBalance(sources: List<IncomeSource>) {
        val sdf = SimpleDateFormat("MM-yyyy", Locale.US)
        val currentMonthYear = sdf.format(Date())
        
        val monthlyBalances = repository.allMonthlyBalances.first()
        val alreadyRecorded = monthlyBalances.any { it.monthYear == currentMonthYear }

        if (!alreadyRecorded) {
            val totalBalance = sources.sumOf { it.totalBalance }
            repository.insertMonthlyBalance(
                MonthlyBalance(
                    monthYear = currentMonthYear,
                    totalBalance = totalBalance
                )
            )
        }
    }

    private suspend fun applyDailyYields(sources: List<IncomeSource>) {
        val now = System.currentTimeMillis()
        sources.forEach { source ->
            val diffMillis = now - source.lastUpdateMillis
            val daysPassed = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

            if (daysPassed > 0) {
                var currentBalance = source.balance
                var totalYield = 0.0

                repeat(daysPassed) {
                    val tempSource = source.copy(balance = currentBalance)
                    val y1 = tempSource.balance1 * (source.rate1 / 365.0)
                    val y2 = if (source.hasTier2) tempSource.balance2 * (source.rate2 / 365.0) else 0.0
                    val y3 = if (source.hasTier3) tempSource.balance3 * (source.rate3 / 365.0) else 0.0
                    
                    val dailyYield = y1 + y2 + y3
                    currentBalance += dailyYield
                    totalYield += dailyYield
                }

                if (totalYield > 0.0) {
                    repository.updateSource(source.copy(
                        balance = currentBalance,
                        lastUpdateMillis = now
                    ))
                    repository.insertMovement(
                        Movement(
                            sourceId = source.id,
                            sourceName = source.name,
                            amount = totalYield,
                            type = "Rendimiento",
                            description = "Rendimiento automático ($daysPassed d)"
                        )
                    )
                }
            }
        }
    }

    fun insertSource(source: IncomeSource) = viewModelScope.launch {
        val id = repository.insertSource(source)
        if (source.totalBalance > 0.0) {
            repository.insertMovement(
                Movement(
                    sourceId = id.toInt(),
                    sourceName = source.name,
                    amount = source.totalBalance,
                    type = "Depósito",
                    description = "Depósito inicial"
                )
            )
        }
    }

    fun transact(source: IncomeSource, amount: Double, type: String, description: String) = viewModelScope.launch {
        // Transactions affect the total balance
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
        // Yields are added to the total balance
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

    fun deleteMovement(movement: Movement) = viewModelScope.launch {
        // Find the source and revert the balance
        val source = repository.getSourceById(movement.sourceId)
        if (source != null) {
            val updatedSource = source.copy(balance = source.balance - movement.amount)
            repository.updateSource(updatedSource)
        }
        repository.deleteMovement(movement)
    }

    fun updateSource(source: IncomeSource) = viewModelScope.launch {
        repository.updateSource(source)
    }

    fun transfer(from: IncomeSource, to: IncomeSource, amount: Double, description: String) = viewModelScope.launch {
        // 1. Withdraw from source
        val updatedFrom = from.copy(balance = from.balance - amount)
        repository.updateSource(updatedFrom)
        repository.insertMovement(
            Movement(
                sourceId = from.id,
                sourceName = from.name,
                amount = -amount,
                type = "Transferencia",
                description = "A ${to.name}: $description"
            )
        )
        
        // 2. Deposit to destination
        val updatedTo = to.copy(balance = to.balance + amount)
        repository.updateSource(updatedTo)
        repository.insertMovement(
            Movement(
                sourceId = to.id,
                sourceName = to.name,
                amount = amount,
                type = "Transferencia",
                description = "De ${from.name}: $description"
            )
        )
    }
}

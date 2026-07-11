package com.passiveincome.tracker.data

import kotlinx.coroutines.flow.Flow

class IncomeRepository(private val incomeDao: IncomeDao) {

    val allSources: Flow<List<IncomeSource>> = incomeDao.getAllSources()
    val allMovements: Flow<List<Movement>> = incomeDao.getAllMovements()

    suspend fun getSourceById(id: Int): IncomeSource? {
        return incomeDao.getSourceById(id)
    }

    suspend fun insertSource(source: IncomeSource): Long {
        return incomeDao.insertSource(source)
    }

    suspend fun updateSource(source: IncomeSource) {
        incomeDao.updateSource(source)
    }

    suspend fun deleteSource(source: IncomeSource) {
        incomeDao.deleteSource(source)
        incomeDao.deleteMovementsForSource(source.id)
    }

    suspend fun insertMovement(movement: Movement): Long {
        return incomeDao.insertMovement(movement)
    }
}

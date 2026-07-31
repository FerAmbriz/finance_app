package com.passiveincome.tracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {

    @Query("SELECT * FROM income_sources ORDER BY name ASC")
    fun getAllSources(): Flow<List<IncomeSource>>

    @Query("SELECT * FROM income_sources WHERE id = :id")
    suspend fun getSourceById(id: Int): IncomeSource?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: IncomeSource): Long

    @Update
    suspend fun updateSource(source: IncomeSource)

    @Delete
    suspend fun deleteSource(source: IncomeSource)

    @Query("SELECT * FROM movements ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<Movement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: Movement): Long

    @Delete
    suspend fun deleteMovement(movement: Movement)

    @Query("DELETE FROM movements WHERE sourceId = :sourceId")
    suspend fun deleteMovementsForSource(sourceId: Int)

    @Query("SELECT * FROM monthly_balances ORDER BY timestamp DESC")
    fun getAllMonthlyBalances(): Flow<List<MonthlyBalance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyBalance(balance: MonthlyBalance)
}

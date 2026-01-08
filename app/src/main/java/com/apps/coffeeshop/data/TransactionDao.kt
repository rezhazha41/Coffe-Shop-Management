package com.apps.coffeeshop.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = 'IN' ORDER BY date DESC")
    fun getIncomeTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE type = 'OUT' ORDER BY date DESC")
    fun getExpenseTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)
    
    @Query("SELECT SUM(totalAmount) FROM transactions WHERE type IN ('IN', 'CAPITAL')")
    fun getTotalIncome(): Flow<Double?>
    
    @Query("SELECT SUM(totalAmount) FROM transactions WHERE type = 'IN'")
    fun getTotalSales(): Flow<Double?>
    
    @Query("SELECT SUM(totalAmount) FROM transactions WHERE type IN ('OUT', 'WITHDRAW')")
    fun getTotalExpense(): Flow<Double?>

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND type = 'IN' ORDER BY date ASC")
    fun getSalesAfter(startDate: Long): Flow<List<Transaction>>

    // Optional: Range query if needed explicitly
    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate AND type = 'IN' ORDER BY date ASC")
    fun getSalesInRange(startDate: Long, endDate: Long): Flow<List<Transaction>>
}

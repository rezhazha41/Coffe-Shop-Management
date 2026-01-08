package com.apps.coffeeshop.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long, // Timestamp
    val totalAmount: Double,
    val type: String, // "IN" for Income (Sales), "OUT" for Expense
    val itemsJson: String, // detailed items in JSON format or formatted string
    val note: String = "" // Optional note (e.g., "Electricity Bill")
)

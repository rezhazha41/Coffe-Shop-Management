package com.apps.coffeeshop.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "store_profile")
data class StoreProfile(
    @PrimaryKey
    val id: Int = 1, // Always 1, single row table
    val storeName: String,
    val storeAddress: String,
    val printerMacAddress: String = "",
    val username: String = "admin",
    val password: String = "admin123",
    val cashierPassword: String = "kasir123",
    val logoUri: String? = null,
    val isDarkMode: Boolean = false
)

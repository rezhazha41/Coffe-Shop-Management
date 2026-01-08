package com.apps.coffeeshop.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val price: Double,
    val category: String, // "Kopi", "Teh", "Non-Kopi", "Makanan"
    val imageUrl: String,
    val stock: Int = 0
)

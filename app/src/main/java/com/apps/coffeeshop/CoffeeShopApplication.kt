package com.apps.coffeeshop

import android.app.Application
import com.apps.coffeeshop.data.AppDatabase
import com.apps.coffeeshop.data.ProductRepository

class CoffeeShopApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { 
        ProductRepository(
            database.productDao(),
            database.transactionDao(),
            database.storeProfileDao()
        ) 
    }
}

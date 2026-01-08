package com.apps.coffeeshop.data

import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao,
    private val transactionDao: TransactionDao,
    private val storeProfileDao: StoreProfileDao
) {
    // Products
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    suspend fun insert(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun update(product: Product) {
        productDao.updateProduct(product)
    }

    suspend fun delete(product: Product) {
        productDao.deleteProduct(product)
    }
    
    // Transactions
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val incomeTransactions: Flow<List<Transaction>> = transactionDao.getIncomeTransactions()
    val totalIncome: Flow<Double?> = transactionDao.getTotalIncome()
    val totalSales: Flow<Double?> = transactionDao.getTotalSales()
    val totalExpense: Flow<Double?> = transactionDao.getTotalExpense()
    
    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }
    
    // Profile
    val storeProfile: Flow<StoreProfile?> = storeProfileDao.getProfile()
    
    suspend fun saveProfile(profile: StoreProfile) {
        storeProfileDao.insertOrUpdateProfile(profile)
    }
}

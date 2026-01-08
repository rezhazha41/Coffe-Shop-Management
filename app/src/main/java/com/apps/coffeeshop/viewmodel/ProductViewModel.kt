package com.apps.coffeeshop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apps.coffeeshop.data.Product
import com.apps.coffeeshop.data.ProductRepository
import com.apps.coffeeshop.data.StoreProfile
import com.apps.coffeeshop.data.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartItem(
    val product: Product,
    val quantity: Int
)

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    // --- Product Logic ---
    // Getting all products as a hot flow
    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory
    
    // Filtered products based on selection
    val filteredProducts = combine(allProducts, _selectedCategory) { products, category ->
        if (category == "Semua") products else products.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    // CRUD
    // CRUD
    // CRUD
    fun addProduct(name: String, price: Double, category: String, imageUrl: String, stock: Int) = viewModelScope.launch {
        try {
            repository.insert(Product(name = name, price = price, category = category, imageUrl = imageUrl, stock = stock))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteProduct(product: Product) = viewModelScope.launch {
        try {
            repository.delete(product)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun updateProduct(product: Product) = viewModelScope.launch {
        try {
            repository.update(product)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun updateStock(product: Product, newStock: Int) = viewModelScope.launch {
        try {
            repository.update(product.copy(stock = newStock))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Restock Logic (Operational Flow) ---
    fun restockProduct(product: Product, quantity: Int, cost: Double) = viewModelScope.launch {
        try {
            // 1. Update Stock (+)
            val newStock = product.stock + quantity
            repository.update(product.copy(stock = newStock))

            // 2. Record Expense (Modal Belanja)
            repository.insertTransaction(
                Transaction(
                    date = System.currentTimeMillis(),
                    totalAmount = cost,
                    type = "OUT", // Expense
                    itemsJson = "Restock: ${quantity}x ${product.name}",
                    note = "Restock Modal"
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Cart Logic ---
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    fun addToCart(product: Product) {
        val currentCart = _cartItems.value.toMutableList()
        val existingItemIndex = currentCart.indexOfFirst { it.product.id == product.id }
        
        var currentQtyInCart = 0
        if (existingItemIndex != -1) {
            currentQtyInCart = currentCart[existingItemIndex].quantity
        }
        
        // Simple stock check
        if (currentQtyInCart + 1 > product.stock) {
             // Block adding if out of stock
             return 
        }

        if (existingItemIndex != -1) {
            val existingItem = currentCart[existingItemIndex]
            currentCart[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            currentCart.add(CartItem(product, 1))
        }
        _cartItems.value = currentCart
    }

    fun removeFromCart(product: Product) {
        val currentCart = _cartItems.value.toMutableList()
        val existingItemIndex = currentCart.indexOfFirst { it.product.id == product.id }

        if (existingItemIndex != -1) {
            val existingItem = currentCart[existingItemIndex]
            if (existingItem.quantity > 1) {
                currentCart[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity - 1)
            } else {
                currentCart.removeAt(existingItemIndex)
            }
        }
        _cartItems.value = currentCart
    }
    
    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // --- Transactions (Financials) ---
    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        
    val incomeTransactions: StateFlow<List<Transaction>> = repository.incomeTransactions
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        
    val totalIncome: StateFlow<Double> = repository.totalIncome
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalSales: StateFlow<Double> = repository.totalSales
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
        
    val totalExpense: StateFlow<Double> = repository.totalExpense
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
        
    // Calculate Balance: Total Income (Sales + Capital) - Total Expense
    val currentBalance: StateFlow<Double> = combine(totalIncome, totalExpense) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    fun addTransaction(type: String, amount: Double, items: String = "", note: String = "") {
        viewModelScope.launch {
            try {
                repository.insertTransaction(
                    Transaction(
                        date = System.currentTimeMillis(),
                        totalAmount = amount,
                        type = type,
                        itemsJson = items,
                        note = note
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun processCheckout(totalAmount: Double, items: List<CartItem>) {
        viewModelScope.launch {
            try {
                 // 1. Record Transaction
                 repository.insertTransaction(
                    Transaction(
                        date = System.currentTimeMillis(),
                        totalAmount = totalAmount,
                        type = "IN",
                        itemsJson = items.joinToString(", ") { "${it.quantity}x ${it.product.name}" },
                        note = "Sales"
                    )
                 )
                 
                 // 2. Decrement Stock
                 items.forEach { item ->
                     val newStock = item.product.stock - item.quantity
                     repository.update(item.product.copy(stock = if (newStock < 0) 0 else newStock))
                 }
                 
                 clearCart()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // --- Top Selling Logic ---
    val topSellingProducts: StateFlow<List<Pair<String, Int>>> = allTransactions.map { transactions ->
        val productSales = mutableMapOf<String, Int>()
        
        transactions.filter { it.type == "IN" }.forEach { transaction ->
            // Parse itemsJson: "2x Kopi Susu, 1x Latte"
            transaction.itemsJson.split(", ").forEach { itemStr ->
                val parts = itemStr.split("x ")
                if (parts.size == 2) {
                    val qty = parts[0].trim().toIntOrNull() ?: 0
                    val name = parts[1].trim()
                    productSales[name] = productSales.getOrDefault(name, 0) + qty
                }
            }
        }
        
        productSales.toList().sortedByDescending { it.second }.take(5)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // --- Trend Logic ---
    enum class TrendRange { DAYS_7, DAYS_30 }
    
    private val _trendRange = MutableStateFlow(TrendRange.DAYS_7)
    val trendRange: StateFlow<TrendRange> = _trendRange
    
    fun setTrendRange(range: TrendRange) {
        _trendRange.value = range
    }

    val salesTrendData = combine(allTransactions, _trendRange) { transactions, range ->
        val days = if (range == TrendRange.DAYS_7) 7 else 30
        val calendar = java.util.Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat(if (range == TrendRange.DAYS_7) "EEE" else "dd MMM", java.util.Locale("id", "ID"))
        
        val datesToCheck = (0 until days).map { i ->
             val cal = java.util.Calendar.getInstance()
             cal.add(java.util.Calendar.DAY_OF_YEAR, -((days - 1) - i))
             cal
        }
        
        val salesByDay = transactions
            .filter { it.type == "IN" }
            .groupBy { 
                val date = java.util.Date(it.date)
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(date)
            }
            
        // Income Data
        val incomeData = datesToCheck.map { cal ->
            val dateKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.time)
            val dayLabel = dateFormat.format(cal.time)
            val totalForDay = salesByDay[dateKey]?.sumOf { it.totalAmount } ?: 0.0
            dayLabel to totalForDay.toFloat()
        }
        
        // Quantity Data
        val quantityData = datesToCheck.map { cal ->
            val dateKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.time)
            val dayLabel = dateFormat.format(cal.time)
            val totalQty = salesByDay[dateKey]?.sumOf { trx ->
                trx.itemsJson.split(", ").sumOf { itemStr ->
                    try {
                        itemStr.trim().split("x ")[0].toInt()
                    } catch (e: Exception) { 0 }
                }
            } ?: 0
            dayLabel to totalQty.toFloat()
        }
        
        val totalPeriodSales = incomeData.sumOf { it.second.toDouble() }
        
        Triple(incomeData, quantityData, totalPeriodSales)
    }.stateIn(viewModelScope, SharingStarted.Lazily, Triple(emptyList(), emptyList(), 0.0))

    // --- Profile Logic ---
    val storeProfile: StateFlow<StoreProfile?> = repository.storeProfile
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun updateLogo(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = java.io.File(context.filesDir, "store_logo.jpg")
                val outputStream = java.io.FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                
                // Update specific field in DB (Need to get current profile first or update via modify)
                val current = storeProfile.value
                if (current != null) {
                    repository.saveProfile(current.copy(logoUri = file.absolutePath))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- RBAC (Session) ---
    enum class UserRole {
        ADMIN, CASHIER, NONE
    }

    private val _userRole = MutableStateFlow(UserRole.NONE)
    val userRole: StateFlow<UserRole> = _userRole

    fun setSession(role: UserRole) {
        _userRole.value = role
    }
    
    fun logout() {
        _userRole.value = UserRole.NONE
        // Optional: Clear cart on logout
        clearCart()
    }

    fun saveProfile(name: String, address: String, printerMac: String, username: String, password: String, cashierPassword: String, isDarkMode: Boolean, logoUri: String? = null) {
        viewModelScope.launch {
            try {
                repository.saveProfile(
                    StoreProfile(
                        storeName = name,
                        storeAddress = address,
                        printerMacAddress = printerMac,
                        username = username,
                        password = password,
                        cashierPassword = cashierPassword,
                        logoUri = logoUri, // Preserve if pass null? Actually typically we pass the current one
                        isDarkMode = isDarkMode
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class ProductViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

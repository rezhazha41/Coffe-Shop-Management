package com.apps.coffeeshop.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.launch

@Database(entities = [Product::class, Transaction::class, StoreProfile::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun storeProfileDao(): StoreProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coffee_shop_database"
                )
                .addCallback(DatabaseCallback(context))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    val database = getDatabase(context)
                    populateDatabase(database.productDao(), database.storeProfileDao())
                }
            }
        }

        suspend fun populateDatabase(productDao: ProductDao, storeProfileDao: StoreProfileDao) {
            val initialProducts = listOf(
                Product(name = "Americano", price = 25000.0, category = "Kopi", imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBgwxDFJ-Sw0h5YJzSq2v3ah98tYgLR3QKtU4K1uvymWki-llD7ZjYXLWcqN_pGq0eAWOBD1LDUuyetplr3d8yDJDTwLcOgI3CWkFVI5dpjDloZfo3ZrpFw0d2r8IfK5aTP4bGWMhEW-p5uOAPbSY0j1TrCcfTUDcC899V_qJkxcbZFBuTXQSXdnQKFMlh_LAKqVqc_LYsK7q0Qfop3On3K3YqToMvSrHRPIj1QQ6Krab8t8IsI8hT1dPyH8NMW4e4hxP7RxY5u9ljD", stock = 100),
                Product(name = "Latte", price = 28000.0, category = "Kopi", imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAL8z3MK4mtXC6RcsZ_WPVLqifNv06yjsRm7pDvUKTWWri8wi7Iq8JcUVJloLyyFGw8CwCxtt8vLL_Z6oOfL_eYJXR6sBXYjGCof3GCJvYRyv5w07JweHIhEsqwIHLN1sg4iR6ATfnWtYV0VQt7lDOySxNXlAWPrZUT0L_PlL34U13H4YKsAB2PbwpuG6T97PPOsnq-f5HYxGSuiq70JXEHvnMqn9a99SfhISP6LhqLWtrgGmt8JnAh7hbz-Ctm1ill168fZpLVAM_9", stock = 100),
                Product(name = "Cappuccino", price = 28000.0, category = "Kopi", imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCCPVX5-EMwRL8wv-uWCaDUEa6_l9YFKp8gGZp_rP2U5GCrq0ANYIZU2n9QwyUpx6ShYL0q2ReG0v3Hwr7dkX6Qwj8Tc2_Ht-A8nLJo3XNjKW22vk6Vayx4mWF6StQPZdpbFslTM4MH_uy739xel8PkE9FW9cE0k5yrx41_b7PARi1dXdmMPvSSuIDJXxQXAVQ-f_TtkY-Zbc-Z8YtCge_rIm5375xPmL0gSULo3NgAZQd8Zlo3DnAu8Khge7e4vVFHblISgLCnBYV6", stock = 100),
                Product(name = "Matcha Latte", price = 30000.0, category = "Non-Kopi", imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBQ3u4HmSSzsoz50JhVbSTBElLU0wS1iJSYmvRNQDlVyzsKHG_QtRpxF3NzekGd_DF9t-EofYsIKsPdS5BtkxUlYq-YUM4T7aymK19HJD2H5aMWjf4lyQ7WY6mMzv49vCUy_Hbbhx6dzRPWTjuh0Fuzuq6boK4yGpNKWVuGSwlPEqm0iFwpBCK8qzRxSqpkX5ihYLvkh3NdlYMvUn7qnlmEYohlIQD24auHrm9XqSZna91MTEYtEp2FcrEad1ZCNtzGcRD3xGYeWe-U", stock = 50),
                Product(name = "Croissant", price = 22000.0, category = "Makanan", imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBMTY0MXf0FQwM8K55a6f0evdyGPeU8iuL_5onLs8cJRWW5PQlB_7J8m4p6xesjFiV9jbHIb8jC5iBo6BpvVEfPcAh6lxv3sdxZC2frgbHPmjWXi_M9F_Vdk14yns3wT58WFZ8mKDaLSBFZ3Ac_V8WAetZLmFjAK03AGc1F9EtT3g9r5LCI7k-975kJWiD9BDkfs-_lR0Be5G2Sq2raKKRZSYgdopWhhTNoMF1PuJqn4QQ5EElCp9AF-ZiRDGNjxUVYI5lY0-QZlEyN", stock = 30),
                Product(name = "Red Velvet Cake", price = 35000.0, category = "Makanan", imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDJQshoi3bo_sHjQLXauDqZ25cuHv9XjkTgXtJjvQhcPf0_uPj_fEUJ1QM9WemcpSmw9SwZLhrJIul48IJv_ZomXK50aCFqq4wEt_-EBHar1RmGgnTNeL7MJmSejTHoDGN2jU6noNPG05dwdZrooQqCkB0PDMACq0BZIlQu6MhGVLK5uxrwNxiXN4ERrhJ_6fzdRHk-FDGcK6OwYpbE3O43JD6hca3qpQB3qsBbFe9ho3TZp8jkz-75KofKUYYyv5W9Xn2X2_C7UkBw", stock = 20)
            )
            initialProducts.forEach { productDao.insertProduct(it) }
            
            // Seed Profile
            storeProfileDao.insertOrUpdateProfile(
                StoreProfile(
                    storeName = "My Coffee Shop", 
                    storeAddress = "Jl. Contoh No. 123", 
                    printerMacAddress = "",
                    username = "admin",
                    password = "admin123",
                    isDarkMode = false
                )
            )
        }
    }
}

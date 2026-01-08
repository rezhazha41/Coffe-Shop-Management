package com.apps.coffeeshop.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreProfileDao {
    @Query("SELECT * FROM store_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<StoreProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: StoreProfile)
}

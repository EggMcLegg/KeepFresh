package com.example.keepfresh.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDatabaseDao {
    @Insert
    suspend fun insertItem(foodItem: FoodItem)

    @Query("SELECT * FROM food_items_table")
    fun getAllItems(): Flow<List<FoodItem>>

    @Delete
    suspend fun delete(foodItem: FoodItem)
}
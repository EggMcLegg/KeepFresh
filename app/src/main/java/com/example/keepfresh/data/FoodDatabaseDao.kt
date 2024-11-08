package com.example.keepfresh.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDatabaseDao {
    @Insert
    suspend fun insertItem(foodItem: FoodItem)

    @Query("SELECT * FROM food_items_table")
    fun getAllItems(): Flow<List<FoodItem>>

    @Delete
    suspend fun delete(foodItem: FoodItem)

    @Query("""
        SELECT * FROM food_items_table 
        WHERE expiration_date  <= :daysLater 
        AND expiration_date  >= :today 
        AND (last_notified = 0 OR strftime('%Y-%m-%d', last_notified / 1000, 'unixepoch') != strftime('%Y-%m-%d', :today / 1000, 'unixepoch'))
    """)
    fun getItemsExpiringSoon(today: Long, daysLater: Long): LiveData<List<FoodItem>>

    // Update the item to mark that it has been notified
    @Update
    suspend fun updateItem(item: FoodItem)
}
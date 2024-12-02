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

    @Query(
        """
        SELECT * FROM food_items_table 
        WHERE expiration_date  <= :daysLater 
        AND expiration_date  >= :today 
        AND (last_notified = 0 OR strftime('%Y-%m-%d', last_notified / 1000, 'unixepoch') != strftime('%Y-%m-%d', :today / 1000, 'unixepoch'))
        AND (notification_option = 1)
    """
    )
    fun getItemsExpiringSoon(today: Long, daysLater: Long): LiveData<List<FoodItem>>

    @Query("SELECT * FROM food_items_table WHERE state = :state")
    fun getItemsByState(state: String): LiveData<List<FoodItem>>

    // Searchable Spinner
    @Query("SELECT * FROM food_items_table WHERE food_name LIKE '%' || :query || '%'")
    fun getFoodItemsByName(query: String): LiveData<List<FoodItem>>

    // Update the item to mark that it has been notified
    @Update
    suspend fun updateItem(item: FoodItem)

    @Query("SELECT * FROM food_items_table WHERE id = :id")
    fun getFoodItemById(id: Long): LiveData<FoodItem>

    @Query("DELETE FROM food_items_table WHERE id = :foodId")
    suspend fun deleteFoodItemById(foodId: Long)

    // Background Notifications
    @Query("SELECT * FROM food_items_table WHERE expiration_date BETWEEN :today AND :daysLater")
    suspend fun getExpiringItems(today: Long, daysLater: Long): List<FoodItem>
}
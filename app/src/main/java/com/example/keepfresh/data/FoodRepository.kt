package com.example.keepfresh.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FoodRepository (private val foodDatabaseDao: FoodDatabaseDao) {
    val allFoodItems: Flow<List<FoodItem>> = foodDatabaseDao.getAllItems()
    fun getFoodItemById(id: Long): LiveData<FoodItem> = foodDatabaseDao.getFoodItemById(id)

    fun insert(foodItem: FoodItem){
        CoroutineScope(IO).launch{
            foodDatabaseDao.insertItem(foodItem)
        }
    }

    fun deleteFoodItemById(foodId: Long){
        CoroutineScope(IO).launch {
            foodDatabaseDao.deleteFoodItemById(foodId)
        }
    }

    fun getExpiringItemsSoon(daysLater: Long): LiveData<List<FoodItem>> {
        val today = System.currentTimeMillis()
        return foodDatabaseDao.getItemsExpiringSoon(today, daysLater)
    }

    fun update(foodItem: FoodItem ) {
        CoroutineScope(IO).launch {
            foodDatabaseDao.updateItem(foodItem)
        }
    }
}
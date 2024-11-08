package com.example.keepfresh.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FoodRepository (private val foodDatabaseDao: FoodDatabaseDao) {
    val allFoodItems: Flow<List<FoodItem>> = foodDatabaseDao.getAllItems()

    fun insert(foodItem: FoodItem){
        CoroutineScope(IO).launch{
            foodDatabaseDao.insertItem(foodItem)
        }
    }

    fun delete(foodItem: FoodItem){
        CoroutineScope(IO).launch {
            foodDatabaseDao.delete(foodItem)
        }
    }

    fun getExpiringItemsSoon(daysLater: Long): LiveData<List<FoodItem>> {
        val today = System.currentTimeMillis()
        return foodDatabaseDao.getItemsExpiringSoon(today, daysLater)
    }
}
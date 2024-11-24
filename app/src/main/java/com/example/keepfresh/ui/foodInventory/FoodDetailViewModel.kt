package com.example.keepfresh.ui.foodInventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.keepfresh.data.FoodItem
import com.example.keepfresh.data.FoodRepository
import java.lang.IllegalArgumentException

class FoodDetailViewModel(private val repository: FoodRepository) : ViewModel() {
    fun getFoodItemById(id: Long): LiveData<FoodItem> = repository.getFoodItemById(id)

    fun deleteFoodItemById(foodId: Long){
        repository.deleteFoodItemById(foodId)
    }

    fun updateFoodItem(foodItem: FoodItem){
        repository.update(foodItem)
    }
}

class FoodDetailViewModelFactory(private val repository: FoodRepository): ViewModelProvider.Factory{
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{ //create() creates a new instance of the modelClass
        if(modelClass.isAssignableFrom(FoodDetailViewModel::class.java))
            return FoodDetailViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
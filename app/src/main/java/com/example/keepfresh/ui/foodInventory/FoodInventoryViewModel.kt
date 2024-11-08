package com.example.keepfresh.ui.foodInventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.keepfresh.data.FoodItem
import com.example.keepfresh.data.FoodRepository
import com.example.keepfresh.ui.foodInput.FoodInputViewModel
import java.lang.IllegalArgumentException

class FoodInventoryViewModel(private val repository: FoodRepository) : ViewModel() {
    val allFoodItems: LiveData<List<FoodItem>> = repository.allFoodItems.asLiveData()
}

class FoodInventoryViewModelFactory(private val repository: FoodRepository): ViewModelProvider.Factory{
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{ //create() creates a new instance of the modelClass
        if(modelClass.isAssignableFrom(FoodInventoryViewModel::class.java))
            return FoodInventoryViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



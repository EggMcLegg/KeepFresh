package com.example.keepfresh.ui.foodInput

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.keepfresh.data.FoodItem
import com.example.keepfresh.data.FoodRepository
import java.lang.IllegalArgumentException

class FoodInputViewModel(private val repository: FoodRepository) : ViewModel() {
    fun insert(foodItem: FoodItem){
        repository.insert(foodItem)
    }
}

class FoodInputViewModelFactory(private val repository: FoodRepository): ViewModelProvider.Factory{
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{ //create() creates a new instance of the modelClass
        if(modelClass.isAssignableFrom(FoodInputViewModel::class.java))
            return FoodInputViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.example.keepfresh.ui.foodInput

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.keepfresh.data.FoodItem
import com.example.keepfresh.data.FoodRepository
import java.lang.IllegalArgumentException

class FoodInputViewModel(private val repository: FoodRepository) : ViewModel() {
    private val _scannedFoodItem = MutableLiveData<FoodItem?>()
    val scannedFoodItem: LiveData<FoodItem?> get() = _scannedFoodItem

    fun insert(foodItem: FoodItem){
        repository.insert(foodItem)
    }
    fun fetchFoodDetailsFromBarcode(barcode: String) {
       repository.fetchFoodDetailsFromBarcode(barcode) { foodItem ->
           _scannedFoodItem.postValue(foodItem)
       }
    }
}

class FoodInputViewModelFactory(private val repository: FoodRepository): ViewModelProvider.Factory{
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{ //create() creates a new instance of the modelClass
        if(modelClass.isAssignableFrom(FoodInputViewModel::class.java))
            return FoodInputViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
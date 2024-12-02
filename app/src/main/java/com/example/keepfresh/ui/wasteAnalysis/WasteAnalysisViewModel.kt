package com.example.keepfresh.ui.wasteAnalysis

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.keepfresh.data.FoodRepository
import com.example.keepfresh.data.FoodState
import kotlinx.coroutines.flow.map

data class FoodDetail(
    val name: String,
    val cost: Double
)

data class WasteData(
    val month: String,
    val foodNames: List<String>, // Names of wasted food items
    val foodDetails: List<FoodDetail>, // List of detailed wasted items
    val totalFoodWasted: Int, // Total number of wasted food items
    val totalMoneyWasted: Double, // Total cost of wasted food
    val mostExpensiveWaste: FoodDetail? // Item with the highest monetary waste
)

class WasteAnalysisViewModel(private val repository: FoodRepository) : ViewModel() {
    val wasteAnalysis: LiveData<List<WasteData>> = repository.allFoodItems
    .map { items ->
        items.groupBy { item ->
            val date = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
            date.format(item.getExpirationDate())
        }.map { (month, items) ->
            val expiredItems = items.filter { it.getState() == FoodState.EXPIRED }
            val foodDetails = expiredItems.map { FoodDetail(it.getFoodName(), it.getCost()) }
            val mostExpensiveWaste = foodDetails.maxByOrNull { it.cost } // Find the most expensive waste

            WasteData(
                month = month,
                foodNames = expiredItems.map { it.getFoodName() },
                foodDetails = foodDetails,
                totalFoodWasted = expiredItems.size,
                totalMoneyWasted = expiredItems.sumOf { it.getCost() },
                mostExpensiveWaste = mostExpensiveWaste
            )
        }
    }.asLiveData()
}

class WasteAnalysisViewModelFactory(private val repository: FoodRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WasteAnalysisViewModel::class.java)) {
            return WasteAnalysisViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

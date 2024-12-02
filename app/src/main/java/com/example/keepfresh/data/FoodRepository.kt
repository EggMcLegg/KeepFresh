package com.example.keepfresh.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class FoodRepository (private val foodDatabaseDao: FoodDatabaseDao) {

    companion object {
        private const val BASE_URL = "https://world.openfoodfacts.org/api/v2/product/"
    }

    val allFoodItems: Flow<List<FoodItem>> = foodDatabaseDao.getAllItems()

    fun getFoodItemById(id: Long): LiveData<FoodItem> = foodDatabaseDao.getFoodItemById(id)

    fun insert(foodItem: FoodItem) {
        CoroutineScope(IO).launch {
            foodDatabaseDao.insertItem(foodItem)
        }
    }

    fun deleteFoodItemById(foodId: Long) {
        CoroutineScope(IO).launch {
            foodDatabaseDao.deleteFoodItemById(foodId)
        }
    }

    fun getExpiringItemsSoon(daysLater: Long): LiveData<List<FoodItem>> {
        val today = System.currentTimeMillis()
        return foodDatabaseDao.getItemsExpiringSoon(today, daysLater)
    }

    fun update(foodItem: FoodItem) {
        CoroutineScope(IO).launch {
            foodDatabaseDao.updateItem(foodItem)
        }
    }

    fun getItemsByState(state: String): LiveData<List<FoodItem>> {
        return foodDatabaseDao.getItemsByState(state)
    }

    fun getFoodItemsByName(query: String): LiveData<List<FoodItem>> {
        return foodDatabaseDao.getFoodItemsByName(query)
    }

    // Background Notification
    suspend fun getExpiringItems(today: Long, daysLater: Long): List<FoodItem> {
        return foodDatabaseDao.getExpiringItems(today, daysLater)
    }

    fun fetchFoodDetailsFromBarcode(barcode: String, onResult: (FoodItem?) -> Unit) {
        CoroutineScope(IO).launch {
            val apiURL = "$BASE_URL$barcode.json"
            val url = URL(apiURL)
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.connect()
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val jsonResponse = connection.inputStream.bufferedReader().use { it.readText() }
                    val productJson = JSONObject(jsonResponse).optJSONObject("product")

                    if(productJson != null) {
                        val name = productJson.optString("product_name", "Unknown")
                        val imageURL = productJson.optString("image_url","")
                        val expirationDate = System.currentTimeMillis() +  + 7 * 24 * 60 * 60 * 1000    // Default 7 days

                        val foodItem = FoodItem(
                            foodName = name,
                            expirationDate = expirationDate,
                            foodPhotoUri = imageURL
                        )
                        onResult(foodItem)
                    } else {
                        onResult(null)
                    }
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            } finally {
                connection.disconnect()
            }
        }
    }
    fun markExpiredFoodItems(){
        CoroutineScope(IO).launch {
            foodDatabaseDao.getAllItems().collect { allItems ->
                val currentTime = System.currentTimeMillis()
                for (item in allItems) {
                    if (item.getExpirationDate() < currentTime && item.getState() != FoodState.USED && item.getState() != FoodState.EXPIRED) {
                        item.setState(FoodState.EXPIRED)
                        foodDatabaseDao.updateItem(item)
                    }
                }
            }
        }
    }
}

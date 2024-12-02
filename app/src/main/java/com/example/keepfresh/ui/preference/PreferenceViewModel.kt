package com.example.keepfresh.ui.preference

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import com.example.keepfresh.data.FoodDatabase
import com.example.keepfresh.data.FoodRepository
import com.example.keepfresh.data.FoodItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*

class PreferenceViewModel(application: android.app.Application) : AndroidViewModel(application) {

    private val foodRepository: FoodRepository
    var expiringItems: LiveData<List<FoodItem>> = MutableLiveData()

    init {
        val foodDao = FoodDatabase.getInstance(application).foodDatabaseDao
        foodRepository = FoodRepository(foodDao)
    }

    fun searchFoodItems(query: String): LiveData<List<FoodItem>> {
        return foodRepository.getFoodItemsByName(query)
    }

    fun updateFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            foodRepository.update(foodItem)
        }
    }

    fun fetchExpiringItems(daysLater: Long) {
        expiringItems = foodRepository.getExpiringItemsSoon(daysLater)
    }

    fun getFoodNames(): Flow<List<FoodItem>> {
        return foodRepository.allFoodItems
    }

    /**
// Method to send notifications for items expiring soon
    fun sendNotification(context: Context, items: List<FoodItem>) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val vibrationPattern = longArrayOf(0, 500, 500)
        val handler = Handler(Looper.getMainLooper())

        for ((index, item) in items.withIndex()) {
            handler.postDelayed({
                Log.d("sendNotification", "Sending notification for ${item.getFoodName()}")

                val notification = NotificationCompat.Builder(context, "food_expiry_id")
                    .setContentTitle("Food Expiration Alert")
                    .setContentText("${item.getFoodName()} is expiring soon!")
                    .setSmallIcon(android.R.drawable.ic_notification_overlay)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setVibrate(vibrationPattern)
                    .setAutoCancel(true)
                    .build()

                // Notify the user with the given ID (use item ID to ensure it's unique)
                notificationManager.notify(item.getId().toInt(), notification)
            }, index * 10000L)  // Spacing out notifications by 10 seconds
        }
    } **/
}

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.keepfresh.data.FoodDatabase
import com.example.keepfresh.data.FoodDatabaseDao
import com.example.keepfresh.data.FoodItem
import com.example.keepfresh.data.FoodRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background Notifications: This entire file can be deleted if necessary.
 */

class WorkerNotification(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    private val foodDatabaseDao: FoodDatabaseDao = FoodDatabase.getInstance(appContext.applicationContext).foodDatabaseDao

    override suspend fun doWork(): Result {
        val sharedPreferences = applicationContext.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val daysLaterValue = sharedPreferences.getInt("selectedDays", 1) // Default to 1 days if not set
        val today = System.currentTimeMillis()
        val daysLater = today + (daysLaterValue * 24 * 60 * 60 * 1000)

        return try {
            sendExpirationNotifications(today, daysLater)
            Result.success()
        } catch (e: Exception) {
            Log.e("WorkerNotification", "Error in sending notifications", e)
            Result.failure()
        }
    }

    // Function to call suspend function in a coroutine context
    private suspend fun getExpiringItems(today: Long, daysLater: Long): List<FoodItem> {
        return withContext(Dispatchers.IO) {
            foodDatabaseDao.getExpiringItems(today, daysLater)
        }
    }

    private suspend fun sendExpirationNotifications(today: Long, daysLater: Long) {
        createNotificationChannel(applicationContext)

        val foodRepository = FoodRepository(FoodDatabase.getInstance(applicationContext).foodDatabaseDao) // Replace with your repository instance
        val items = foodRepository.getExpiringItems(today, daysLater)
        val eligibleItems = items.filter { it.getNotificationOption() }

        if (eligibleItems.isNotEmpty()) {
            Log.d("WorkerNotification", "Eligible items: ${eligibleItems.size}")
            sendNotification(applicationContext, eligibleItems)
        } else {
            Log.d("WorkerNotification", "No items with notifications enabled are expiring.")
        }
    }


    private fun sendNotification(context: Context, items: List<FoodItem>) {
        Log.d("sendNotification", "sendNotification executing")
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "food_expiry_id"
        val vibrationPattern = longArrayOf(0, 500, 500)

        val handler = Handler(Looper.getMainLooper())

        for ((index, item) in items.withIndex()) {
            handler.postDelayed({
                Log.d("sendNotification", "Sending notification for ${item.getFoodName()}")

                // Create the notification
                val notification = NotificationCompat.Builder(context, channelId)
                    .setContentTitle("Food Expiration Alert")
                    .setContentText("${item.getFoodName()} is expiring soon!")
                    .setSmallIcon(android.R.drawable.ic_notification_overlay)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setVibrate(vibrationPattern)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(item.getId().toInt(), notification)
            }, index * 2500L)
        }

        Log.d("sendNotification", "Notifications sent for ${items.size} items.")
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "food_expiry_id"
            val channelName = "Food Expiration Alerts"
            val channelDescription = "Notifications for food expiration alerts"

            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = channelDescription

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d("NotificationChannel", "Notification Channel created with ID: $channelId")
        }
    }

}



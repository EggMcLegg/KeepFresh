package com.example.keepfresh.ui.preference

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.keepfresh.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Create a NotificationManager instance
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification
        val notification = NotificationCompat.Builder(context, "notification_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Set your notification icon
            .setContentTitle("Keep Fresh")
            .setContentText("This is your reminder notification!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Show the notification
        notificationManager.notify(1, notification)
    }
}

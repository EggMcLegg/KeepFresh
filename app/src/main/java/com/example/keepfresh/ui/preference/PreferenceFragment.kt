package com.example.keepfresh.ui.preference

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.keepfresh.databinding.FragmentPreferenceBinding
import com.example.keepfresh.data.FoodItem
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.AdapterView
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

import androidx.core.content.ContextCompat

class PreferenceFragment : Fragment() {

    private var _binding: FragmentPreferenceBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationSwitch: SwitchCompat
    private lateinit var daysSpinner: Spinner
    private val days = arrayOf("1 day before expiry", "2 days before expiry", "3 days before expiry",
        "4 days before expiry", "5 days before expiry", "6 days before expiry", "7 days before expiry")
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var scheduleButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(PreferenceViewModel::class.java)

        _binding = FragmentPreferenceBinding.inflate(inflater, container, false)
        val root: View = binding.root

        notificationSwitch = binding.notificationSwitch
        daysSpinner = binding.daysSpinner
        scheduleButton = binding.scheduleNotificationsButton
        sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, days)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        daysSpinner.adapter = adapter

        val savedDays = sharedPreferences.getInt("selectedDays", 1) // Default is 1 day
        val savedPosition = savedDays - 1
        if (savedPosition >= 0) {
            daysSpinner.setSelection(savedPosition)
        }

        val isNotificationsEnabled = sharedPreferences.getBoolean("switchState", false)
        notificationSwitch.isChecked = isNotificationsEnabled
        daysSpinner.isEnabled = isNotificationsEnabled


        daysSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedDays = days[position].split(" ")[0].toInt()
                // Save the selected value to SharedPreferences
                sharedPreferences.edit().putInt("selectedDays", selectedDays).apply()
                Log.d("PreferenceFragment", "Selected days: $selectedDays")
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Handle case when no item is selected (this usually doesn't happen for spinners)
            }
        }
        // Toggle notifications
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save the switch state to SharedPreferences
            sharedPreferences.edit().putBoolean("switchState", isChecked).apply()

            // Enable/Disable the spinner based on switch state
            daysSpinner.isEnabled = isChecked

            if (isChecked) {
                // Notifications enabled, spinner is enabled
                Log.d("PreferenceFragment", "Notifications enabled")
            } else {
                // Notifications disabled, spinner is disabled
                Log.d("PreferenceFragment", "Notifications disabled")
                // Clear the selected days preference when notifications are off
                sharedPreferences.edit().remove("selectedDays").apply()
            }
        }

        scheduleButton.setOnClickListener {
            val selectedDays = sharedPreferences.getInt("selectedDays",1)
            val today = System.currentTimeMillis()
            val daysLater = today + (selectedDays * 24 * 60 * 60 * 1000)
            scheduleNotifications(daysLater)
        }

        return root
    }



    /** Redundant Code
     * private fun loadPreferences() {
        val savedDays = sharedPreferences.getInt("selectedDays", 1) // Default is 1 day
        val savedSwitchState = sharedPreferences.getBoolean("switchState", false) // Default is off

        Log.d("PreferenceFragment", "Loaded switch state: $savedSwitchState")
        Log.d("PreferenceFragment", "Loaded selected days: $savedDays")

        daysSpinner.setSelection(savedDays - 1)

        notificationSwitch = binding.notificationSwitch
        notificationSwitch.isChecked = savedSwitchState

        daysSpinner.isEnabled = savedSwitchState
    }

    private fun savePreferences(switchState: Boolean, daysBefore: Int) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("switchState", switchState)
        editor.putInt("selectedDays", daysBefore)
        editor.apply()

        Log.d("PreferenceFragment", "Saved switch state: $switchState")
        Log.d("PreferenceFragment", "Saved selected days: $daysBefore")
    }
    **/
    private fun scheduleNotifications(daysLater: Long) {
        //val today = System.currentTimeMillis()
        //val daysLater = today + (daysBefore * 24 * 60 * 60 * 1000)  // In milliseconds
        Log.d("PreferenceFragment", "Scheduling notifications for items expiring between today and $daysLater")
        fetchExpiringItems(daysLater)
    }

    private fun fetchExpiringItems(daysLater: Long) {
        createNotificationChannel(requireContext())

        val viewModel = ViewModelProvider(this).get(PreferenceViewModel::class.java)
        viewModel.fetchExpiringItems(daysLater)
        viewModel.expiringItems.observe(viewLifecycleOwner, Observer { items ->
            if (items != null && items.isNotEmpty()) {
                Log.d("PreferenceFragment", "Fetched ${items.size} expiring items")
                sendNotification(requireContext(), items)
            } else {
                Log.d("PreferenceFragment", "No items are expiring within the selected time range")
            }
        })
    }

    private fun sendNotification(context: Context, items: List<FoodItem>) {
        Log.d("sendNotification", "sendNotification executing")
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "food_expiry_id"

        val vibrationPattern = longArrayOf(0, 500, 500)

        val handler = Handler(Looper.getMainLooper())

        for ((index, item) in items.withIndex()) {
            // Add a delay for each notification by 3 seconds
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
            }, index * 2500L)  // 3 second delays
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

    private fun cancelNotifications() {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()  // Cancel all active notifications
        Log.d("PreferenceFragment", "All notifications cancelled")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



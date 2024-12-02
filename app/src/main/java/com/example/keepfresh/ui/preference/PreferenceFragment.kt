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
import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * An attempt was made to implement a Searchable Spinner, but it was too buggy.
 * Searches successfully filtered the spinner, but the toggleSwitch would not update properly.
 * The commented-out code is/was for the Searchable Spinner, which is not natively supported by Android
 * As a last resort due to deadlines, a simple Spinner had to suffice. -Sasha
 */

class PreferenceFragment : Fragment() {

    private var _binding: FragmentPreferenceBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationSwitch: SwitchCompat
    private lateinit var daysSpinner: Spinner
    private val days = arrayOf("1 day before expiry", "2 days before expiry", "3 days before expiry",
        "4 days before expiry", "5 days before expiry", "6 days before expiry", "7 days before expiry")
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var scheduleButton: Button
    private lateinit var foodItemsSpinner: Spinner
    private lateinit var toggleSwitch: SwitchCompat

    private lateinit var foodItemsAdapter: ArrayAdapter<String>

    private var foodItemsList: MutableList<FoodItem> = mutableListOf()

    // Searchable Spinner
    //private lateinit var searchBar: EditText
    //private var filteredFoodItems: MutableList<FoodItem> = mutableListOf()

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
        //searchBar = binding.searchBar
        foodItemsSpinner = binding.foodItemsSpinner
        toggleSwitch = binding.toggleSwitch
        sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        daysSpinner.isEnabled = sharedPreferences.getBoolean("switchState", true)
        foodItemsSpinner.isEnabled

        notificationSwitch.isChecked = sharedPreferences.getBoolean("switchState", true)
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("switchState", isChecked).apply()
            daysSpinner.isEnabled = isChecked
            foodItemsSpinner.isEnabled = isChecked
            //searchBar.isEnabled = isChecked
            toggleSwitch.isEnabled = isChecked

            if (isChecked) {
                Log.d("PreferenceFragment", "Notifications enabled")
            } else {
                Log.d("PreferenceFragment", "All notifications disabled")
                sharedPreferences.edit().remove("selectedDays").apply()
            }
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, days)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        daysSpinner.adapter = adapter

        val savedDays = sharedPreferences.getInt("selectedDays", 1) // Default is 1 day
        val savedPosition = savedDays - 1
        if (savedPosition >= 0) {
            daysSpinner.setSelection(savedPosition)
        }

        foodItemsAdapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            mutableListOf<String>()
        )
        foodItemsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        foodItemsSpinner.adapter = foodItemsAdapter

        // Searchable Spinner
        /**
        galleryViewModel.fetchExpiringItems(System.currentTimeMillis() + (savedDays * 24 * 60 * 60 * 1000))
        galleryViewModel.expiringItems.observe(viewLifecycleOwner) { items ->
            foodItemsList = items.toMutableList()
            Log.d("foodItemsSpinner", "foodItemList size: ${foodItemsList.size}")
            //resetFoodItemsList()
        }
        **/
        lifecycleScope.launch {
            galleryViewModel.getFoodNames().collect { foodItems ->
                // Update the list with the new data
                foodItemsList = foodItems.toMutableList()
                resetFoodItemsList()
            }
        }

        // Searchable Spinner
        /**
        searchBar.addTextChangedListener { text ->
            val query = text.toString().trim()

            if (query.isNotEmpty()) {
                Log.d("foodItemsSpinner", "textChanged: calling query")
                filterFoodItems(query)
            } else {
                Log.d("foodItemsSpinner", "textChanged: Calling resetFoodItemsList")
                resetFoodItemsList()
            }
        }
        **/

        toggleSwitch.visibility = View.GONE


        foodItemsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.d("foodItemsSpinner", "Item selected: position = $position, id = $id")
                if (position != -1 && foodItemsList.isNotEmpty()) {
                    val selectedFoodItem = foodItemsList[position]
                    toggleSwitch.visibility = View.VISIBLE
                    //searchBar.setText(selectedFoodItem.getFoodName())
                    val isNotifEnabled = selectedFoodItem.getNotificationOption()
                    Log.d("foodItemsSpinner", "Selected item: ${selectedFoodItem.getFoodName()}, NotifEnabled: $isNotifEnabled")
                    toggleSwitch.isChecked = isNotifEnabled

                } else {
                    Log.d("foodItemsSpinner", "no item selected, toggleSwitch hidden")
                    Log.d("foodItemsSpinner", "position:${position}, foodItemsList:${foodItemsList.size}")
                    toggleSwitch.visibility = View.GONE
                    //searchBar.text.clear()
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                Log.d("foodItemsSpinner", "OnNothingSelected()")
                toggleSwitch.visibility = View.GONE
                //searchBar.text.clear()
            }
        }

        toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            val selectedIndex = foodItemsSpinner.selectedItemPosition
            Log.d("foodItemsSpinner", "selectedIndex: ${selectedIndex} isChecked: ${isChecked}")
            if (selectedIndex != -1 && foodItemsList.isNotEmpty()) {
                val selectedFoodItem = foodItemsList[selectedIndex]
                selectedFoodItem.setNotificationOption(isChecked)
                val message = if (isChecked) {
                    "${selectedFoodItem.getFoodName()}: Notifications enabled"
                } else {
                    "${selectedFoodItem.getFoodName()}: Notifications disabled"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                galleryViewModel.updateFoodItem(selectedFoodItem)
            }
        }

        daysSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedDays = days[position].split(" ")[0].toInt()
                // Save the selected value to SharedPreferences
                sharedPreferences.edit().putInt("selectedDays", selectedDays).apply()
                Log.d("PreferenceFragment", "Selected days: $selectedDays")
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
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

    private fun resetFoodItemsList() {
        foodItemsAdapter.clear()
        foodItemsAdapter.addAll(foodItemsList.map { it.getFoodName() }) // Update adapter with food item names
        foodItemsAdapter.notifyDataSetChanged()
        foodItemsSpinner.setSelection(-1)
    }

    // Searchable Spinner
    /**
    private fun filterFoodItems(query: String) {
        Log.d("foodItemsSpinner", "filterFoodItemsCalled")
        val viewModel = ViewModelProvider(this).get(PreferenceViewModel::class.java)

        viewModel.searchFoodItems(query).observe(viewLifecycleOwner) { items ->
            filteredFoodItems = items.toMutableList()
            foodItemsAdapter.clear()
            foodItemsAdapter.addAll(filteredFoodItems.map { it.getFoodName() })
            foodItemsAdapter.notifyDataSetChanged()
            foodItemsSpinner.setSelection(-1)
        }
    }

    // Searchable Spinner
    private fun resetFoodItemsList() {
        Log.d("foodItemsSpinner", "resetFoodItems called")
        foodItemsAdapter.clear()
        foodItemsAdapter.addAll(foodItemsList.map { it.getFoodName() })  // Show all food items again
        foodItemsAdapter.notifyDataSetChanged()
        foodItemsSpinner.setSelection(-1)
    }
    **/

    private fun scheduleNotifications(daysLater: Long) {
        Log.d("PreferenceFragment", "Scheduling notifications for items expiring between today and $daysLater")
        fetchExpiringItems(daysLater)
    }

    private fun fetchExpiringItems(daysLater: Long) {
        createNotificationChannel(requireContext())

        val viewModel = ViewModelProvider(this).get(PreferenceViewModel::class.java)
        viewModel.fetchExpiringItems(daysLater)
        viewModel.expiringItems.observe(viewLifecycleOwner) { items ->
            val eligibleItems = items.filter { it.getNotificationOption() }
            if (eligibleItems.isNotEmpty()) {
                Log.d("PreferenceFragment", "eligibleItems: ${eligibleItems.size}")
                sendNotification(requireContext(), eligibleItems)
            } else {
                Log.d("PreferenceFragment", "No items with notifications enabled are expiring.")
            }
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

    // Searchable Spinner
/**
    private fun toggleNotification(foodItem: FoodItem) {
        val viewModel = ViewModelProvider(this).get(PreferenceViewModel::class.java)
        val newNotificationState = !foodItem.getNotificationOption()
        foodItem.setNotificationOption(newNotificationState)
        viewModel.updateFoodItem(foodItem)
        val message = if (newNotificationState) {
            "${foodItem.getFoodName()} notifications enabled!"
        } else {
            "${foodItem.getFoodName()} notifications disabled!"
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun cancelNotifications() {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()  // Cancel all active notifications
        Log.d("PreferenceFragment", "All notifications cancelled")
    }
    **/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



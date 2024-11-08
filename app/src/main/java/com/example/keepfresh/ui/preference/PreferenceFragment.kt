package com.example.keepfresh.ui.preference

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.keepfresh.databinding.FragmentPreferenceBinding
import java.util.*

class PreferenceFragment : Fragment() {
    private var _binding: FragmentPreferenceBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationSwitch: SwitchCompat
    private lateinit var daysSpinner: Spinner
    private val days = arrayOf(1, 2, 3, 4, 5, 6, 7) // Array for the spinner options

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel = ViewModelProvider(this).get(PreferenceViewModel::class.java)

        _binding = FragmentPreferenceBinding.inflate(inflater, container, false)
        val root: View = binding.root

        notificationSwitch = binding.notificationSwitch
        daysSpinner = binding.daysSpinner

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, days)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        daysSpinner.adapter = adapter

        if (!notificationSwitch.isChecked) {
            daysSpinner.visibility = View.GONE
        }

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                daysSpinner.visibility = View.VISIBLE
                val daysBefore = daysSpinner.selectedItem as Int
                scheduleNotification(daysBefore)
                Toast.makeText(context, "Notifications Enabled", Toast.LENGTH_SHORT).show()
            } else {
                daysSpinner.visibility = View.GONE
                cancelNotification()
                Toast.makeText(context, "Notifications Disabled", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    private fun scheduleNotification(daysBefore: Int) {
        val targetTime = Calendar.getInstance()
        targetTime.add(Calendar.DAY_OF_YEAR, daysBefore)

        targetTime.set(Calendar.HOUR_OF_DAY, 10)   // 10 AM
        targetTime.set(Calendar.MINUTE, 0)         // 0 minutes
        targetTime.set(Calendar.SECOND, 0)         // 0 seconds

        val intent = Intent(context, NotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Schedule the alarm (notification will trigger at 10 AM on the target time)
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,             // Wake up the device if it's asleep
            targetTime.timeInMillis,            // Set the time to the target time
            pendingIntent                      // Trigger the PendingIntent at the target time
        )

        Toast.makeText(context, "Notification scheduled for $daysBefore days at 10 AM", Toast.LENGTH_SHORT).show()
    }

    private fun cancelNotification() {
        // Cancel the scheduled alarm if the switch is off
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        Toast.makeText(context, "Notification cancelled", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

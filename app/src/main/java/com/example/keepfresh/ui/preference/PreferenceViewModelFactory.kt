package com.example.keepfresh.ui.preference

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.keepfresh.data.FoodRepository

class PreferenceViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    // This function creates and returns an instance of the ViewModel
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreferenceViewModel::class.java)) {
            // Create the ViewModel and return it
            return PreferenceViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

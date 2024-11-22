package com.example.keepfresh.ui.foodInput

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.keepfresh.Util
import com.example.keepfresh.Util.ImagePickerHelper
import com.example.keepfresh.Util.ImagePickerHelper.ImageSource
import com.example.keepfresh.databinding.FragmentInputBinding
import com.example.keepfresh.data.FoodDatabase
import com.example.keepfresh.data.FoodDatabaseDao
import com.example.keepfresh.data.FoodItem
import com.example.keepfresh.data.FoodRepository
import java.util.Calendar

class FoodInputFragment : Fragment(), FoodPhotoDialogFragment.FoodPhotoListener, DatePickerDialog.OnDateSetListener {

    private var _binding: FragmentInputBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FoodDatabase
    private lateinit var databaseDao: FoodDatabaseDao
    private lateinit var repository: FoodRepository
    private lateinit var viewModelFactory: FoodInputViewModelFactory
    private lateinit var foodInputViewModel: FoodInputViewModel

    private lateinit var photoResultLauncher: ActivityResultLauncher<Intent>
    private val calendar = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        database = FoodDatabase.getInstance(requireContext())
        databaseDao = database.foodDatabaseDao
        repository = FoodRepository(databaseDao)
        viewModelFactory = FoodInputViewModelFactory(repository)
        foodInputViewModel = ViewModelProvider(this, viewModelFactory).get(FoodInputViewModel::class.java)

        _binding = FragmentInputBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupUI()
        setupPhotoResultLauncher()

        return root
    }

    private fun setupUI() {
        binding.btnChangePhoto.setOnClickListener {
            Util.checkPermissions(requireActivity())
            val dialog = FoodPhotoDialogFragment()
            dialog.setListener(this)
            dialog.show(childFragmentManager, "FoodPhotoDialogFragment")
        }

        binding.inputExpirationDate.setOnClickListener {
            Util.showDatePicker(requireContext(), binding.inputExpirationDate, calendar)
        }

        binding.btnSave.setOnClickListener {
            saveFoodItem()
        }

        binding.btnCancel.setOnClickListener {
            clearInputs()
        }
    }

    private fun saveFoodItem() {
        val foodName = binding.inputFoodName.text.toString()
        val expirationDate = calendar.timeInMillis
        val photoUri = ImagePickerHelper.getPhotoUri()

        if (foodName.isNotBlank() && photoUri != null) {
            val foodItem = FoodItem(
                foodName = foodName,
                expirationDate = expirationDate,
                foodPhotoUri = photoUri.toString()
            )
            foodInputViewModel.insert(foodItem)
            Toast.makeText(requireContext(), "Food item saved!", Toast.LENGTH_SHORT).show()
            clearInputs()
        } else {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearInputs() {
        binding.inputFoodName.text?.clear()
        binding.inputExpirationDate.text?.clear()
        binding.photoFood.setImageURI(null)
        ImagePickerHelper.getPhotoUri()?.let { Uri.EMPTY }
    }

    private fun setupPhotoResultLauncher() {
        photoResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                binding.photoFood.setImageURI(ImagePickerHelper.getPhotoUri())
            } else {
                Toast.makeText(requireContext(), "Failed to select or capture image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCameraSelected() {
        ImagePickerHelper.launchImageSelection(requireContext(), ImageSource.CAMERA, photoResultLauncher)
    }

    override fun onGallerySelected() {
        ImagePickerHelper.launchImageSelection(requireContext(), ImageSource.GALLERY, photoResultLauncher)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        val selectedDate = "${year}/${month + 1}/$dayOfMonth"
        binding.inputExpirationDate.setText(selectedDate)
        calendar.set(year, month, dayOfMonth)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

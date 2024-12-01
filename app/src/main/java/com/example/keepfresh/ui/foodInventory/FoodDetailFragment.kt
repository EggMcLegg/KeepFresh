package com.example.keepfresh.ui.foodInventory

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.keepfresh.R
import com.example.keepfresh.Util
import com.example.keepfresh.Util.formatDate
import com.example.keepfresh.data.FoodDatabase
import com.example.keepfresh.data.FoodDatabaseDao
import com.example.keepfresh.data.FoodItem
import com.example.keepfresh.data.FoodRepository
import com.example.keepfresh.databinding.FragmentDetailFoodBinding
import com.example.keepfresh.ui.foodInput.FoodPhotoDialogFragment
import com.squareup.picasso.Picasso
import java.util.Calendar
import kotlin.math.cos

class FoodDetailFragment: Fragment() {
    private var _binding: FragmentDetailFoodBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FoodDatabase
    private lateinit var databaseDao: FoodDatabaseDao
    private lateinit var repository: FoodRepository
    private lateinit var viewModelFactory: FoodDetailViewModelFactory
    private lateinit var foodDetailViewModel: FoodDetailViewModel

    private var foodId: Long = 0L
    private val calendar = Calendar.getInstance()
    private lateinit var photoResultLauncher: ActivityResultLauncher<Intent>
    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        foodId = arguments?.getLong("foodId") ?: 0L
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        database = FoodDatabase.getInstance(requireContext())
        databaseDao = database.foodDatabaseDao
        repository = FoodRepository(databaseDao)
        viewModelFactory = FoodDetailViewModelFactory(repository)
        foodDetailViewModel = ViewModelProvider(this, viewModelFactory).get(FoodDetailViewModel::class.java)

        _binding = FragmentDetailFoodBinding.inflate(inflater, container, false)
        val root: View = binding.root
        showFoodItemDetails()
        setupPhotoResultLauncher()
        buttonClickListener()
        return root
    }

    private fun showFoodItemDetails(){
        foodDetailViewModel.getFoodItemById(foodId).observe(viewLifecycleOwner) { foodItem ->
            foodItem?.let{
                photoUri = Uri.parse(it.getFoodPhotoUri())
                if (photoUri != null) {
                    Picasso.get()
                        .load(photoUri.toString())
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_error)
                        .into(binding.photoFood)
                } else {
                    binding.photoFood.setImageResource(R.drawable.ic_placeholder) // Default placeholder
                }
                binding.foodNameInput.setText(foodItem.getFoodName())
                binding.expirationDateInput.setText(formatDate(foodItem.getExpirationDate()))
                binding.costInput.setText(Util.formatPrice(foodItem.getCost()))
            }
        }
    }

    private fun buttonClickListener(){
        binding.btnChangePhoto.setOnClickListener {
            updateFoodPhoto()
        }

        binding.expirationDateInput.setOnClickListener {
            Util.showDatePicker(requireContext(), binding.expirationDateInput, calendar)
        }

        binding.btnSave.setOnClickListener {
            saveUpdateFoodDetails()
        }

        binding.btnDelete.setOnClickListener {
            deleteFoodItem(foodId)
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupPhotoResultLauncher() {
        photoResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoUri = Util.ImagePickerHelper.getPhotoUri()
                binding.photoFood.setImageURI(photoUri)
            } else {
                Toast.makeText(requireContext(), "Failed to change photo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFoodPhoto(){
        Util.checkPermissions(requireActivity())
        val dialog = FoodPhotoDialogFragment()
        dialog.setListener(object : FoodPhotoDialogFragment.FoodPhotoListener {
            override fun onCameraSelected() {
                Util.ImagePickerHelper.launchImageSelection(
                    requireContext(),
                    Util.ImagePickerHelper.ImageSource.CAMERA,
                    photoResultLauncher
                )
            }

            override fun onGallerySelected() {
                Util.ImagePickerHelper.launchImageSelection(
                    requireContext(),
                    Util.ImagePickerHelper.ImageSource.GALLERY,
                    photoResultLauncher
                )
            }
        })
        dialog.show(childFragmentManager, "FoodPhotoDialogFragment")
    }

    private fun saveUpdateFoodDetails(){
        val foodName = binding.foodNameInput.text.toString()
        val expirationDate = calendar.timeInMillis
        val price = binding.costInput.text.toString().toDoubleOrNull() ?: 0.0
        val photoUri = photoUri?.toString()

        if (foodName.isNotBlank() || photoUri != null ) {
            val foodItem = FoodItem(
                id = foodId, // Keep the same ID for updates
                foodName = foodName,
                expirationDate = expirationDate,
                cost = price,
                foodPhotoUri = photoUri.toString()
            )

            // Save the updated item in the database
            foodDetailViewModel.updateFoodItem(foodItem)
            Toast.makeText(requireContext(), "Food item updated!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        } else {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteFoodItem(foodId: Long){
        foodDetailViewModel.deleteFoodItemById(foodId)
        findNavController().popBackStack()
    }
}

package com.example.keepfresh.ui.foodInput

import BarcodeScannerDialogFragment
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.keepfresh.R
import com.example.keepfresh.Util
import com.example.keepfresh.Util.ImagePickerHelper
import com.example.keepfresh.Util.ImagePickerHelper.ImageSource
import com.example.keepfresh.databinding.FragmentInputBinding
import com.example.keepfresh.data.FoodDatabase
import com.example.keepfresh.data.FoodDatabaseDao
import com.example.keepfresh.data.FoodItem
import com.example.keepfresh.data.FoodRepository
import com.squareup.picasso.Picasso
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

    private lateinit var foodNameInput: EditText
    private lateinit var expirationDateInput: EditText
    private lateinit var photoFood: ImageView

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
        setupBarcodeResultListener()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        foodNameInput = binding.inputFoodName
        expirationDateInput = binding.inputExpirationDate
        photoFood = binding.photoFood
    }

    private fun setupUI() {
        binding.btnChangePhoto.setOnClickListener {
            Util.checkPermissions(requireActivity())
            val dialog = FoodPhotoDialogFragment()
            dialog.setListener(this)
            dialog.show(childFragmentManager, "FoodPhotoDialogFragment")
        }

        binding.inputExpirationDate.setOnClickListener {
            Util.showDatePicker(requireContext(), expirationDateInput, calendar)
        }

        binding.btnSave.setOnClickListener {
            saveFoodItem()
        }

        binding.btnCancel.setOnClickListener {
            clearInputs()
        }

            binding.barcodeIcon.setOnClickListener {
                Util.checkPermissions(requireActivity())
                val barcodeFragment = BarcodeScannerDialogFragment()
                barcodeFragment.show(childFragmentManager, "BarcodeScannerDialogFragment")
            }


//        binding.testApiButton.setOnClickListener {
//            testFetchProductDetails()
//        }

    }

    private fun saveFoodItem() {
        val foodName = foodNameInput.text.toString()
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
        foodNameInput.text?.clear()
        expirationDateInput.text?.clear()
        photoFood.setImageURI(null)
        photoFood.setImageResource(R.drawable.ic_placeholder)
        ImagePickerHelper.setPhotoUri(Uri.EMPTY)
    }

    private fun setupPhotoResultLauncher() {
        photoResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoFood.setImageURI(ImagePickerHelper.getPhotoUri())
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

//    private fun testFetchProductDetails() {
//        val testBarcode = "737628064502"
//        fetchProductDetails(testBarcode)
//    }

    private fun setupBarcodeResultListener() {
        // Listen for barcode scan results
        childFragmentManager.setFragmentResultListener("barcode_result", viewLifecycleOwner) { _, bundle ->
            val barcode = bundle.getString("barcode")
            barcode?.let {
                Log.d("FoodInputFragment", "Scanned Barcode: $it")
                fetchProductDetails(it) // Use the scanned barcode to fetch product details
            }
        }
    }

    private fun fetchProductDetails(barcode: String) {
        foodInputViewModel.fetchFoodDetailsFromBarcode(barcode)
        foodInputViewModel.scannedFoodItem.observe(viewLifecycleOwner) { foodItem ->
            if (foodItem != null) {
                foodNameInput.setText(foodItem.getFoodName())
                calendar.timeInMillis = foodItem.getExpirationDate()
                expirationDateInput.setText(Util.formatDate(calendar.timeInMillis))

                foodItem.getFoodPhotoUri()?.takeIf { it.isNotEmpty() }?.let { photoUri ->
                    // Update ImageView with the fetched photo URI
                    ImagePickerHelper.setPhotoUri(Uri.parse(photoUri))
                    Picasso.get()
                        .load(photoUri)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_error)
                        .into(photoFood)
                } ?: run {
                    // If the URI is null or empty, set a default placeholder image
                    photoFood.setImageResource(R.drawable.ic_placeholder)
                    Log.w("FoodInputFragment", "Photo URI is empty or null")
                }

                Toast.makeText(requireContext(), "Product details loaded!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to fetch food details", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

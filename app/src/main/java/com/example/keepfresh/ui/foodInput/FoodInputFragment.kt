package com.example.keepfresh.ui.foodInput

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.keepfresh.Util
import com.example.keepfresh.databinding.FragmentInputBinding
import com.example.keepfresh.data.FoodDatabase
import com.example.keepfresh.data.FoodDatabaseDao
import com.example.keepfresh.data.FoodItem
import com.example.keepfresh.data.FoodRepository
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FoodInputFragment : Fragment(), FoodPhotoDialogFragment.FoodPhotoListener, DatePickerDialog.OnDateSetListener {

    private var _binding: FragmentInputBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FoodDatabase
    private lateinit var databaseDao: FoodDatabaseDao
    private lateinit var repository: FoodRepository
    private lateinit var viewModelFactory: FoodInputViewModelFactory
    private lateinit var foodInputViewModel: FoodInputViewModel

    private lateinit var photoResultLauncher: ActivityResultLauncher<Intent>
    private var photoUri: Uri? = null

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

    private fun setupUI(){
        binding.btnChangePhoto.setOnClickListener {
            Util.checkPermissions(requireActivity())
            val dialog = FoodPhotoDialogFragment()
            dialog.setListener(this)
            dialog.show(childFragmentManager, "FoodPhotoDialogFragment")
        }

        binding.inputExpirationDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(), this,calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        binding.btnSave.setOnClickListener {
            saveFoodItem()
        }

        binding.btnCancel.setOnClickListener {
            clearInputs()
        }
    }

    private fun saveFoodItem(){
        val foodName = binding.inputFoodName.text.toString()
        val expirationDate = calendar.timeInMillis

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

    private fun clearInputs(){
        binding.inputFoodName.text?.clear()
        binding.inputExpirationDate.text?.clear()
        binding.photoFood.setImageURI(null)
        photoUri = null
    }

    // Display Date from User Input
    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        val selectedDate = "${year}/${month + 1}/$dayOfMonth"
        binding.inputExpirationDate.setText(selectedDate)
        calendar.set(year, month, dayOfMonth)
    }

    // Implement FoodPhotoListeners methods
    private fun setupPhotoResultLauncher(){
        photoResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                binding.photoFood.setImageURI(photoUri)
            } else {
                Toast.makeText(requireContext(), "Failed to select or capture image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCameraSelected() {
        launchImageSelection(ImageSource.CAMERA)
    }

    override fun onGallerySelected() {
        launchImageSelection(ImageSource.GALLERY)
    }

    private fun launchImageSelection(source: ImageSource){
        val intent = when (source) {
            ImageSource.GALLERY -> Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            ImageSource.CAMERA -> {
                val photoFile = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Toast.makeText(requireContext(), "Error creating file", Toast.LENGTH_SHORT).show()
                    null
                }
                photoFile?.let {
                    photoUri = FileProvider.getUriForFile(requireContext(), "com.example.keepfresh.provider", photoFile)
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    }
                }
            }
        }
        intent?.let { photoResultLauncher.launch(it) }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = requireActivity().getExternalFilesDir(null)!!
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private enum class ImageSource{
        GALLERY, CAMERA
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
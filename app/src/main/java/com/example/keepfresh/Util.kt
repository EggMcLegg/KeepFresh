package com.example.keepfresh

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Util {
    fun checkPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23) return

        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(activity!!, it) != PackageManager.PERMISSION_GRANTED
        }

        // Request permissions if any are missing
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity!!, missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }
    private const val PERMISSION_REQUEST_CODE = 101

    /*
    Retrieves a Bitmap image from the specified Uri,
    applying necessary rotation based on EXIF data.
     */
    fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(imgUri)
        var bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Read the orientation from EXIF data
        val exifInterface = ExifInterface(context.contentResolver.openInputStream(imgUri)!!)
        val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        // Determine the rotation angle from the EXIF orientation
        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        // Apply rotation if necessary
        if (rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        return bitmap
    }

    fun formatDate(date: Long): String{
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return dateFormat.format(Date(date))
    }

    fun formatPrice(price: Double?): String {
        return if (price != null) {
            String.format("%.2f$", price)
        } else {
            "0.00$"
        }
    }

    fun showDatePicker(context: Context, editText: EditText, calendar: Calendar) {
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = "${year}/${month + 1}/$dayOfMonth"
                editText.setText(selectedDate)
                calendar.set(year, month, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    object ImagePickerHelper {
        private var photoUri: Uri? = null

        enum class ImageSource {
            GALLERY, CAMERA
        }
        fun setPhotoUri(uri: Uri?) {
            photoUri = uri
        }

        fun getPhotoUri(): Uri? = photoUri

        fun launchImageSelection(
            context: Context,
            source: ImageSource,
            activityResultLauncher: ActivityResultLauncher<Intent>
        ) {
            val intent = when (source) {
                ImageSource.GALLERY -> Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                ImageSource.CAMERA -> {
                    val photoFile = try {
                        createImageFile(context)
                    } catch (ex: IOException) {
                        Toast.makeText(context, "Error creating file", Toast.LENGTH_SHORT).show()
                        null
                    }
                    photoFile?.let {
                        photoUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", it)
                        Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                            putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri)
                        }
                    }
                }
            }
            intent?.let { activityResultLauncher.launch(it) }
        }

        @Throws(IOException::class)
        private fun createImageFile(context: Context): File {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir: File = context.getExternalFilesDir(null)!!
            return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        }
    }
}
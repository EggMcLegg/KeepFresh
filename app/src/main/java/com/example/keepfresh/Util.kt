package com.example.keepfresh

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

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
}
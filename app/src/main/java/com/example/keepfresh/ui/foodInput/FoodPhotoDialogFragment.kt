package com.example.keepfresh.ui.foodInput

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class FoodPhotoDialogFragment: DialogFragment(){

    interface FoodPhotoListener{
        fun onCameraSelected()
        fun onGallerySelected()
    }

    private var foodPhotoListener: FoodPhotoListener? = null

    fun setListener(listener: FoodPhotoListener) {
        this.foodPhotoListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose an option")
            .setItems(arrayOf("Camera", "Gallery")) { _, which ->
                when (which) {
                    0 -> foodPhotoListener?.onCameraSelected()  // Notify listener for camera
                    1 -> foodPhotoListener?.onGallerySelected() // Notify listener for gallery
                }
            }
        return builder.create()
    }

    override fun onDetach() {
        super.onDetach()
        foodPhotoListener = null
    }
}
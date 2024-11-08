package com.example.keepfresh.ui.foodInventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.keepfresh.R
import com.example.keepfresh.data.FoodItem
import com.example.keepfresh.databinding.ItemsFoodDisplayBinding
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FoodInventoryAdapter(private val foodItems: List<FoodItem>)
    : RecyclerView.Adapter<FoodInventoryAdapter.FoodInventoryViewHolder>() {

    class FoodInventoryViewHolder(private val binding: ItemsFoodDisplayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(foodItem: FoodItem) {
            binding.foodName.text = foodItem.getFoodName()
            binding.expirationDate.text = "Expires on: ${formatDate(foodItem.getExpirationDate())}"

            // Load image using Picasso library
            Picasso.get()
                .load(foodItem.getFoodPhotoUri())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(binding.foodImage)
        }

        private fun formatDate(date: Long): String{
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            return dateFormat.format(Date(date))
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodInventoryViewHolder {
        val binding = ItemsFoodDisplayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FoodInventoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodInventoryViewHolder, position: Int) {
        holder.bind(foodItems[position])
    }

    override fun getItemCount(): Int = foodItems.size
}

package com.example.keepfresh.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items_table")
data class FoodItem (
    @PrimaryKey(autoGenerate = true)
    private var id: Long = 0L,

    @ColumnInfo(name = "food_name")
    private var foodName: String = "",

    @ColumnInfo(name = "expiration_date")
    private var expirationDate: Long = 0L,

    @ColumnInfo(name = "food_photo_uri")
    private var foodPhotoUri: String = "",
){
    // Getters
    fun getId(): Long = id
    fun getFoodName(): String = foodName
    fun getExpirationDate(): Long = expirationDate
    fun getFoodPhotoUri(): String = foodPhotoUri

    // Setters
    fun setId(id: Long) {
        this.id = id
    }

    fun setFoodName(foodName: String){
        this.foodName = foodName
    }

    fun setExpirationDate(expirationDate: Long){
        this.expirationDate = expirationDate
    }

    fun setFoodPhotoUri(foodPhotoUri: String){
        this.foodPhotoUri = foodPhotoUri
    }

}
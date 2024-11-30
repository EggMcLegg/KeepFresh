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

    @ColumnInfo(name = "last_notified")
    private var lastNotified: Long = 0L,

    @ColumnInfo(name = "state")
    private var state: String = "",

    @ColumnInfo(name = "money_spent")
    private var moneySpent: Double = 0.0,

    @ColumnInfo(name = "notification_option")
    private var notificationOption: Boolean = false,
){
    // Getters
    fun getId(): Long = id
    fun getFoodName(): String = foodName
    fun getExpirationDate(): Long = expirationDate
    fun getFoodPhotoUri(): String = foodPhotoUri
    fun getLastNotified(): Long = lastNotified
    fun getState(): String = state
    fun getMoneySpent(): Double = moneySpent
    fun getNotificationOption(): Boolean = notificationOption

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

    fun setLastNotified(lastNotified: Long){
        this.lastNotified = lastNotified
    }

    fun setState(state: String) {
        this.state = state
    }

    fun setMoneySpent(moneySpent: Double) {
        this.moneySpent = moneySpent
    }

    fun setNotificationOption(notificationOption: Boolean) {
        this.notificationOption = notificationOption
    }
}
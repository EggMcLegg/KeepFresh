package com.example.keepfresh.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FoodItem::class], version = 1)
abstract class FoodDatabase : RoomDatabase() {
    abstract val foodDatabaseDao : FoodDatabaseDao

    companion object{
        //The Volatile keyword guarantees visibility of changes to the INSTANCE variable across threads
        @Volatile
        private var INSTANCE: FoodDatabase? = null

        fun getInstance(context: Context) : FoodDatabase {
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext,
                        FoodDatabase::class.java, "food_items_table").build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

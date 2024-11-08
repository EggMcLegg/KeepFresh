package com.example.keepfresh.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FoodItem::class], version = 2, exportSchema = false)
abstract class FoodDatabase : RoomDatabase() {
    abstract val foodDatabaseDao : FoodDatabaseDao

    companion object{
        private val migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE food_items_table ADD COLUMN last_notified INTEGER NOT NULL DEFAULT 0")
            }
        }
        //The Volatile keyword guarantees visibility of changes to the INSTANCE variable across threads
        @Volatile
        private var INSTANCE: FoodDatabase? = null

        fun getInstance(context: Context) : FoodDatabase {
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext,
                        FoodDatabase::class.java, "food_items_table").addMigrations(migration).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

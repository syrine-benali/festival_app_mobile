package com.example.festivalappmobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.festivalappmobile.data.local.dao.ReservationDao
import com.example.festivalappmobile.data.local.entity.ReservationEntity

@Database(entities = [ReservationEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reservationDao(): ReservationDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "festival_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
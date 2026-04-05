package com.example.festivalappmobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.festivalappmobile.data.local.dao.DashboardEditeurDao
import com.example.festivalappmobile.data.local.dao.DashboardFestivalDao
import com.example.festivalappmobile.data.local.dao.DashboardJeuDao
import com.example.festivalappmobile.data.local.dao.ReservationDao
import com.example.festivalappmobile.data.local.entity.DashboardEditeurEntity
import com.example.festivalappmobile.data.local.entity.DashboardFestivalEntity
import com.example.festivalappmobile.data.local.entity.DashboardJeuEntity
import com.example.festivalappmobile.data.local.entity.ReservationEntity

@Database(
    entities = [
        ReservationEntity::class,
        DashboardFestivalEntity::class,
        DashboardJeuEntity::class,
        DashboardEditeurEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reservationDao(): ReservationDao
    abstract fun dashboardFestivalDao(): DashboardFestivalDao
    abstract fun dashboardJeuDao(): DashboardJeuDao
    abstract fun dashboardEditeurDao(): DashboardEditeurDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "festival_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example.festivalappmobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.festivalappmobile.data.local.dao.EditeurDao
import com.example.festivalappmobile.data.local.dao.FestivalDao
import com.example.festivalappmobile.data.local.dao.GameDao
import com.example.festivalappmobile.data.local.dao.ReservationDao
import com.example.festivalappmobile.data.local.entity.EditeurEntity
import com.example.festivalappmobile.data.local.entity.FestivalEntity
import com.example.festivalappmobile.data.local.entity.GameEntity
import com.example.festivalappmobile.data.local.entity.ReservationEntity

/**
 * Base de données Room locale.
 * Sert de Single Source of Truth pour l'UI en mode offline-first.
 * version = 3 : ajout des tables festivals, games, editeurs
 */
@Database(
    entities = [
        ReservationEntity::class,
        FestivalEntity::class,
        GameEntity::class,
        EditeurEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reservationDao(): ReservationDao
    abstract fun festivalDao(): FestivalDao
    abstract fun gameDao(): GameDao
    abstract fun editeurDao(): EditeurDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "festival_db"
                )
                    // La DB est un cache : la migration destructive est acceptable.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
package com.example.festivalappmobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.festivalappmobile.data.local.entity.ReservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {
    // Retourne un Flow : chaque modif dans la DB mettra à jour l'UI automatiquement !
    @Query("SELECT * FROM reservations")
    fun getAllReservations(): Flow<List<ReservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservations(reservations: List<ReservationEntity>)

    @Query("DELETE FROM reservations")
    suspend fun clearReservations()
}
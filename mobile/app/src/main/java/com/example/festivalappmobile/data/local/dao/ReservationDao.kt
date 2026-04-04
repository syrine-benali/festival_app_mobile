package com.example.festivalappmobile.data.local.dao

import androidx.room.*
import com.example.festivalappmobile.data.local.entity.ReservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservations ORDER BY id DESC")
    fun getAll(): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE festivalId = :festivalId ORDER BY id DESC")
    fun getByFestival(festivalId: Int): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE id = :id")
    suspend fun getById(id: Int): ReservationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reservations: List<ReservationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reservation: ReservationEntity)

    @Delete
    suspend fun delete(reservation: ReservationEntity)

    @Query("DELETE FROM reservations")
    suspend fun clearAll()
}
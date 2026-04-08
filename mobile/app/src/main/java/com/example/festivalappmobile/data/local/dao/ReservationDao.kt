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

    @Query("SELECT id FROM reservations")
    suspend fun getAllIds(): List<Int>

    @Query("SELECT id FROM reservations WHERE festivalId = :festivalId")
    suspend fun getIdsByFestival(festivalId: Int): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reservations: List<ReservationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reservation: ReservationEntity)

    @Delete
    suspend fun delete(reservation: ReservationEntity)

    @Query("DELETE FROM reservations WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM reservations")
    suspend fun clearAll()
}
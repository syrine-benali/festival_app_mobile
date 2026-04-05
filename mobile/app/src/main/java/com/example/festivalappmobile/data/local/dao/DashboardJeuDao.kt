package com.example.festivalappmobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.festivalappmobile.data.local.entity.DashboardJeuEntity

@Dao
interface DashboardJeuDao {

    @Query("SELECT * FROM dashboard_jeux")
    suspend fun getAll(): List<DashboardJeuEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jeux: List<DashboardJeuEntity>)

    @Query("DELETE FROM dashboard_jeux")
    suspend fun clear()
}

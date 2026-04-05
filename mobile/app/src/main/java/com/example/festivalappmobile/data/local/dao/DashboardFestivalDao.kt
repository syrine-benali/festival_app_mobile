package com.example.festivalappmobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.festivalappmobile.data.local.entity.DashboardFestivalEntity

@Dao
interface DashboardFestivalDao {

    @Query("SELECT * FROM dashboard_festivals")
    suspend fun getAll(): List<DashboardFestivalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(festivals: List<DashboardFestivalEntity>)

    @Query("DELETE FROM dashboard_festivals")
    suspend fun clear()
}

package com.example.festivalappmobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.festivalappmobile.data.local.entity.DashboardEditeurEntity

@Dao
interface DashboardEditeurDao {

    @Query("SELECT * FROM dashboard_editeurs")
    suspend fun getAll(): List<DashboardEditeurEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(editeurs: List<DashboardEditeurEntity>)

    @Query("DELETE FROM dashboard_editeurs")
    suspend fun clear()
}

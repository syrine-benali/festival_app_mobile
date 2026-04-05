package com.example.festivalappmobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cache Room pour les festivals du tableau de bord.
 * Stocke les festivals actifs tels qu'ils ont été affichés lors du dernier accès en ligne.
 */
@Entity(tableName = "dashboard_festivals")
data class DashboardFestivalEntity(
    @PrimaryKey val id: Int,
    val nom: String,
    val lieu: String,
    val dateDebut: String,
    val dateFin: String,
    val nbTotalTable: Int,
    val nbTotalChaise: Int,
    val bigTables: Int,
    val bigChairs: Int,
    val smallTables: Int,
    val smallChairs: Int,
    val mairieTables: Int,
    val mairieChairs: Int
)

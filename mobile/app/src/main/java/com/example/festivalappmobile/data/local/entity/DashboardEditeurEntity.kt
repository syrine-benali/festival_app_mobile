package com.example.festivalappmobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cache Room pour les éditeurs du tableau de bord.
 * Note : le champ logo n'est pas stocké — il n'est pas accessible sans connexion.
 */
@Entity(tableName = "dashboard_editeurs")
data class DashboardEditeurEntity(
    @PrimaryKey val id: Int,
    val libelle: String,
    val exposant: Boolean,
    val distributeur: Boolean,
    val phone: String?,
    val email: String?,
    val notes: String?,
    val workflowStatus: String?,
    val hasReservation: Boolean
)

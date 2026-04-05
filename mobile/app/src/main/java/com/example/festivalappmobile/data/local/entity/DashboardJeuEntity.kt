package com.example.festivalappmobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cache Room pour les jeux du tableau de bord.
 * Note : le champ image n'est pas stocké — il n'est pas accessible sans connexion.
 */
@Entity(tableName = "dashboard_jeux")
data class DashboardJeuEntity(
    @PrimaryKey val id: Int,
    val libelle: String,
    val auteur: String?,
    val nbMinJoueur: Int?,
    val nbMaxJoueur: Int?,
    val ageMin: Int?,
    val duree: Int?,
    val prototype: Boolean,
    val editeurId: Int?,
    val editeurNom: String?,
    val typeJeuId: Int?,
    val typeJeuNom: String?
)

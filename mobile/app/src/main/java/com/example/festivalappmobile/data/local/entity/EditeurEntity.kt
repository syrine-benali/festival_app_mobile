package com.example.festivalappmobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.models.WorkflowStatus

/**
 * Entité Room pour le cache local des éditeurs.
 * [workflowStatus] est stocké comme String (nom de l'enum) pour la compatibilité Room.
 */
@Entity(tableName = "editeurs")
data class EditeurEntity(
    @PrimaryKey val id: Int,
    val libelle: String,
    val exposant: Boolean,
    val distributeur: Boolean,
    val logo: String?,
    val phone: String?,
    val email: String?,
    val notes: String?,
    val workflowStatus: String?,
    val hasReservation: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun EditeurEntity.toDomain() = Editeur(
    id = id,
    libelle = libelle,
    exposant = exposant,
    distributeur = distributeur,
    logo = logo,
    phone = phone,
    email = email,
    notes = notes,
    workflowStatus = workflowStatus?.let {
        try { WorkflowStatus.valueOf(it) } catch (e: IllegalArgumentException) { null }
    },
    hasReservation = hasReservation
)

fun Editeur.toEntity() = EditeurEntity(
    id = id,
    libelle = libelle,
    exposant = exposant,
    distributeur = distributeur,
    logo = logo,
    phone = phone,
    email = email,
    notes = notes,
    workflowStatus = workflowStatus?.name,
    hasReservation = hasReservation
)

package com.example.festivalappmobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.festivalappmobile.domain.models.*

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey val id: Int,
    val editeurId: Int,
    val editeurLibelle: String,
    val festivalId: Int,
    val festivalNom: String,
    val workflowStatus: String,
    val typeReservant: String,
    val viendraPresenteSesJeux: Boolean,
    val nousPresentons: Boolean,
    val listeJeuxDemandee: Boolean,
    val listeJeuxObtenue: Boolean,
    val jeuxRecusPhysiquement: Boolean,
    val notesClient: String?,
    val notesWorkflow: String?,
    val nbPrisesElectriques: Int,
    val typeRemise: String?,
    val valeurRemise: Double,
    val totalTables: Int,
    val totalPrice: Double,
    val cachedAt: Long = System.currentTimeMillis() // timestamp pour savoir si le cache est frais
)

fun ReservationEntity.toDomain() = ReservationSummary(
    id = id,
    editeurId = editeurId,
    editeurLibelle = editeurLibelle,
    festivalId = festivalId,
    festivalNom = festivalNom,
    workflowStatus = WorkflowStatus.fromString(workflowStatus),
    typeReservant = TypeReservant.entries.firstOrNull { it.name == typeReservant }
        ?: TypeReservant.EDITEUR,
    totalTables = totalTables,
    totalPrice = totalPrice
)

fun ReservationSummary.toEntity() = ReservationEntity(
    id = id,
    editeurId = editeurId,
    editeurLibelle = editeurLibelle,
    festivalId = festivalId,
    festivalNom = festivalNom,
    workflowStatus = workflowStatus.name,
    typeReservant = typeReservant.name,
    viendraPresenteSesJeux = true,
    nousPresentons = false,
    listeJeuxDemandee = false,
    listeJeuxObtenue = false,
    jeuxRecusPhysiquement = false,
    notesClient = null,
    notesWorkflow = null,
    nbPrisesElectriques = 0,
    typeRemise = null,
    valeurRemise = 0.0,
    totalTables = totalTables,
    totalPrice = totalPrice
)
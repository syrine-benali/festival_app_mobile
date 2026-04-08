package com.example.festivalappmobile.domain.models

data class Reservation(
    val id: Int,
    val editeurId: Int,
    val editeurLibelle: String,
    val festivalId: Int,
    val festivalNom: String,
    val workflowStatus: WorkflowStatus,
    val typeReservant: TypeReservant,
    val dateFacturation: String?,
    // Flags workflow
    val viendraPresenteSesJeux: Boolean,
    val nousPresentons: Boolean,
    val listeJeuxDemandee: Boolean,
    val listeJeuxObtenue: Boolean,
    val jeuxRecusPhysiquement: Boolean,
    // Notes
    val notesClient: String?,
    val notesWorkflow: String?,
    // Finances
    val nbPrisesElectriques: Int,
    val typeRemise: TypeRemise?,
    val valeurRemise: Double,
    // Relations
    val reservationLines: List<ReservationLine>,
    val reservationContacts: List<ReservationContact>,
    val reservationJeux: List<ReservationJeu>,
    // Calculé
    val totalTables: Int,
    val totalPrice: Double
)

// Version allégée pour la liste
data class ReservationSummary(
    val id: Int,
    val editeurId: Int,
    val editeurLibelle: String,
    val festivalId: Int,
    val festivalNom: String,
    val workflowStatus: WorkflowStatus,
    val typeReservant: TypeReservant,
    val totalTables: Int,
    val totalPrice: Double
)
package com.example.festivalappmobile.domain.repository

import com.example.festivalappmobile.domain.models.*
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {

    // Liste (Flow pour réactivité + offline)
    fun getReservations(festivalId: Int? = null): Flow<List<ReservationSummary>>

    // Détail
    suspend fun getReservationById(id: Int): Result<Reservation>

    // CRUD Réservation
    suspend fun createReservation(
        editeurId: Int,
        festivalId: Int,
        typeReservant: TypeReservant = TypeReservant.EDITEUR,
        notesClient: String? = null
    ): Result<Reservation>

    suspend fun deleteReservation(id: Int): Result<Unit>

    suspend fun updateWorkflowStatus(
        id: Int,
        status: WorkflowStatus
    ): Result<Reservation>

    suspend fun updateReservationFlags(
        id: Int,
        typeReservant: TypeReservant? = null,
        dateFacturation: String? = null,
        viendraPresenteSesJeux: Boolean? = null,
        nousPresentons: Boolean? = null,
        listeJeuxDemandee: Boolean? = null,
        listeJeuxObtenue: Boolean? = null,
        jeuxRecusPhysiquement: Boolean? = null,
        notesClient: String? = null,
        notesWorkflow: String? = null,
        typeRemise: TypeRemise? = null,
        valeurRemise: Double? = null,
        nbPrisesElectriques: Int? = null
    ): Result<Reservation>

    // Contacts
    suspend fun addContact(
        reservationId: Int,
        dateContact: String,
        commentaire: String? = null
    ): Result<ReservationContact>

    suspend fun deleteContact(contactId: Int): Result<Unit>

    // Lignes tarifaires
    suspend fun addLine(
        reservationId: Int,
        pricingId: Int,
        nbTables: Int,
        grandesTablesSouhaitees: Boolean = false
    ): Result<ReservationLine>

    suspend fun updateLine(
        lineId: Int,
        nbTables: Int? = null,
        nbM2: Double? = null,
        grandesTablesSouhaitees: Boolean? = null
    ): Result<ReservationLine>

    suspend fun deleteLine(lineId: Int): Result<Unit>

    // Jeux
    suspend fun addJeu(
        reservationId: Int,
        jeuId: Int,
        nbExemplaires: Int,
        nbTablesAllouees: Int,
        placementId: Int? = null
    ): Result<ReservationJeu>

    suspend fun updateJeu(
        jeuId: Int,
        nbExemplaires: Int? = null,
        nbTablesAllouees: Int? = null,
        placementId: Int? = null
    ): Result<ReservationJeu>

    suspend fun deleteJeu(jeuId: Int): Result<Unit>

    suspend fun calculatePrice(reservationId: Int): Result<Double>

    // Sync offline
    fun isOnline(): Boolean
}
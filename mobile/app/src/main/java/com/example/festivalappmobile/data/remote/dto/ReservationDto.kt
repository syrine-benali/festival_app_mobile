package com.example.festivalappmobile.data.remote.dto

import com.example.festivalappmobile.domain.models.*
import com.google.gson.annotations.SerializedName

data class ReservationLineDto(
    @SerializedName("id") val id: Int,
    @SerializedName("zoneTarifaireId") val pricingId: Int,
    @SerializedName("zoneTarifaire") val pricing: PricingDto?,
    @SerializedName("nbTables") val nbTables: Int,
    @SerializedName("nbM2") val nbM2: Double,
    @SerializedName("grandesTablesSouhaitees") val grandesTablesSouhaitees: Boolean,
    @SerializedName("sousTotal") val sousTotal: Double
)

data class PricingDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nom") val nom: String,
    @SerializedName("prixTable") val prixTable: Double,
    @SerializedName("prixM2") val prixM2: Double
)

data class ReservationContactDto(
    @SerializedName("id") val id: Int,
    @SerializedName("dateContact") val dateContact: String,
    @SerializedName("commentaire") val commentaire: String?
)

data class ReservationJeuDto(
    @SerializedName("id") val id: Int,
    @SerializedName("jeuId") val jeuId: Int,
    @SerializedName("jeu") val jeu: JeuSimpleDto?,
    @SerializedName("editeurJeuId") val editeurJeuId: Int?,
    @SerializedName("editeurJeu") val editeurJeu: EditeurSimpleDto?,
    @SerializedName("zonePlanId") val placementId: Int?,
    @SerializedName("zonePlan") val placement: PlacementDto?,
    @SerializedName("nbExemplaires") val nbExemplaires: Int,
    @SerializedName("nbTablesAllouees") val nbTablesAllouees: Int
)

data class JeuSimpleDto(
    @SerializedName("id") val id: Int,
    @SerializedName("libelle") val libelle: String
)

data class EditeurSimpleDto(
    @SerializedName("id") val id: Int,
    @SerializedName("libelle") val libelle: String
)

data class PlacementDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nom") val nom: String
)

data class ReservationDto(
    @SerializedName("id") val id: Int,
    @SerializedName("editeurId") val editeurId: Int,
    @SerializedName("editeur") val editeur: EditeurSimpleDto?,
    @SerializedName("festivalId") val festivalId: Int,
    @SerializedName("festival") val festival: FestivalSimpleDto?,
    @SerializedName("workflowStatus") val workflowStatus: String,
    @SerializedName("typeReservant") val typeReservant: String,
    @SerializedName("dateFacturation") val dateFacturation: String?,
    @SerializedName("viendraPresenteSesJeux") val viendraPresenteSesJeux: Boolean,
    @SerializedName("nousPresentons") val nousPresentons: Boolean,
    @SerializedName("listeJeuxDemandee") val listeJeuxDemandee: Boolean,
    @SerializedName("listeJeuxObtenue") val listeJeuxObtenue: Boolean,
    @SerializedName("jeuxRecusPhysiquement") val jeuxRecusPhysiquement: Boolean,
    @SerializedName("notesClient") val notesClient: String?,
    @SerializedName("notesWorkflow") val notesWorkflow: String?,
    @SerializedName("nbPrisesElectriques") val nbPrisesElectriques: Int,
    @SerializedName("typeRemise") val typeRemise: String?,
    @SerializedName("valeurRemise") val valeurRemise: Double,
    @SerializedName("reservationLines") val reservationLines: List<ReservationLineDto>?,
    @SerializedName("reservationContacts") val reservationContacts: List<ReservationContactDto>?,
    @SerializedName("reservationJeux") val reservationJeux: List<ReservationJeuDto>?
)

data class FestivalSimpleDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nom") val nom: String
)

// Wrappers de réponse API
data class ReservationResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("reservation") val reservation: ReservationDto?
)

data class ReservationDeleteResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?
)

data class ReservationListResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<ReservationSummaryDto>?,
    @SerializedName("total") val total: Int
)

data class ReservationSummaryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("editeurId") val editeurId: Int,
    @SerializedName("editeur") val editeur: EditeurSimpleDto?,
    @SerializedName("festivalId") val festivalId: Int,
    @SerializedName("festival") val festival: FestivalSimpleDto?,
    @SerializedName("workflowStatus") val workflowStatus: String,
    @SerializedName("typeReservant") val typeReservant: String,
    @SerializedName("totalTables") val totalTables: Int,
    @SerializedName("totalPrice") val totalPrice: Double
)

// Request DTOs
data class CreateReservationRequestDto(
    @SerializedName("editeurId") val editeurId: Int,
    @SerializedName("festivalId") val festivalId: Int,
    @SerializedName("typeReservant") val typeReservant: String = "EDITEUR",
    @SerializedName("notesClient") val notesClient: String? = null
)

data class UpdateReservationRequestDto(
    @SerializedName("workflowStatus") val workflowStatus: String? = null,
    @SerializedName("typeReservant") val typeReservant: String? = null,
    @SerializedName("dateFacturation") val dateFacturation: String? = null,
    @SerializedName("viendraPresenteSesJeux") val viendraPresenteSesJeux: Boolean? = null,
    @SerializedName("nousPresentons") val nousPresentons: Boolean? = null,
    @SerializedName("listeJeuxDemandee") val listeJeuxDemandee: Boolean? = null,
    @SerializedName("listeJeuxObtenue") val listeJeuxObtenue: Boolean? = null,
    @SerializedName("jeuxRecusPhysiquement") val jeuxRecusPhysiquement: Boolean? = null,
    @SerializedName("notesClient") val notesClient: String? = null,
    @SerializedName("notesWorkflow") val notesWorkflow: String? = null,
    @SerializedName("typeRemise") val typeRemise: String? = null,
    @SerializedName("valeurRemise") val valeurRemise: Double? = null,
    @SerializedName("nbPrisesElectriques") val nbPrisesElectriques: Int? = null
)

data class AddContactRequestDto(
    @SerializedName("dateContact") val dateContact: String,
    @SerializedName("commentaire") val commentaire: String? = null
)

data class AddContactResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("contact") val contact: ReservationContactDto?
)

data class AddLineRequestDto(
    @SerializedName("zoneTarifaireId") val pricingId: Int,
    @SerializedName("nbTables") val nbTables: Int,
    @SerializedName("grandesTablesSouhaitees") val grandesTablesSouhaitees: Boolean = false
)

data class UpdateLineRequestDto(
    @SerializedName("nbTables") val nbTables: Int? = null,
    @SerializedName("nbM2") val nbM2: Double? = null,
    @SerializedName("grandesTablesSouhaitees") val grandesTablesSouhaitees: Boolean? = null
)

data class AddLineResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("line") val line: ReservationLineDto?
)

data class AddJeuRequestDto(
    @SerializedName("jeuId") val jeuId: Int,
    @SerializedName("nbExemplaires") val nbExemplaires: Int,
    @SerializedName("nbTablesAllouees") val nbTablesAllouees: Int,
    @SerializedName("zonePlanId") val placementId: Int? = null
)

data class UpdateJeuRequestDto(
    @SerializedName("nbExemplaires") val nbExemplaires: Int? = null,
    @SerializedName("nbTablesAllouees") val nbTablesAllouees: Int? = null,
    @SerializedName("zonePlanId") val placementId: Int? = null
)

data class AddJeuResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("jeu") val jeu: ReservationJeuDto?
)

data class PriceCalculationResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("totalTables") val totalTables: Int,
    @SerializedName("totalM2") val totalM2: Double,
    @SerializedName("sousTotal") val sousTotal: Double,
    @SerializedName("coutPrises") val coutPrises: Double,
    @SerializedName("remise") val remise: Double,
    @SerializedName("totalGeneral") val totalGeneral: Double
)

// Wrapper réel de GET /api/reservations :
// { success, data: [...], total, currentPage, totalPages }
data class ReservationsListResponseDto(
    val success: Boolean,
    val data: List<ReservationDto>,
    val total: Int,
    val currentPage: Int = 1,
    val totalPages: Int = 1
)
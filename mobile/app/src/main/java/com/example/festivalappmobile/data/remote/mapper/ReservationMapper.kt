package com.example.festivalappmobile.data.remote.mapper

import com.example.festivalappmobile.data.remote.dto.*
import com.example.festivalappmobile.domain.models.*

fun ReservationSummaryDto.toDomain() = ReservationSummary(
    id = id,
    editeurId = editeurId,
    editeurLibelle = editeur?.libelle ?: "Inconnu",
    festivalId = festivalId,
    festivalNom = festival?.nom ?: "Inconnu",
    workflowStatus = WorkflowStatus.fromString(workflowStatus),
    typeReservant = TypeReservant.entries.firstOrNull { it.name == typeReservant }
        ?: TypeReservant.EDITEUR,
    totalTables = totalTables,
    totalPrice = totalPrice
)

fun ReservationDto.toDomain() = Reservation(
    id = id,
    editeurId = editeurId,
    editeurLibelle = editeur?.libelle ?: "Inconnu",
    festivalId = festivalId,
    festivalNom = festival?.nom ?: "Inconnu",
    workflowStatus = WorkflowStatus.fromString(workflowStatus),
    typeReservant = TypeReservant.entries.firstOrNull { it.name == typeReservant }
        ?: TypeReservant.EDITEUR,
    dateFacturation = dateFacturation,
    viendraPresenteSesJeux = viendraPresenteSesJeux,
    nousPresentons = nousPresentons,
    listeJeuxDemandee = listeJeuxDemandee,
    listeJeuxObtenue = listeJeuxObtenue,
    jeuxRecusPhysiquement = jeuxRecusPhysiquement,
    notesClient = notesClient,
    notesWorkflow = notesWorkflow,
    nbPrisesElectriques = nbPrisesElectriques,
    typeRemise = typeRemise?.let { t ->
        TypeRemise.entries.firstOrNull { it.name == t }
    },
    valeurRemise = valeurRemise,
    reservationLines = reservationLines?.map { it.toDomain() } ?: emptyList(),
    reservationContacts = reservationContacts?.map { it.toDomain() } ?: emptyList(),
    reservationJeux = reservationJeux?.map { it.toDomain() } ?: emptyList(),
    totalTables = reservationLines?.sumOf { it.nbTables } ?: 0,
    totalPrice = reservationLines?.sumOf { it.sousTotal } ?: 0.0
)

fun ReservationLineDto.toDomain() = ReservationLine(
    id = id,
    zoneTarifaireId = zoneTarifaireId,
    zoneTarifaireNom = zoneTarifaire?.nom ?: "Zone $zoneTarifaireId",
    prixTable = zoneTarifaire?.prixTable ?: 0.0,
    nbTables = nbTables,
    nbM2 = nbM2,
    grandesTablesSouhaitees = grandesTablesSouhaitees,
    sousTotal = sousTotal
)

fun ReservationContactDto.toDomain() = ReservationContact(
    id = id,
    dateContact = dateContact,
    commentaire = commentaire
)

fun ReservationJeuDto.toDomain() = ReservationJeu(
    id = id,
    jeuId = jeuId,
    jeuLibelle = jeu?.libelle ?: "Jeu $jeuId",
    editeurJeuId = editeurJeuId,
    editeurJeuLibelle = editeurJeu?.libelle,
    zonePlanId = zonePlanId,
    zonePlanNom = zonePlan?.nom,
    nbExemplaires = nbExemplaires,
    nbTablesAllouees = nbTablesAllouees
)
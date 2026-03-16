package com.example.festivalappmobile.data.repository

import com.example.festivalappmobile.data.local.dao.ReservationDao
import com.example.festivalappmobile.data.local.entity.ReservationEntity
import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.dto.ReservationDto
import com.example.festivalappmobile.domain.models.Reservation
import com.example.festivalappmobile.domain.models.TypeReservant
import com.example.festivalappmobile.domain.models.WorkflowStatus
import com.example.festivalappmobile.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReservationRepositoryImpl(
    private val apiService: ApiService,
    private val dao: ReservationDao
) : ReservationRepository {

    // 1. La source de vérité est la base locale (Room)
    // Dès que Room est mis à jour, le Flow émet une nouvelle liste convertie pour l'UI
    override val reservations: Flow<List<Reservation>> = dao.getAllReservations().map { entities ->
        entities.map { it.toDomainModel() }
    }

    // 2. Récupère depuis Retrofit et insère dans Room
    override suspend fun refreshReservations() {
        try {
            val response = apiService.getReservations()
            if (response.isSuccessful) {
                response.body()?.let { dtos ->
                    // On convertit les DTOs du réseau en Entités pour Room
                    val entities = dtos.map { it.toEntity() }

                    // On sauvegarde dans la base locale (le Flow au-dessus sera notifié automatiquement !)
                    dao.insertReservations(entities)
                }
            } else {
                // Gestion d'erreur si le backend renvoie une erreur (ex: 401, 500)
                throw Exception("Erreur serveur : ${response.code()}")
            }
        } catch (e: Exception) {
            // S'il n'y a pas d'internet ou que le serveur est planté,
            // on relance l'erreur pour que le ViewModel puisse afficher un petit toast/message
            throw Exception("Impossible de rafraîchir les données : ${e.localizedMessage}")
        }
    }
}

// API (Dto) -> Base de données locale (Entity)
fun ReservationDto.toEntity(): ReservationEntity {
    return ReservationEntity(
        id = this.id,
        editeurId = this.editeurId,
        festivalId = this.festivalId,
        workflowStatus = this.workflowStatus,
        typeReservant = this.typeReservant,
        viendraPresenteSesJeux = this.viendra_presente_ses_jeux,
        editeurNom = this.editeur?.libelle // On extrait juste le nom si l'éditeur est joint
    )
}

// Base de données locale (Entity) -> Modèle pur pour l'UI (Domain)
fun ReservationEntity.toDomainModel(): Reservation {
    return Reservation(
        id = this.id,
        editeurId = this.editeurId,
        festivalId = this.festivalId,
        // Conversion sécurisée des String en Enum (fallback par défaut si erreur)
        workflowStatus = try {
            WorkflowStatus.valueOf(this.workflowStatus)
        } catch (e: IllegalArgumentException) {
            WorkflowStatus.PAS_DE_CONTACT
        },
        typeReservant = try {
            TypeReservant.valueOf(this.typeReservant)
        } catch (e: IllegalArgumentException) {
            TypeReservant.EDITEUR
        },
        viendraPresenteSesJeux = this.viendraPresenteSesJeux,
        editeurNom = this.editeurNom
    )
}
package com.example.festivalappmobile.domain.usecases.reservation

import com.example.festivalappmobile.domain.models.ReservationContact
import com.example.festivalappmobile.domain.repository.ReservationRepository

class AddContactUseCase(private val repo: ReservationRepository) {
    //enregistrer le fait qu'un organisateur a contacté un éditeur de jeux.
    suspend operator fun invoke(
        reservationId: Int,
        dateContact: String,
        commentaire: String? = null
    ): Result<ReservationContact> {
        if (dateContact.isBlank())
            return Result.failure(Exception("La date de contact est requise"))
        return repo.addContact(reservationId, dateContact, commentaire)
    }
}
package com.example.festivalappmobile.domain.usecases.reservation

import com.example.festivalappmobile.domain.models.Reservation
import com.example.festivalappmobile.domain.models.WorkflowStatus
import com.example.festivalappmobile.domain.repository.ReservationRepository

class UpdateWorkflowUseCase(private val repo: ReservationRepository) {
    //utilisation de Result parce que si on retorunait Reservation, l'app planterai dès qu'une erreur surviendrait
    //avec cette utilisation, on oblige le traitement du cas de l'échec
    suspend operator fun invoke(
        reservationId: Int,
        newStatus: WorkflowStatus
    ): Result<Reservation> = repo.updateWorkflowStatus(reservationId, newStatus)
}
package com.example.festivalappmobile.domain.usecases.reservation

import com.example.festivalappmobile.domain.models.Reservation
import com.example.festivalappmobile.domain.repository.ReservationRepository

class GetReservationDetailUseCase(private val repo: ReservationRepository) {
    suspend operator fun invoke(id: Int): Result<Reservation> =
        repo.getReservationById(id)
}
package com.example.festivalappmobile.domain.usecases.reservation

import com.example.festivalappmobile.domain.models.ReservationSummary
import com.example.festivalappmobile.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow

class GetReservationsUseCase(private val repo: ReservationRepository) {
    //invoke acts as a bridge (when called it asks the ReservationRepository to fetch data)
    //returns a Flow of ReservationSummary (this means that if the data in the DB or API changes,
    // the UI will auto receive the updated list without having to ask for it
    operator fun invoke(festivalId: Int? = null): Flow<List<ReservationSummary>> =
        repo.getReservations(festivalId)
}
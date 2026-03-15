package com.example.festivalappmobile.domain.repository

import com.example.festivalappmobile.domain.models.Reservation
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {
    // Un flux continu des données locales
    val reservations: Flow<List<Reservation>>

    // Une fonction pour déclencher la mise à jour depuis l'API
    suspend fun refreshReservations()
}
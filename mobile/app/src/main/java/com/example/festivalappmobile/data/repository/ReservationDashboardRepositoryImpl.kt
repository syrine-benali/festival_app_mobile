package com.example.festivalappmobile.data.repository

import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.dto.ReservationDto

// Repository léger pour le dashboard — appel direct à l'API sans passer par Room.
// On a uniquement besoin des festivalId / editeurId pour filtrer les données.
class ReservationDashboardRepositoryImpl(private val api: ApiService) {

    // Utilise getReservationsWrapped() car l'API retourne { success, data: [...], total }
    // et non une liste brute — l'ancien getReservations() échoue silencieusement sur ce format
    suspend fun getReservations(): List<ReservationDto> {
        return try {
            val response = api.getReservationsWrapped()
            if (response.isSuccessful) response.body()?.data ?: emptyList()
            else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

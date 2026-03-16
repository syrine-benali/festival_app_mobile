package com.example.festivalappmobile.data.repository

import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.dto.FestivalDto
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.repository.FestivalRepository

// service used to make the API DTO and the app dto match and compatible
class FestivalRepositoryImpl(private val apiService: ApiService) : FestivalRepository {
    override suspend fun getFestivals(): List<Festival> {
        val response = apiService.getFestivals()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                return body.map { dto ->
                    Festival(
                        id = dto.id,
                        nom = dto.nom,
                        lieu = dto.lieu,
                        dateDebut = dto.dateDebut,
                        dateFin = dto.dateFin
                    )
                }
            }
        }
        return emptyList()
    }
}

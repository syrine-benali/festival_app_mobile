package com.example.festivalappmobile.data.repository

import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.mapper.toDomain
import com.example.festivalappmobile.domain.models.Jeu
import com.example.festivalappmobile.domain.repository.JeuRepository

// Implémentation du repository Jeu — fait l'appel GET /api/jeux
class JeuRepositoryImpl(private val apiService: ApiService) : JeuRepository {

    override suspend fun getAllJeux(): List<Jeu> {
        return try {
            val response = apiService.getJeux()
            if (response.isSuccessful) {
                response.body()?.jeux?.map { it.toDomain() } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

package com.example.festivalappmobile.domain.usecases.jeu

import com.example.festivalappmobile.domain.models.Jeu
import com.example.festivalappmobile.domain.repository.JeuRepository

// Use case pour récupérer tous les jeux via GET /api/jeux
class GetJeuxUseCase(private val repository: JeuRepository) {
    suspend operator fun invoke(): List<Jeu> {
        return repository.getAllJeux()
    }
}

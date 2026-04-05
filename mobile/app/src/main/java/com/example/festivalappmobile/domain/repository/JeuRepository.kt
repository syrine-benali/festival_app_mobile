package com.example.festivalappmobile.domain.repository

import com.example.festivalappmobile.domain.models.Jeu

interface JeuRepository {
    suspend fun getAllJeux(): List<Jeu>
}

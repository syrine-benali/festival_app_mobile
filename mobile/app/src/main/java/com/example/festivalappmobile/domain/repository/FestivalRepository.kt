package com.example.festivalappmobile.domain.repository

import com.example.festivalappmobile.domain.models.Festival

interface FestivalRepository {
    // one-shot request. because festival list are not prone to change a lot,
    // no need to create a flow to have quick refresh
    suspend fun getFestivals(): List<Festival>
}

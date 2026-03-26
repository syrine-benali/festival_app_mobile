package com.example.festivalappmobile.domain.usecases.festival

import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.repository.FestivalRepository

class GetFestivalsUseCase(private val repository: FestivalRepository) {
    suspend operator fun invoke(): List<Festival> {
        return repository.getFestivals()
    }
}

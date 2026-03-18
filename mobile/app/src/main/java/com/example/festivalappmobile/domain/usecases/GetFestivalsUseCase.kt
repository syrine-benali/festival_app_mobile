package com.example.festivalappmobile.domain.usecases

import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.repository.FestivalRepository

// dedicated use case for getting festivals. having such use case 
// in domain enables decoupling between viewmodels and data layer
class GetFestivalsUseCase(private val repository: FestivalRepository) {
    suspend operator fun invoke(): List<Festival> {
        return repository.getFestivals()
    }
}

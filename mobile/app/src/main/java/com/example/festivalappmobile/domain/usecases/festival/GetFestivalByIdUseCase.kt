package com.example.festivalappmobile.domain.usecases.festival

import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.repository.FestivalRepository

class GetFestivalByIdUseCase(private val repository: FestivalRepository) {
    suspend operator fun invoke(id: Int): Festival? {
        return repository.getFestivalById(id)
    }
}

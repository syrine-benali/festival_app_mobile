package com.example.festivalappmobile.domain.usecases.festival

import com.example.festivalappmobile.domain.repository.FestivalRepository

class DeleteFestivalUseCase(private val repository: FestivalRepository) {
    suspend operator fun invoke(id: Int): Boolean {
        return repository.deleteFestival(id)
    }
}

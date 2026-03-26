package com.example.festivalappmobile.domain.usecases.festival

import com.example.festivalappmobile.data.remote.dto.FestivalCreateRequestDto
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.repository.FestivalRepository

class CreateFestivalUseCase(private val repository: FestivalRepository) {
    suspend operator fun invoke(request: FestivalCreateRequestDto): Festival? {
        return repository.createFestival(request)
    }
}

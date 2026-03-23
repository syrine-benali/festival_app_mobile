package com.example.festivalappmobile.domain.usecases.festival

import com.example.festivalappmobile.data.remote.dto.FestivalUpdateRequestDto
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.repository.FestivalRepository

class UpdateFestivalUseCase(private val repository: FestivalRepository) {
    suspend operator fun invoke(id: Int, request: FestivalUpdateRequestDto): Festival? {
        return repository.updateFestival(id, request)
    }
}

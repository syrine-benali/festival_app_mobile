package com.example.festivalappmobile.domain.repository

import com.example.festivalappmobile.data.remote.dto.FestivalCreateRequestDto
import com.example.festivalappmobile.data.remote.dto.FestivalUpdateRequestDto
import com.example.festivalappmobile.domain.models.Festival
import kotlinx.coroutines.flow.StateFlow

interface FestivalRepository {
    val festivals: StateFlow<List<Festival>>
    suspend fun getFestivals(): List<Festival>
    suspend fun getFestivalById(id: Int): Festival?
    suspend fun createFestival(request: FestivalCreateRequestDto): Festival?
    suspend fun updateFestival(id: Int, request: FestivalUpdateRequestDto): Festival?
    suspend fun deleteFestival(id: Int): Boolean
}

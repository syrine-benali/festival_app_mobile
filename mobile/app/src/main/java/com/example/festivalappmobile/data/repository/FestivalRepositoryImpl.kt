package com.example.festivalappmobile.data.repository

import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.dto.FestivalCreateRequestDto
import com.example.festivalappmobile.data.remote.dto.FestivalUpdateRequestDto
import com.example.festivalappmobile.data.remote.mapper.toDomain
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.repository.FestivalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FestivalRepositoryImpl(private val apiService: ApiService) : FestivalRepository {

    private val _festivals = MutableStateFlow<List<Festival>>(emptyList())
    override val festivals: StateFlow<List<Festival>> = _festivals.asStateFlow()

    override suspend fun getFestivals(): List<Festival> {
        return try {
            val response = apiService.getFestivals()
            if (response.isSuccessful) {
                val list = response.body()?.map { it.toDomain() } ?: emptyList()
                _festivals.value = list
                list
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getFestivalById(id: Int): Festival? {
        return try {
            val response = apiService.getFestivalById(id)
            if (response.isSuccessful) {
                response.body()?.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createFestival(request: FestivalCreateRequestDto): Festival? {
        return try {
            val response = apiService.createFestival(request)
            if (response.isSuccessful) {
                val result = response.body()?.toDomain()
                if (result != null) {
                    getFestivals() // Trigger refresh
                }
                result
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateFestival(id: Int, request: FestivalUpdateRequestDto): Festival? {
        return try {
            val response = apiService.updateFestival(id, request)
            if (response.isSuccessful) {
                val result = response.body()?.toDomain()
                if (result != null) {
                    getFestivals() // Trigger refresh
                }
                result
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteFestival(id: Int): Boolean {
        return try {
            val response = apiService.deleteFestival(id)
            if (response.isSuccessful) {
                getFestivals() // Trigger refresh
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}

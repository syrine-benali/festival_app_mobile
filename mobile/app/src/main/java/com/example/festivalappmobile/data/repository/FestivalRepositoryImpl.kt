package com.example.festivalappmobile.data.repository

import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.dto.FestivalCreateRequestDto
import com.example.festivalappmobile.data.remote.dto.FestivalUpdateRequestDto
import com.example.festivalappmobile.data.remote.mapper.toDomain
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.repository.FestivalRepository

class FestivalRepositoryImpl(private val apiService: ApiService) : FestivalRepository {

    override suspend fun getFestivals(): List<Festival> {
        return try {
            val response = apiService.getFestivals()
            if (response.isSuccessful) {
                response.body()?.map { it.toDomain() } ?: emptyList()
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
                response.body()?.toDomain()
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
                response.body()?.toDomain()
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
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}

package com.example.festivalappmobile.data.repository

import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.dto.EditeurCreateRequestDto
import com.example.festivalappmobile.data.remote.dto.EditeurUpdateRequestDto
import com.example.festivalappmobile.data.remote.mapper.toDomain
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.repository.EditeurRepository

// Repository class in charge of dispatching api calls to api service.
class EditeurRepositoryImpl(private val apiService: ApiService) : EditeurRepository {
    
    override suspend fun getAllEditeurs(): List<Editeur> {
        return try {
            val response = apiService.getAllEditeurs()
            if (response.isSuccessful) {
                response.body()?.editeurs?.map { it.toDomain() } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getEditeurById(id: Int): Editeur? {
        return try {
            val response = apiService.getEditeurById(id)
            if (response.isSuccessful) {
                response.body()?.editeur?.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createEditeur(request: EditeurCreateRequestDto): Editeur? {
        return try {
            val response = apiService.createEditeur(request)
            if (response.isSuccessful) {
                response.body()?.editeur?.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateEditeur(id: Int, request: EditeurUpdateRequestDto): Editeur? {
        return try {
            val response = apiService.updateEditeur(id, request)
            if (response.isSuccessful) {
                response.body()?.editeur?.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteEditeur(id: Int): Boolean {
        return try {
            val response = apiService.deleteEditeur(id)
            response.isSuccessful && (response.body()?.success ?: false)
        } catch (e: Exception) {
            false
        }
    }
}

package com.example.festivalappmobile.data.repository

import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.dto.EditeurCreateRequestDto
import com.example.festivalappmobile.data.remote.dto.EditeurUpdateRequestDto
import com.example.festivalappmobile.data.remote.mapper.toDomain
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.repository.EditeurRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Repository class in charge of dispatching api calls to api service.
class EditeurRepositoryImpl(private val apiService: ApiService) : EditeurRepository {
    
    private val _editeurs = MutableStateFlow<List<Editeur>>(emptyList())
    override val editeurs: StateFlow<List<Editeur>> = _editeurs.asStateFlow()
    override suspend fun getAllEditeurs(): List<Editeur> {
        return try {
            val response = apiService.getAllEditeurs()
            if (response.isSuccessful) {
                val editeursList = response.body()?.editeurs?.map { it.toDomain() } ?: emptyList()
                _editeurs.value = editeursList
                editeursList
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
                val createdEditeur = response.body()?.editeur?.toDomain()
                if (createdEditeur != null) {
                    // Update the flow by adding the new editor or re-fetching everything
                    // Re-fetching ensures consistent data from backend
                    getAllEditeurs()
                }
                createdEditeur
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
            val success = response.isSuccessful && (response.body()?.success ?: false)
            if (success) {
                getAllEditeurs()
            }
            success
        } catch (e: Exception) {
            false
        }
    }
}

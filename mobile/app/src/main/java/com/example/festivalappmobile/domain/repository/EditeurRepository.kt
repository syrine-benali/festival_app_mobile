package com.example.festivalappmobile.domain.repository

import com.example.festivalappmobile.domain.models.Editeur
import kotlinx.coroutines.flow.StateFlow
import com.example.festivalappmobile.data.remote.dto.EditeurCreateRequestDto
import com.example.festivalappmobile.data.remote.dto.EditeurUpdateRequestDto

// Editeur api endpoints interface
interface EditeurRepository {
    val editeurs: StateFlow<List<Editeur>>
    suspend fun getAllEditeurs(): List<Editeur>
    suspend fun getEditeurById(id: Int): Editeur?
    suspend fun createEditeur(request: EditeurCreateRequestDto): Editeur?
    suspend fun updateEditeur(id: Int, request: EditeurUpdateRequestDto): Editeur?
    suspend fun deleteEditeur(id: Int): Boolean
}

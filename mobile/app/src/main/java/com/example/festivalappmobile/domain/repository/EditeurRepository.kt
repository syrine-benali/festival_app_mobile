package com.example.festivalappmobile.domain.repository

import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.data.remote.dto.EditeurCreateRequestDto
import com.example.festivalappmobile.data.remote.dto.EditeurUpdateRequestDto

// Editeur api endpoints interface
interface EditeurRepository {
    suspend fun getAllEditeurs(): List<Editeur>
    suspend fun getEditeurById(id: Int): Editeur?
    suspend fun createEditeur(request: EditeurCreateRequestDto): Editeur?
    suspend fun updateEditeur(id: Int, request: EditeurUpdateRequestDto): Editeur?
    suspend fun deleteEditeur(id: Int): Boolean
}

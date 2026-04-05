package com.example.festivalappmobile.domain.usecases.editeur

import com.example.festivalappmobile.data.remote.dto.EditeurCreateRequestDto
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.repository.EditeurRepository

/**
 * Use case to create a new editeur.
 */
class CreateEditeurUseCase(private val repository: EditeurRepository) {
    suspend operator fun invoke(request: EditeurCreateRequestDto): Editeur? {
        return repository.createEditeur(request)
    }
}

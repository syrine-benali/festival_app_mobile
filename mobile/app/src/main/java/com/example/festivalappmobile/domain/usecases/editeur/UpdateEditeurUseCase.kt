package com.example.festivalappmobile.domain.usecases.editeur

import com.example.festivalappmobile.data.remote.dto.EditeurUpdateRequestDto
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.repository.EditeurRepository

/**
 * Use case to update an existing editeur.
 */
class UpdateEditeurUseCase(private val repository: EditeurRepository) {
    suspend operator fun invoke(id: Int, request: EditeurUpdateRequestDto): Editeur? {
        return repository.updateEditeur(id, request)
    }
}

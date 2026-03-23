package com.example.festivalappmobile.domain.usecases.editeur

import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.repository.EditeurRepository

/**
 * Use case to retrieve a single editeur by its unique identifier.
 */
class GetEditeurByIdUseCase(private val repository: EditeurRepository) {
    suspend operator fun invoke(id: Int): Editeur? {
        return repository.getEditeurById(id)
    }
}

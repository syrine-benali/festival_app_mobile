package com.example.festivalappmobile.domain.usecases.editeur

import com.example.festivalappmobile.domain.repository.EditeurRepository

/**
 * Use case to delete an existing editeur.
 */
class DeleteEditeurUseCase(private val repository: EditeurRepository) {
    suspend operator fun invoke(id: Int): Boolean {
        return repository.deleteEditeur(id)
    }
}

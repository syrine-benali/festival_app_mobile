package com.example.festivalappmobile.domain.usecases.editeur

import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.repository.EditeurRepository

/**
 * Use case to retrieve all editeurs from the repository.
 */
class GetEditeursUseCase(private val repository: EditeurRepository) {
    suspend operator fun invoke(): List<Editeur> {
        return repository.getAllEditeurs()
    }
}

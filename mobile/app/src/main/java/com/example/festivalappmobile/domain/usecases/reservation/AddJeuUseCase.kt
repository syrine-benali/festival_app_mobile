package com.example.festivalappmobile.domain.usecases.reservation

import com.example.festivalappmobile.domain.models.ReservationJeu
import com.example.festivalappmobile.domain.repository.ReservationRepository

class AddJeuUseCase(private val repo: ReservationRepository) {
    suspend operator fun invoke(
        reservationId: Int,
        jeuId: Int,
        nbExemplaires: Int,
        nbTablesAllouees: Int,
        placementId: Int? = null
    ): Result<ReservationJeu> {
        if (nbExemplaires <= 0)
            return Result.failure(Exception("Le nombre d'exemplaires doit être positif"))
        if (nbTablesAllouees < 0)
            return Result.failure(Exception("Le nombre de tables ne peut pas être négatif"))
        // Règle métier : 1 jeu ne peut pas occuper 2 tables seul
        if (nbTablesAllouees == 2 && nbExemplaires == 1)
            return Result.failure(Exception("Un seul exemplaire ne peut pas occuper 2 tables"))
        return repo.addJeu(reservationId, jeuId, nbExemplaires, nbTablesAllouees, placementId)
    }
}
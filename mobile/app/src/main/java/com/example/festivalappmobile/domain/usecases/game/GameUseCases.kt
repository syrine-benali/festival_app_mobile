package com.example.festivalappmobile.domain.usecases.game

import com.example.festivalappmobile.domain.models.Game
import com.example.festivalappmobile.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow

class GetAllGamesUseCase(private val repository: GameRepository) {
    operator fun invoke(): Flow<Result<List<Game>>> {
        return repository.getGames()
    }
}

class CreateGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(game: Game): Result<Game> {
        return repository.createGame(game)
    }
}

class UpdateGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(game: Game): Result<Game> {
        return repository.updateGame(game)
    }
}

class DeleteGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(id: Int): Result<Unit> {
        return repository.deleteGame(id)
    }
}

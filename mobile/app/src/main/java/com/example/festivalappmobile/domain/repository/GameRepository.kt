package com.example.festivalappmobile.domain.repository

import com.example.festivalappmobile.domain.models.Game
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getGames(): Flow<Result<List<Game>>>
    suspend fun getGameById(id: Int): Result<Game>
    suspend fun createGame(game: Game): Result<Game>
    suspend fun updateGame(game: Game): Result<Game>
    suspend fun deleteGame(id: Int): Result<Unit>
}

package com.example.festivalappmobile.data.repository

import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.mapper.toCreateRequest
import com.example.festivalappmobile.data.remote.mapper.toDomain
import com.example.festivalappmobile.data.remote.mapper.toGame
import com.example.festivalappmobile.data.remote.mapper.toUpdateRequest
import com.example.festivalappmobile.domain.models.Game
import com.example.festivalappmobile.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GameRepositoryImpl(private val apiService: ApiService) : GameRepository {

    override fun getGames(): Flow<Result<List<Game>>> = flow {
        try {
            val response = apiService.getJeux()
            if (response.isSuccessful) {
                val games = response.body()?.jeux?.map { it.toGame() } ?: emptyList()
                emit(Result.success(games))
            } else {
                emit(Result.failure(Exception("Failed to fetch games: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun getGamesByEditeur(editeurId: Int): Result<List<Game>> {
        // Essai 1 : endpoint dédié api/jeux/editeur/{editeurId}
        try {
            val response = apiService.getGamesByEditeur(editeurId)
            if (response.isSuccessful) {
                val games = response.body()?.jeux?.map { it.toDomain() } ?: emptyList()
                // Si la liste n'est pas vide on la retourne, sinon on essaie le fallback
                if (games.isNotEmpty()) return Result.success(games)
            }
        } catch (_: Exception) { /* on tombe sur le fallback */ }

        // Essai 2 (fallback) : récupérer tous les jeux via api/jeux et filtrer côté client
        return try {
            val allResponse = apiService.getJeux()
            if (allResponse.isSuccessful) {
                val games = allResponse.body()?.jeux
                    ?.filter { it.editeur?.id == editeurId }
                    ?.map { it.toGame() }
                    ?: emptyList()
                Result.success(games)
            } else {
                Result.failure(Exception("Impossible de charger les jeux (${allResponse.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGameById(id: Int): Result<Game> {
        return try {
            val response = apiService.getJeux()
            if (response.isSuccessful) {
                val game = response.body()?.jeux?.find { it.id == id }?.toGame()
                if (game != null) {
                    Result.success(game)
                } else {
                    Result.failure(Exception("Game with id $id not found"))
                }
            } else {
                Result.failure(Exception("Failed to fetch games: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createGame(game: Game): Result<Game> {
        return try {
            val response = apiService.createGame(game.toCreateRequest())
            if (response.isSuccessful) {
                val createdGame = response.body()?.jeu?.toDomain()
                if (createdGame != null) {
                    Result.success(createdGame)
                } else {
                    Result.failure(Exception("Created game is null"))
                }
            } else {
                Result.failure(Exception("Failed to create game: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGame(game: Game): Result<Game> {
        return try {
            val response = apiService.updateGame(game.id, game.toUpdateRequest())
            if (response.isSuccessful) {
                val updatedGame = response.body()?.jeu?.toDomain()
                if (updatedGame != null) {
                    Result.success(updatedGame)
                } else {
                    Result.failure(Exception("Updated game is null"))
                }
            } else {
                Result.failure(Exception("Failed to update game: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGame(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteGame(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete game: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

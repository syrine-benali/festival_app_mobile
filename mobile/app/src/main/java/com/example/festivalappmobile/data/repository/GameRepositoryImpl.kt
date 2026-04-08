package com.example.festivalappmobile.data.repository

import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.mapper.toCreateRequest
import com.example.festivalappmobile.data.remote.mapper.toDomain
import com.example.festivalappmobile.data.remote.mapper.toUpdateRequest
import com.example.festivalappmobile.domain.models.Game
import com.example.festivalappmobile.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GameRepositoryImpl(private val apiService: ApiService) : GameRepository {

    override fun getGames(): Flow<Result<List<Game>>> = flow {
        try {
            val response = apiService.getAllGames()
            if (response.isSuccessful) {
                val games = response.body()?.jeux?.map { it.toDomain() } ?: emptyList()
                emit(Result.success(games))
            } else {
                emit(Result.failure(Exception("Failed to fetch games: ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun getGamesByEditeur(editeurId: Int): Result<List<Game>> {
        return try {
            val response = apiService.getGamesByEditeur(editeurId)
            if (response.isSuccessful) {
                val games = response.body()?.jeux?.map { it.toDomain() } ?: emptyList()
                Result.success(games)
            } else {
                Result.failure(Exception("Failed to fetch editor games: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGameById(id: Int): Result<Game> {
        return try {
            val response = apiService.getAllGames()
            if (response.isSuccessful) {
                val game = response.body()?.jeux?.find { it.id == id }?.toDomain()
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

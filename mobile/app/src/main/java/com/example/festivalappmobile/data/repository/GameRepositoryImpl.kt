package com.example.festivalappmobile.data.repository

import com.example.festivalappmobile.data.local.dao.GameDao
import com.example.festivalappmobile.data.local.entity.toDomain
import com.example.festivalappmobile.data.local.entity.toEntity
import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.mapper.toCreateRequest
import com.example.festivalappmobile.data.remote.mapper.toDomain
import com.example.festivalappmobile.data.remote.mapper.toUpdateRequest
import com.example.festivalappmobile.domain.models.Game
import com.example.festivalappmobile.domain.repository.GameRepository
import com.example.festivalappmobile.utils.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Implémentation offline-first de [GameRepository].
 *
 * [getGames] retourne un Flow qui :
 * 1. Émet immédiatement les données du cache Room (disponible même hors ligne).
 * 2. Si internet disponible, rafraîchit depuis l'API puis émet une seconde fois.
 *
 * Cela garantit une réponse rapide (cache local) + données fraîches (API).
 */
class GameRepositoryImpl(
    private val apiService: ApiService,
    private val gameDao: GameDao,
    private val networkMonitor: NetworkMonitor
) : GameRepository {

    // ──────────────────────────────────────────────────────────────────────────
    // Lecture principale — offline-first Flow
    // ──────────────────────────────────────────────────────────────────────────

    override fun getGames(): Flow<Result<List<Game>>> = flow {
        // 1. Émettre depuis Room en premier (réponse immédiate, fonctionne hors ligne)
        val cached = gameDao.getAll().first().map { it.toDomain() }
        if (cached.isNotEmpty()) {
            emit(Result.success(cached))
        }

        // 2. Si online, rafraîchir depuis l'API
        if (networkMonitor.isCurrentlyOnline()) {
            try {
                val response = apiService.getAllGames()
                if (response.isSuccessful) {
                    val games = response.body()?.jeux?.map { it.toDomain() } ?: emptyList()
                    if (games.isNotEmpty()) {
                        gameDao.upsertAll(games.map { it.toEntity() })
                        gameDao.deleteNotInIds(games.map { it.id })
                        emit(Result.success(games))
                    } else if (cached.isEmpty()) {
                        emit(Result.success(emptyList()))
                    }
                } else {
                    if (cached.isEmpty()) {
                        emit(Result.failure(Exception("Impossible de charger les jeux : ${response.message()}")))
                    }
                }
            } catch (e: Exception) {
                if (cached.isEmpty()) {
                    emit(Result.failure(e))
                }
            }
        } else if (cached.isEmpty()) {
            // Hors ligne et aucun cache disponible
            emit(Result.failure(Exception("Pas de connexion réseau et aucune donnée en cache")))
        }
    }

    override suspend fun getGamesByEditeur(editeurId: Int): Result<List<Game>> {
        return if (networkMonitor.isCurrentlyOnline()) {
            try {
                val response = apiService.getGamesByEditeur(editeurId)
                if (response.isSuccessful) {
                    val games = response.body()?.jeux?.map { it.toDomain() } ?: emptyList()
                    Result.success(games)
                } else {
                    // Fallback sur le cache local pour cet éditeur
                    Result.success(gameDao.getByEditeur(editeurId).map { it.toDomain() })
                }
            } catch (e: Exception) {
                Result.success(gameDao.getByEditeur(editeurId).map { it.toDomain() })
            }
        } else {
            Result.success(gameDao.getByEditeur(editeurId).map { it.toDomain() })
        }
    }

    override suspend fun getGameById(id: Int): Result<Game> {
        return if (networkMonitor.isCurrentlyOnline()) {
            try {
                // L'API n'a pas d'endpoint par id, on cherche dans la liste complète puis le cache
                val response = apiService.getAllGames()
                if (response.isSuccessful) {
                    val game = response.body()?.jeux?.find { it.id == id }?.toDomain()
                    if (game != null) {
                        gameDao.upsert(game.toEntity())
                        Result.success(game)
                    } else {
                        Result.failure(Exception("Jeu avec l'id $id introuvable"))
                    }
                } else {
                    gameDao.getById(id)?.toDomain()?.let { Result.success(it) }
                        ?: Result.failure(Exception("Jeu introuvable"))
                }
            } catch (e: Exception) {
                gameDao.getById(id)?.toDomain()?.let { Result.success(it) }
                    ?: Result.failure(e)
            }
        } else {
            gameDao.getById(id)?.toDomain()?.let { Result.success(it) }
                ?: Result.failure(Exception("Pas de connexion réseau"))
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Écriture
    // ──────────────────────────────────────────────────────────────────────────

    override suspend fun createGame(game: Game): Result<Game> {
        return try {
            val response = apiService.createGame(game.toCreateRequest())
            if (response.isSuccessful) {
                val created = response.body()?.jeu?.toDomain()
                if (created != null) {
                    gameDao.upsert(created.toEntity())
                    Result.success(created)
                } else {
                    Result.failure(Exception("Le jeu créé est null"))
                }
            } else {
                Result.failure(Exception("Échec création : ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGame(game: Game): Result<Game> {
        return try {
            val response = apiService.updateGame(game.id, game.toUpdateRequest())
            if (response.isSuccessful) {
                val updated = response.body()?.jeu?.toDomain()
                if (updated != null) {
                    gameDao.upsert(updated.toEntity())
                    Result.success(updated)
                } else {
                    Result.failure(Exception("Le jeu mis à jour est null"))
                }
            } else {
                Result.failure(Exception("Échec mise à jour : ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGame(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteGame(id)
            if (response.isSuccessful) {
                gameDao.deleteById(id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Échec suppression : ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

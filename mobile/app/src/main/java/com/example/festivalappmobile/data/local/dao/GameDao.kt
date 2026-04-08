package com.example.festivalappmobile.data.local.dao

import androidx.room.*
import com.example.festivalappmobile.data.local.entity.GameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    /** Retourne tous les jeux en temps réel (observable). */
    @Query("SELECT * FROM games ORDER BY libelle ASC")
    fun getAll(): Flow<List<GameEntity>>

    /** Retourne un jeu par son id. */
    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getById(id: Int): GameEntity?

    /** Retourne tous les jeux d'un éditeur. */
    @Query("SELECT * FROM games WHERE idEditeur = :editeurId ORDER BY libelle ASC")
    suspend fun getByEditeur(editeurId: Int): List<GameEntity>

    /** Insert ou met à jour une liste de jeux. */
    @Upsert
    suspend fun upsertAll(games: List<GameEntity>)

    /** Insert ou met à jour un seul jeu. */
    @Upsert
    suspend fun upsert(game: GameEntity)

    /**
     * Supprime les jeux dont l'id n'est pas dans la liste.
     * Appeler seulement quand [ids] est non vide.
     */
    @Query("DELETE FROM games WHERE id NOT IN (:ids)")
    suspend fun deleteNotInIds(ids: List<Int>)

    /** Supprime tous les jeux du cache. */
    @Query("DELETE FROM games")
    suspend fun deleteAll()

    /** Supprime un jeu par son id. */
    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteById(id: Int)
}

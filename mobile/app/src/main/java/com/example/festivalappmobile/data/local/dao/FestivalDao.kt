package com.example.festivalappmobile.data.local.dao

import androidx.room.*
import com.example.festivalappmobile.data.local.entity.FestivalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FestivalDao {

    /** Retourne tous les festivals en temps réel (observable). */
    @Query("SELECT * FROM festivals ORDER BY dateDebut DESC")
    fun getAll(): Flow<List<FestivalEntity>>

    /** Retourne un festival par son id (lecture unique). */
    @Query("SELECT * FROM festivals WHERE id = :id")
    suspend fun getById(id: Int): FestivalEntity?

    /** Insert ou met à jour une liste de festivals (upsert). */
    @Upsert
    suspend fun upsertAll(festivals: List<FestivalEntity>)

    /** Insert ou met à jour un seul festival. */
    @Upsert
    suspend fun upsert(festival: FestivalEntity)

    /**
     * Supprime les festivals dont l'id n'est pas dans la liste fournie.
     * Appeler seulement quand [ids] est non vide pour éviter une suppression totale.
     */
    @Query("DELETE FROM festivals WHERE id NOT IN (:ids)")
    suspend fun deleteNotInIds(ids: List<Int>)

    /** Supprime tous les festivals du cache. */
    @Query("DELETE FROM festivals")
    suspend fun deleteAll()

    /** Supprime un festival par son id. */
    @Query("DELETE FROM festivals WHERE id = :id")
    suspend fun deleteById(id: Int)
}

package com.example.festivalappmobile.data.local.dao

import androidx.room.*
import com.example.festivalappmobile.data.local.entity.EditeurEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EditeurDao {

    /** Retourne tous les éditeurs en temps réel (observable). */
    @Query("SELECT * FROM editeurs ORDER BY libelle ASC")
    fun getAll(): Flow<List<EditeurEntity>>

    /** Retourne un éditeur par son id. */
    @Query("SELECT * FROM editeurs WHERE id = :id")
    suspend fun getById(id: Int): EditeurEntity?

    /** Insert ou met à jour une liste d'éditeurs. */
    @Upsert
    suspend fun upsertAll(editeurs: List<EditeurEntity>)

    /** Insert ou met à jour un seul éditeur. */
    @Upsert
    suspend fun upsert(editeur: EditeurEntity)

    /**
     * Supprime les éditeurs dont l'id n'est pas dans la liste.
     * Appeler seulement quand [ids] est non vide.
     */
    @Query("DELETE FROM editeurs WHERE id NOT IN (:ids)")
    suspend fun deleteNotInIds(ids: List<Int>)

    /** Supprime tous les éditeurs du cache. */
    @Query("DELETE FROM editeurs")
    suspend fun deleteAll()

    /** Supprime un éditeur par son id. */
    @Query("DELETE FROM editeurs WHERE id = :id")
    suspend fun deleteById(id: Int)
}

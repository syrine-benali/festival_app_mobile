package com.example.festivalappmobile.data.repository

import com.example.festivalappmobile.data.local.dao.EditeurDao
import com.example.festivalappmobile.data.local.entity.toDomain
import com.example.festivalappmobile.data.local.entity.toEntity
import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.dto.EditeurCreateRequestDto
import com.example.festivalappmobile.data.remote.dto.EditeurUpdateRequestDto
import com.example.festivalappmobile.data.remote.mapper.toDomain
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.repository.EditeurRepository
import com.example.festivalappmobile.utils.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/**
 * Implémentation offline-first de [EditeurRepository].
 *
 * Stratégie Single Source of Truth :
 * - Online  → fetch API → upsert Room → met à jour StateFlow
 * - Offline → lit Room → met à jour StateFlow
 */
class EditeurRepositoryImpl(
    private val apiService: ApiService,
    private val editeurDao: EditeurDao,
    private val networkMonitor: NetworkMonitor
) : EditeurRepository {

    private val _editeurs = MutableStateFlow<List<Editeur>>(emptyList())
    override val editeurs: StateFlow<List<Editeur>> = _editeurs.asStateFlow()

    // ──────────────────────────────────────────────────────────────────────────
    // Lecture principale
    // ──────────────────────────────────────────────────────────────────────────

    override suspend fun getAllEditeurs(): List<Editeur> {
        return if (networkMonitor.isCurrentlyOnline()) {
            fetchFromApiAndCache()
        } else {
            loadFromRoom()
        }
    }

    override suspend fun getEditeurById(id: Int): Editeur? {
        return if (networkMonitor.isCurrentlyOnline()) {
            try {
                val response = apiService.getEditeurById(id)
                if (response.isSuccessful) {
                    val editeur = response.body()?.editeur?.toDomain()
                    if (editeur != null) {
                        editeurDao.upsert(editeur.toEntity())
                    }
                    editeur
                } else {
                    editeurDao.getById(id)?.toDomain()
                }
            } catch (e: Exception) {
                editeurDao.getById(id)?.toDomain()
            }
        } else {
            editeurDao.getById(id)?.toDomain()
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Écriture
    // ──────────────────────────────────────────────────────────────────────────

    override suspend fun createEditeur(request: EditeurCreateRequestDto): Editeur? {
        return try {
            val response = apiService.createEditeur(request)
            if (response.isSuccessful) {
                val created = response.body()?.editeur?.toDomain()
                if (created != null) {
                    editeurDao.upsert(created.toEntity())
                    fetchFromApiAndCache()   // Rafraîchit la liste complète
                }
                created
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateEditeur(id: Int, request: EditeurUpdateRequestDto): Editeur? {
        return try {
            val response = apiService.updateEditeur(id, request)
            if (response.isSuccessful) {
                val updated = response.body()?.editeur?.toDomain()
                if (updated != null) {
                    editeurDao.upsert(updated.toEntity())
                    _editeurs.value = editeurDao.getAll().first().map { it.toDomain() }
                }
                updated
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteEditeur(id: Int): Boolean {
        return try {
            val response = apiService.deleteEditeur(id)
            val success = response.isSuccessful && (response.body()?.success ?: false)
            if (success) {
                editeurDao.deleteById(id)
                _editeurs.value = editeurDao.getAll().first().map { it.toDomain() }
            }
            success
        } catch (e: Exception) {
            false
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers privés
    // ──────────────────────────────────────────────────────────────────────────

    private suspend fun fetchFromApiAndCache(): List<Editeur> {
        return try {
            val response = apiService.getAllEditeurs()
            if (response.isSuccessful) {
                val list = response.body()?.editeurs?.map { it.toDomain() } ?: emptyList()
                if (list.isNotEmpty()) {
                    editeurDao.upsertAll(list.map { it.toEntity() })
                    editeurDao.deleteNotInIds(list.map { it.id })
                }
                _editeurs.value = list
                list
            } else {
                loadFromRoom()
            }
        } catch (e: Exception) {
            loadFromRoom()
        }
    }

    private suspend fun loadFromRoom(): List<Editeur> {
        val cached = editeurDao.getAll().first().map { it.toDomain() }
        _editeurs.value = cached
        return cached
    }
}

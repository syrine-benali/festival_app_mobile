package com.example.festivalappmobile.data.repository

import com.example.festivalappmobile.data.local.dao.FestivalDao
import com.example.festivalappmobile.data.local.entity.toDomain
import com.example.festivalappmobile.data.local.entity.toEntity
import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.dto.FestivalCreateRequestDto
import com.example.festivalappmobile.data.remote.dto.FestivalUpdateRequestDto
import com.example.festivalappmobile.data.remote.mapper.toDomain
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.repository.FestivalRepository
import com.example.festivalappmobile.utils.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/**
 * Implémentation offline-first de [FestivalRepository].
 *
 * Stratégie Single Source of Truth :
 * - Room est la source principale de données pour l'UI.
 * - Quand internet est disponible : fetch API → upsert Room → StateFlow se met à jour.
 * - Quand hors ligne : lit directement depuis Room.
 * - La suppression côté API se répercute sur le cache local.
 */
class FestivalRepositoryImpl(
    private val apiService: ApiService,
    private val festivalDao: FestivalDao,
    private val networkMonitor: NetworkMonitor
) : FestivalRepository {

    private val _festivals = MutableStateFlow<List<Festival>>(emptyList())
    override val festivals: StateFlow<List<Festival>> = _festivals.asStateFlow()

    // ──────────────────────────────────────────────────────────────────────────
    // Lecture principale
    // ──────────────────────────────────────────────────────────────────────────

    override suspend fun getFestivals(): List<Festival> {
        return if (networkMonitor.isCurrentlyOnline()) {
            fetchFromApiAndCache()
        } else {
            loadFromRoom()
        }
    }

    override suspend fun getFestivalById(id: Int): Festival? {
        return if (networkMonitor.isCurrentlyOnline()) {
            try {
                val response = apiService.getFestivalById(id)
                if (response.isSuccessful) {
                    val festival = response.body()?.toDomain()
                    if (festival != null) {
                        festivalDao.upsert(festival.toEntity())
                    }
                    festival
                } else {
                    festivalDao.getById(id)?.toDomain()
                }
            } catch (e: Exception) {
                festivalDao.getById(id)?.toDomain()
            }
        } else {
            festivalDao.getById(id)?.toDomain()
        }
    }



    // ──────────────────────────────────────────────────────────────────────────
    // Écriture (requiert internet)
    // ──────────────────────────────────────────────────────────────────────────

    override suspend fun createFestival(request: FestivalCreateRequestDto): Festival? {
        return try {
            val response = apiService.createFestival(request)
            if (response.isSuccessful) {
                val result = response.body()?.toDomain()
                if (result != null) {
                    festivalDao.upsert(result.toEntity())
                    fetchFromApiAndCache()          // Rafraîchit le cache complet
                }
                result
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateFestival(id: Int, request: FestivalUpdateRequestDto): Festival? {
        return try {
            val response = apiService.updateFestival(id, request)
            if (response.isSuccessful) {
                val result = response.body()?.toDomain()
                if (result != null) {
                    festivalDao.upsert(result.toEntity())
                    _festivals.value = festivalDao.getAll().first().map { it.toDomain() }
                }
                result
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteFestival(id: Int): Boolean {
        return try {
            val response = apiService.deleteFestival(id)
            if (response.isSuccessful) {
                festivalDao.deleteById(id)
                _festivals.value = festivalDao.getAll().first().map { it.toDomain() }
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers privés
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Récupère depuis l'API, met à jour Room (upsert + suppression des orphelins),
     * puis met à jour le StateFlow.
     */
    private suspend fun fetchFromApiAndCache(): List<Festival> {
        return try {
            val response = apiService.getFestivals()
            if (response.isSuccessful) {
                val list = response.body()?.map { it.toDomain() } ?: emptyList()
                if (list.isNotEmpty()) {
                    festivalDao.upsertAll(list.map { it.toEntity() })
                    festivalDao.deleteNotInIds(list.map { it.id })
                }
                _festivals.value = list
                list
            } else {
                loadFromRoom()
            }
        } catch (e: Exception) {
            loadFromRoom()
        }
    }

    /**
     * Charge depuis Room et met à jour le StateFlow.
     * Utilisé en mode offline ou en cas d'erreur réseau.
     */
    private suspend fun loadFromRoom(): List<Festival> {
        val cached = festivalDao.getAll().first().map { it.toDomain() }
        _festivals.value = cached
        return cached
    }
}

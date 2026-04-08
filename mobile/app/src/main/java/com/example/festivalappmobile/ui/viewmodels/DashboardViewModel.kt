package com.example.festivalappmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.data.repository.DashboardCacheRepository
import com.example.festivalappmobile.data.repository.ReservationDashboardRepositoryImpl
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.models.Jeu
import com.example.festivalappmobile.domain.usecases.editeur.GetEditeursUseCase
import com.example.festivalappmobile.domain.usecases.festival.GetFestivalsUseCase
import com.example.festivalappmobile.domain.usecases.jeu.GetJeuxUseCase
import com.example.festivalappmobile.utils.NetworkChecker
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DashboardUiState(
    val festivals: List<Festival> = emptyList(),
    val jeux: List<Jeu> = emptyList(),
    val editeurs: List<Editeur> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    // Indique si aucun festival actif n'a été trouvé
    val noActiveFestival: Boolean = false,
    // Indique si les données viennent du cache local (mode hors ligne)
    val isOffline: Boolean = false
)

class DashboardViewModel(
    private val getFestivalsUseCase: GetFestivalsUseCase,
    private val getJeuxUseCase: GetJeuxUseCase,
    private val getEditeursUseCase: GetEditeursUseCase,
    private val reservationRepo: ReservationDashboardRepositoryImpl,
    private val cacheRepo: DashboardCacheRepository,
    private val networkChecker: NetworkChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState(isLoading = true)

            // ── Pas de connexion : on charge depuis le cache Room ─────────────
            if (!networkChecker.isConnected()) {
                loadFromCache()
                return@launch
            }

            // ── En ligne : appel API, filtrage, puis sauvegarde dans le cache ─
            try {
                val festivalsDeferred    = async { getFestivalsUseCase() }
                val reservationsDeferred = async { reservationRepo.getReservations() }
                val jeuxDeferred         = async { getJeuxUseCase() }
                val editeursDeferred     = async { getEditeursUseCase() }

                val allFestivals    = festivalsDeferred.await()
                val allReservations = reservationsDeferred.await()
                val allJeux         = jeuxDeferred.await()
                val allEditeurs     = editeursDeferred.await()

                // ── Pas de festivals du tout ──────────────────────────────────────
                if (allFestivals.isEmpty()) {
                    cacheRepo.saveFestivals(emptyList())
                    cacheRepo.saveJeux(emptyList())
                    cacheRepo.saveEditeurs(emptyList())
                    _uiState.value = DashboardUiState(noActiveFestival = true, isOffline = false)
                    return@launch
                }

                // ── Tri des festivals : actifs aujourd'hui d'abord, puis à venir, puis passés ──
                val sortedFestivals = allFestivals.sortedWith(
                    compareByDescending<Festival> { isActiveToday(it) }
                        .thenBy {
                            try { LocalDate.parse(it.dateDebut.take(10)) }
                            catch (_: Exception) { LocalDate.MAX }
                        }
                )

                // ── IDs des festivals actifs aujourd'hui (pour filtrer jeux/éditeurs) ──
                val activeFestivalIds = allFestivals
                    .filter { isActiveToday(it) }
                    .map { it.id }
                    .toSet()

                // ── Réservations de référence ──────────────────────────────────────────
                // Si des festivals sont actifs aujourd'hui → on utilise leurs réservations
                // Sinon → on utilise toutes les réservations (à venir ou passées)
                val reservationsDeReference = if (activeFestivalIds.isNotEmpty()) {
                    allReservations.filter { it.festivalId in activeFestivalIds }
                } else {
                    allReservations
                }

                // ── IDs éditeurs présents ─────────────────────────────────────────────
                val editeurIdsPresents: Set<Int> = if (reservationsDeReference.isNotEmpty()) {
                    reservationsDeReference.map { it.editeurId }.toSet()
                } else {
                    // Aucune réservation disponible → tous les éditeurs ayant déjà réservé
                    allEditeurs.filter { it.hasReservation }.map { it.id }.toSet()
                }

                val filteredEditeurs = allEditeurs.filter { it.id in editeurIdsPresents }
                val filteredJeux     = allJeux.filter { it.editeurId in editeurIdsPresents }

                // ── Sauvegarde dans Room pour le mode hors-ligne ──────────────────────
                cacheRepo.saveFestivals(sortedFestivals)
                cacheRepo.saveJeux(filteredJeux)
                cacheRepo.saveEditeurs(filteredEditeurs)

                _uiState.value = DashboardUiState(
                    festivals     = sortedFestivals,
                    jeux          = filteredJeux,
                    editeurs      = filteredEditeurs,
                    // true seulement si aucun festival n'est actif AUJOURD'HUI (bannière info)
                    noActiveFestival = activeFestivalIds.isEmpty(),
                    isOffline     = false
                )

            } catch (e: Exception) {
                // L'API a échoué malgré une connexion détectée → on tente le cache
                loadFromCache(fallbackAfterError = true)
            }
        }
    }

    // ── Chargement depuis Room ─────────────────────────────────────────────────

    private suspend fun loadFromCache(fallbackAfterError: Boolean = false) {
        val cachedFestivals = cacheRepo.getCachedFestivals()
        val cachedJeux      = cacheRepo.getCachedJeux()
        val cachedEditeurs  = cacheRepo.getCachedEditeurs()

        if (cachedFestivals.isEmpty() && cachedJeux.isEmpty() && cachedEditeurs.isEmpty()) {
            val msg = if (fallbackAfterError)
                "Impossible de charger les données"
            else
                "Aucune donnée disponible hors ligne.\nConnectez-vous pour afficher le tableau de bord."
            _uiState.value = DashboardUiState(error = msg, isOffline = true)
        } else {
            _uiState.value = DashboardUiState(
                festivals = cachedFestivals,
                jeux = cachedJeux,
                editeurs = cachedEditeurs,
                isOffline = true,
                noActiveFestival = cachedFestivals.isEmpty()
            )
        }
    }

    // Retourne true si le festival est en cours aujourd'hui (dateDebut ≤ today ≤ dateFin)
    private fun isActiveToday(festival: Festival): Boolean {
        return try {
            val today = LocalDate.now()
            val start = LocalDate.parse(festival.dateDebut.take(10))
            val end   = LocalDate.parse(festival.dateFin.take(10))
            !today.isBefore(start) && !today.isAfter(end)
        } catch (e: Exception) {
            false
        }
    }
}

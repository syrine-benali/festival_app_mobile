package com.example.festivalappmobile.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.data.remote.mapper.toDomain
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.models.Game
import com.example.festivalappmobile.domain.models.ReservationSummary
import com.example.festivalappmobile.domain.repository.EditeurRepository
import com.example.festivalappmobile.domain.repository.FestivalRepository
import com.example.festivalappmobile.domain.repository.GameRepository
import com.example.festivalappmobile.domain.repository.ReservationRepository
import com.example.festivalappmobile.utils.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "DashboardViewModel"

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val festivals: List<Festival>) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

sealed class DashboardDetailUiState {
    object Loading : DashboardDetailUiState()
    data class Success(
        val festival: Festival,
        val games: List<Game>,
        val editeurs: List<Editeur>
    ) : DashboardDetailUiState()
    data class Error(val message: String) : DashboardDetailUiState()
}

/**
 * ViewModel du tableau de bord principal.
 *
 * Gère :
 * - Le chargement offline-first des festivals via [FestivalRepository]
 * - L'état de connexion réseau via [NetworkMonitor]
 * - Le rafraîchissement automatique dès que la connexion revient
 * - [isOnline] exposé à l'UI pour afficher le bandeau offline
 */
class DashboardViewModel(
    private val festivalRepository: FestivalRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    /** Vrai si internet est disponible. L'UI observe ce Flow pour afficher le bandeau. */
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        observeFestivalsFromRepository()
        observeNetworkAndAutoRefresh()
        loadFestivals()
    }

    /**
     * Observe le StateFlow du repository.
     * Dès que Room est mis à jour (online ou offline), l'UI reçoit les nouvelles données.
     */
    private fun observeFestivalsFromRepository() {
        viewModelScope.launch {
            festivalRepository.festivals.collect { festivals ->
                if (festivals.isNotEmpty() || _uiState.value !is DashboardUiState.Loading) {
                    _uiState.value = DashboardUiState.Success(festivals)
                }
            }
        }
    }

    /**
     * Surveille l'état réseau.
     * - Met à jour [isOnline]
     * - Déclenche un refresh automatique quand on revient en ligne
     */
    private fun observeNetworkAndAutoRefresh() {
        viewModelScope.launch {
            networkMonitor.isOnline
                .distinctUntilChanged()
                .collect { online ->
                    val wasOffline = !_isOnline.value
                    _isOnline.value = online
                    if (online && wasOffline) {
                        Log.d(TAG, "Connexion restaurée → rafraîchissement automatique")
                        loadFestivals()
                    }
                }
        }
    }

    /** Déclenche le chargement des festivals (online → API+cache, offline → Room). */
    fun loadFestivals() {
        viewModelScope.launch {
            try {
                festivalRepository.getFestivals()
                // Le StateFlow du repository se met à jour automatiquement via observeFestivalsFromRepository()
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(
                    e.localizedMessage ?: "Erreur lors du chargement des festivals"
                )
            }
        }
    }
}

/**
 * ViewModel du détail d'un festival dans le dashboard.
 *
 * Charge le festival, ses jeux et éditeurs associés (via les réservations).
 * Fonctionne en mode offline en lisant le cache Room.
 */
class DashboardDetailViewModel(
    private val festivalId: Int,
    private val festivalRepository: FestivalRepository,
    private val gameRepository: GameRepository,
    private val reservationRepository: ReservationRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardDetailUiState>(DashboardDetailUiState.Loading)
    val uiState: StateFlow<DashboardDetailUiState> = _uiState.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        observeNetwork()
        load()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.distinctUntilChanged().collect { online ->
                val wasOffline = !_isOnline.value
                _isOnline.value = online
                if (online && wasOffline) {
                    Log.d(TAG, "Connexion restaurée → rechargement détail festival")
                    load()
                }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = DashboardDetailUiState.Loading
            try {
                // ── 1. Charger le festival ──────────────────────────────
                val festival = festivalRepository.getFestivalById(festivalId)
                    ?: throw Exception("Festival non trouvé")
                Log.d(TAG, "Festival chargé : ${festival.nom}")

                // ── 2. Charger les réservations du festival ──────────────
                Log.d(TAG, "Chargement des réservations pour festivalId=$festivalId")
                
                // getReservations() lance automatiquement refreshReservationsAsync() si online
                // .first() retourne le premier état (cache immédiat + données fraîches après)
                val reservations: List<ReservationSummary> = try {
                    val resList = reservationRepository.getReservations(festivalId).first()
                    Log.d(TAG, "getReservations.first() retourné ${resList.size} items")
                    resList
                } catch (e: Exception) {
                    Log.w(TAG, "Erreur chargement réservations : ${e.message}", e)
                    emptyList<ReservationSummary>()
                }
                
                Log.d(TAG, "Réservations trouvées pour festival $festivalId : ${reservations.size}")
                if (reservations.isNotEmpty()) {
                    reservations.forEach { res ->
                        Log.d(TAG, "  - Reservation: id=${res.id}, editeur=${res.editeurLibelle}")
                    }
                }

                // ── 3. Extraire les IDs des éditeurs avec réservation ────
                val editeurIdsFromReservations = reservations.map { it.editeurId }.toSet()
                Log.d(TAG, "Éditeurs avec réservation : $editeurIdsFromReservations")

                // ── 4. Charger les jeux et les filtrer par ces éditeurs ──
                var allGames: List<Game> = emptyList()
                try {
                    gameRepository.getGames().collect { result ->
                        result.onSuccess { gameList ->
                            allGames = gameList
                            Log.d(TAG, "Jeux chargés : ${gameList.size} total")
                        }
                        result.onFailure { error ->
                            Log.w(TAG, "Avertissement jeux : ${error.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception chargement jeux : ${e.message}")
                }

                // Filtrer : garder SEULEMENT les jeux des éditeurs qui ont une réservation
                val filteredGames = allGames.filter { game ->
                    game.idEditeur != null && game.idEditeur in editeurIdsFromReservations
                }
                Log.d(TAG, "Jeux après filtrage : ${filteredGames.size} (éditeurs avec réservation: ${editeurIdsFromReservations.size})")
                if (filteredGames.isNotEmpty()) {
                    filteredGames.take(3).forEach { game ->
                        Log.d(TAG, "  - Jeu: id=${game.id}, libelle=${game.libelle}, idEditeur=${game.idEditeur}")
                    }
                }

                // ── 5. Créer les éditeurs depuis les réservations ────────
                val editeurMap = reservations.associate { res ->
                    res.editeurId to Editeur(
                        id = res.editeurId,
                        libelle = res.editeurLibelle,
                        exposant = false,
                        distributeur = false,
                        logo = null,
                        phone = null,
                        email = null,
                        notes = null,
                        workflowStatus = null,
                        hasReservation = true
                    )
                }
                val filteredEditeurs = editeurMap.values.toList()

                Log.d(TAG, "RÉSULTAT FINAL: festival='${festival.nom}', Jeux affichés : ${filteredGames.size}, Éditeurs : ${filteredEditeurs.size}")

                _uiState.value = DashboardDetailUiState.Success(
                    festival = festival,
                    games = filteredGames,
                    editeurs = filteredEditeurs
                )
            } catch (e: Exception) {
                Log.e(TAG, "Erreur chargement détail : ${e.message}", e)
                _uiState.value = DashboardDetailUiState.Error(
                    e.localizedMessage ?: "Erreur lors du chargement des détails"
                )
            }
        }
    }

    fun reload() = load()
}

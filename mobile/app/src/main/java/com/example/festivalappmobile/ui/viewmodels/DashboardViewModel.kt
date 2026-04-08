package com.example.festivalappmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.models.Game
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.repository.FestivalRepository
import com.example.festivalappmobile.domain.repository.GameRepository
import com.example.festivalappmobile.domain.repository.EditeurRepository
import com.example.festivalappmobile.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

class DashboardViewModel(
    private val festivalRepository: FestivalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeFestivals()
        loadFestivals()
    }

    private fun observeFestivals() {
        viewModelScope.launch {
            festivalRepository.festivals.collect { festivals ->
                _uiState.value = DashboardUiState.Success(festivals)
            }
        }
    }

    fun loadFestivals() {
        viewModelScope.launch {
            try {
                festivalRepository.getFestivals()
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(
                    e.localizedMessage ?: "Erreur lors du chargement des festivals"
                )
            }
        }
    }
}

class DashboardDetailViewModel(
    private val festivalId: Int,
    private val festivalRepository: FestivalRepository,
    private val gameRepository: GameRepository,
    private val editeurRepository: EditeurRepository,
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardDetailUiState>(DashboardDetailUiState.Loading)
    val uiState: StateFlow<DashboardDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = DashboardDetailUiState.Loading
            try {
                // Load festival
                val festival = festivalRepository.getFestivalById(festivalId)
                    ?: throw Exception("Festival non trouvé")

                Log.d(TAG, "Festival chargé: ${festival.nom}")

                // Load reservations for this festival to get associated editors
                val editeurIds = mutableSetOf<Int>()

                val reservations = reservationRepository.getReservations(festivalId = festivalId).first()
                Log.d(TAG, "Réservations trouvées: ${reservations.size}")
                
                // Extract unique editeur IDs from reservations
                reservations.forEach { reservation ->
                    editeurIds.add(reservation.editeurId)
                    Log.d(TAG, "Editeur ID from reservation: ${reservation.editeurId}")
                }

                Log.d(TAG, "Editeur IDs from reservations: $editeurIds")

                // Load all games
                Log.d(TAG, "Début chargement jeux...")
                val allGames = mutableListOf<Game>()
                try {
                    gameRepository.getGames().collect { result ->
                        Log.d(TAG, "Résultat getGames reçu")
                        result.onSuccess { gameList ->
                            allGames.addAll(gameList)
                            Log.d(TAG, "Jeux tous chargés: ${gameList.size}")
                        }
                        result.onFailure { error ->
                            Log.e(TAG, "Erreur getGames: ${error.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception lors du chargement jeux: ${e.message}")
                    throw e
                }

                // Load all editeurs
                Log.d(TAG, "Début chargement éditeurs...")
                val allEditeurs = editeurRepository.getAllEditeurs()
                Log.d(TAG, "Éditeurs tous chargés: ${allEditeurs.size}")

                // Filter games and editeurs by reservation editeur IDs
                val filteredGames = if (editeurIds.isNotEmpty()) {
                    allGames.filter { game ->
                        game.idEditeur in editeurIds
                    }
                } else {
                    emptyList()
                }

                val filteredEditeurs = if (editeurIds.isNotEmpty()) {
                    allEditeurs.filter { editeur ->
                        editeur.id in editeurIds
                    }
                } else {
                    emptyList()
                }

                Log.d(TAG, "Jeux filtrés: ${filteredGames.size}, Éditeurs filtrés: ${filteredEditeurs.size}")

                _uiState.value = DashboardDetailUiState.Success(
                    festival = festival,
                    games = filteredGames,
                    editeurs = filteredEditeurs
                )
            } catch (e: Exception) {
                Log.e(TAG, "Erreur: ${e.message}", e)
                _uiState.value = DashboardDetailUiState.Error(
                    e.localizedMessage ?: "Erreur lors du chargement des détails"
                )
            }
        }
    }

    fun reload() {
        load()
    }
}

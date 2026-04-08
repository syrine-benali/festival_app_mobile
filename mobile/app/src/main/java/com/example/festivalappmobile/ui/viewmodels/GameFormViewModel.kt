package com.example.festivalappmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.domain.models.Game
import com.example.festivalappmobile.domain.repository.EditeurRepository
import com.example.festivalappmobile.domain.repository.GameRepository
import com.example.festivalappmobile.domain.models.Editeur
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameFormState(
    val id: Int = 0,
    val libelle: String = "",
    val auteur: String = "",
    val nbMinJoueur: String = "",
    val nbMaxJoueur: String = "",
    val ageMin: String = "",
    val duree: String = "",
    val prototype: Boolean = false,
    val theme: String = "",
    val description: String = "",
    val idEditeur: Int? = null,
    val idTypeJeu: Int? = null,
    val editeurs: List<Editeur> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class GameFormViewModel(
    private val gameRepository: GameRepository,
    private val editeurRepository: EditeurRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GameFormState())
    val state: StateFlow<GameFormState> = _state.asStateFlow()

    init {
        loadEditeurs()
    }

    private fun loadEditeurs() {
        viewModelScope.launch {
            editeurRepository.editeurs.collect { editeurs ->
                _state.update { it.copy(editeurs = editeurs) }
            }
        }
        viewModelScope.launch {
            editeurRepository.getAllEditeurs()
        }
    }

    fun loadGameById(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            gameRepository.getGameById(id).onSuccess { game ->
                _state.update { it.copy(isLoading = false) }
                loadGame(game)
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadGame(game: Game) {
        _state.update {
            it.copy(
                id = game.id,
                libelle = game.libelle,
                auteur = game.auteur ?: "",
                nbMinJoueur = game.nbMinJoueur?.toString() ?: "",
                nbMaxJoueur = game.nbMaxJoueur?.toString() ?: "",
                ageMin = game.ageMin?.toString() ?: "",
                duree = game.duree?.toString() ?: "",
                prototype = game.prototype,
                theme = game.theme ?: "",
                description = game.description ?: "",
                idEditeur = game.idEditeur,
                idTypeJeu = game.idTypeJeu
            )
        }
    }

    fun onLibelleChange(value: String) = _state.update { it.copy(libelle = value) }
    fun onAuteurChange(value: String) = _state.update { it.copy(auteur = value) }
    fun onMinPlayersChange(value: String) = _state.update { it.copy(nbMinJoueur = value) }
    fun onMaxPlayersChange(value: String) = _state.update { it.copy(nbMaxJoueur = value) }
    fun onAgeMinChange(value: String) = _state.update { it.copy(ageMin = value) }
    fun onDureeChange(value: String) = _state.update { it.copy(duree = value) }
    fun onPrototypeChange(value: Boolean) = _state.update { it.copy(prototype = value) }
    fun onThemeChange(value: String) = _state.update { it.copy(theme = value) }
    fun onDescriptionChange(value: String) = _state.update { it.copy(description = value) }
    fun onEditeurChange(id: Int?) = _state.update { it.copy(idEditeur = id) }

    fun save() {
        viewModelScope.launch {
            val s = _state.value
            _state.update { it.copy(isSaving = true, error = null) }
            
            val game = Game(
                id = s.id,
                libelle = s.libelle,
                auteur = s.auteur.takeIf { it.isNotBlank() },
                nbMinJoueur = s.nbMinJoueur.toIntOrNull(),
                nbMaxJoueur = s.nbMaxJoueur.toIntOrNull(),
                ageMin = s.ageMin.toIntOrNull(),
                duree = s.duree.toIntOrNull(),
                prototype = s.prototype,
                image = null,
                theme = s.theme.takeIf { it.isNotBlank() },
                description = s.description.takeIf { it.isNotBlank() },
                idEditeur = s.idEditeur,
                idTypeJeu = s.idTypeJeu
            )

            val result = if (s.id == 0) {
                gameRepository.createGame(game)
            } else {
                gameRepository.updateGame(game)
            }

            result.onSuccess {
                _state.update { it.copy(isSaving = false, success = true) }
            }.onFailure { e ->
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}

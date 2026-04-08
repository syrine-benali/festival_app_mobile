package com.example.festivalappmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.domain.models.Game
import com.example.festivalappmobile.domain.repository.GameRepository
import com.example.festivalappmobile.domain.usecases.game.DeleteGameUseCase
import com.example.festivalappmobile.domain.usecases.game.GetAllGamesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class GameUiState {
    object Loading : GameUiState()
    data class Success(val games: List<Game>) : GameUiState()
    data class Error(val message: String) : GameUiState()
}

data class GameListState(
    val uiState: GameUiState = GameUiState.Loading,
    val searchQuery: String = ""
)

class GameListViewModel(
    private val getAllGamesUseCase: GetAllGamesUseCase,
    private val deleteGameUseCase: DeleteGameUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GameListState())
    val state: StateFlow<GameListState> = _state.asStateFlow()

    init {
        loadGames()
    }

    fun loadGames() {
        _state.update { it.copy(uiState = GameUiState.Loading) }
        viewModelScope.launch {
            getAllGamesUseCase().collect { result ->
                result.onSuccess { games ->
                    _state.update { it.copy(uiState = GameUiState.Success(games)) }
                }.onFailure { error ->
                    _state.update { it.copy(uiState = GameUiState.Error(error.message ?: "Unknown error")) }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun deleteGame(id: Int) {
        viewModelScope.launch {
            deleteGameUseCase(id).onSuccess {
                loadGames()
            }.onFailure { error ->
                // Optionally handle error state
            }
        }
    }
}

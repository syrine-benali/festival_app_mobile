package com.example.festivalappmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.domain.models.Editeur
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class EditeurUiState {
    object Loading : EditeurUiState()
    data class Success(val editeurs: List<Editeur>) : EditeurUiState()
    data class Error(val message: String) : EditeurUiState()
}

enum class ViewMode {
    LIST, GRID
}

data class EditeurListState(
    val uiState: EditeurUiState = EditeurUiState.Loading,
    val viewMode: ViewMode = ViewMode.LIST
)

class EditeurListViewModel(
    private val repository: com.example.festivalappmobile.domain.repository.EditeurRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditeurListState())
    val state: StateFlow<EditeurListState> = _state.asStateFlow()

    init {
        observeEditeurs()
        loadEditeurs()
    }

    private fun observeEditeurs() {
        viewModelScope.launch {
            repository.editeurs.collect { editeurs ->
                _state.update { it.copy(uiState = EditeurUiState.Success(editeurs)) }
            }
        }
    }

    fun loadEditeurs() {
        // We only trigger the loading, the observation handles the UI update
        viewModelScope.launch {
            try {
                repository.getAllEditeurs()
            } catch (e: Exception) {
                _state.update { it.copy(uiState = EditeurUiState.Error(e.message ?: "Une erreur est survenue")) }
            }
        }
    }

    fun toggleViewMode() {
        _state.update { 
            it.copy(viewMode = if (it.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST)
        }
    }
}

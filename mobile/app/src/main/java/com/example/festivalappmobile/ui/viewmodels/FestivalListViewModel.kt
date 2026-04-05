package com.example.festivalappmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.repository.FestivalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FestivalUiState {
    object Loading : FestivalUiState()
    data class Success(val festivals: List<Festival>) : FestivalUiState()
    data class Error(val message: String) : FestivalUiState()
}

class FestivalListViewModel(private val repository: FestivalRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<FestivalUiState>(FestivalUiState.Loading)
    val uiState: StateFlow<FestivalUiState> = _uiState.asStateFlow()

    init {
        observeFestivals()
        loadFestivals()
    }

    private fun observeFestivals() {
        viewModelScope.launch {
            repository.festivals.collect { festivals ->
                val sortedFestivals = festivals.sortedByDescending { it.id }
                _uiState.value = FestivalUiState.Success(sortedFestivals)
            }
        }
    }

    fun loadFestivals() {
        viewModelScope.launch {
            try {
                repository.getFestivals()
            } catch (e: Exception) {
                _uiState.value = FestivalUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun deleteFestival(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteFestival(id)
            } catch (e: Exception) {
                // Ignore error in this reactive setup as the list will just not update
            }
        }
    }
}

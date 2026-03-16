package com.example.festivalappmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.usecases.GetFestivalsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FestivalUiState {
    object Loading : FestivalUiState()
    data class Success(val festivals: List<Festival>) : FestivalUiState()
    data class Error(val message: String) : FestivalUiState()
}

class FestivalListViewModel(private val getFestivalsUseCase: GetFestivalsUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<FestivalUiState>(FestivalUiState.Loading)
    val uiState: StateFlow<FestivalUiState> = _uiState.asStateFlow()

    init {
        loadFestivals()
    }

    fun loadFestivals() {
        viewModelScope.launch {
            _uiState.value = FestivalUiState.Loading
            try {
                val festivals = getFestivalsUseCase()
                _uiState.value = FestivalUiState.Success(festivals)
            } catch (e: Exception) {
                _uiState.value = FestivalUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}

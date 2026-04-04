package com.example.festivalappmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.data.remote.RetrofitClient
import com.example.festivalappmobile.data.repository.EditeurRepositoryImpl
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.usecases.editeur.GetEditeursUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EditeurUiState {
    object Loading : EditeurUiState()
    data class Success(val editeurs: List<Editeur>) : EditeurUiState()
    data class Error(val message: String) : EditeurUiState()
}

class EditeurListViewModel(private val getEditeursUseCase: GetEditeursUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<EditeurUiState>(EditeurUiState.Loading)
    val uiState: StateFlow<EditeurUiState> = _uiState.asStateFlow()

    init {
        loadEditeurs()
    }

    fun loadEditeurs() {
        viewModelScope.launch {
            _uiState.value = EditeurUiState.Loading
            try {
                val editeurs = getEditeursUseCase()
                _uiState.value = EditeurUiState.Success(editeurs)
            } catch (e: Exception) {
                _uiState.value = EditeurUiState.Error(e.message ?: "Erreur lors du chargement des editeurs")
            }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = EditeurRepositoryImpl(RetrofitClient.instance)
                    val useCase = GetEditeursUseCase(repository)
                    return EditeurListViewModel(useCase) as T
                }
            }
    }
}

package com.example.festivalappmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.data.repository.ReservationRepositoryImpl
import com.example.festivalappmobile.domain.models.Reservation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReservationUiState(
    val isLoading: Boolean = false,
    val reservations: List<Reservation> = emptyList(),
    val errorMessage: String? = null
)

class ReservationViewModel(private val repository: ReservationRepositoryImpl) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // On combine les flux pour créer l'état final de l'UI
    val uiState: StateFlow<ReservationUiState> = combine(
        repository.reservations,
        _isLoading,
        _errorMessage
    ) { reservations, isLoading, error ->
        ReservationUiState(isLoading, reservations, error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReservationUiState())

    init {
        fetchReservations()
    }

    fun fetchReservations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.refreshReservations() // Appel réseau + maj Room
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Mode hors ligne. Affichage des données locales."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
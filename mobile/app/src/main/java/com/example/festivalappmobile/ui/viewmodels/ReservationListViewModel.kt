package com.example.festivalappmobile.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.data.local.AppDatabase
import com.example.festivalappmobile.data.remote.RetrofitClient
import com.example.festivalappmobile.data.repository.ReservationRepositoryImpl
import com.example.festivalappmobile.domain.models.ReservationSummary
import com.example.festivalappmobile.domain.models.WorkflowStatus
import com.example.festivalappmobile.domain.usecases.reservation.GetReservationsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class ReservationListUiState(
    val reservations: List<ReservationSummary> = emptyList(),
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null,
    val filterStatus: WorkflowStatus? = null
)

class ReservationListViewModel(
    context: Context,
    private val festivalId: Int? = null
) : ViewModel() {

    private val repo = ReservationRepositoryImpl(
        api = RetrofitClient.instance,
        db = AppDatabase.getInstance(context),
        context = context.applicationContext  // ← plus de TODO
    )
    private val getReservations = GetReservationsUseCase(repo)
    private var reservationsJob: Job? = null

    private val _uiState = MutableStateFlow(ReservationListUiState())
    val uiState: StateFlow<ReservationListUiState> = _uiState.asStateFlow()

    init {
        observeReservations()
        refresh()
    }

    private fun observeReservations() {
        reservationsJob?.cancel()
        reservationsJob = getReservations(festivalId)
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Erreur de lecture des reservations") }
            }
            .onEach { list ->
                _uiState.update { state ->
                    val filtered = state.filterStatus
                        ?.let { list.filter { r -> r.workflowStatus == it } }
                        ?: list
                    state.copy(reservations = filtered, isLoading = false)
                }
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repo.refreshReservations(festivalId)
                _uiState.update { it.copy(isOffline = !repo.isOnline()) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun setFilter(status: WorkflowStatus?) {
        _uiState.update { it.copy(filterStatus = status) }
        observeReservations()
    }

    // Factory pour créer le ViewModel avec un Context
    companion object {
        fun factory(context: Context, festivalId: Int? = null): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ReservationListViewModel(context.applicationContext, festivalId) as T
            }
    }
}
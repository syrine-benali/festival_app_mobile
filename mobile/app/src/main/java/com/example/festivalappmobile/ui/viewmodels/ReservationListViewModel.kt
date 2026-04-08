package com.example.festivalappmobile.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.data.local.AppDatabase
import com.example.festivalappmobile.data.remote.RetrofitClient
import com.example.festivalappmobile.data.repository.EditeurRepositoryImpl
import com.example.festivalappmobile.data.repository.FestivalRepositoryImpl
import com.example.festivalappmobile.data.repository.ReservationRepositoryImpl
import com.example.festivalappmobile.utils.NetworkMonitor
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.models.ReservationSummary
import com.example.festivalappmobile.domain.models.TypeRemise
import com.example.festivalappmobile.domain.models.TypeReservant
import com.example.festivalappmobile.domain.models.WorkflowStatus
import com.example.festivalappmobile.domain.usecases.reservation.GetReservationsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class ReservationListUiState(
    val reservations: List<ReservationSummary> = emptyList(),
    val isLoading: Boolean = false,
    val isCreateFormLoading: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null,
    val filterStatus: WorkflowStatus? = null,
    val successMessage: String? = null,
    val createdReservationId: Int? = null,
    val editeurs: List<Editeur> = emptyList(),
    val festivals: List<Festival> = emptyList()
)

class ReservationListViewModel(
    context: Context,
    private val festivalId: Int? = null
) : ViewModel() {

    private val networkMonitor = NetworkMonitor(context.applicationContext)
    private val db = AppDatabase.getInstance(context)
    private val api = RetrofitClient.instance

    private val repo = ReservationRepositoryImpl(
        api = api,
        db = db,
        context = context.applicationContext
    )
    private val editeurRepo = EditeurRepositoryImpl(api, db.editeurDao(), networkMonitor)
    private val festivalRepo = FestivalRepositoryImpl(api, db.festivalDao(), networkMonitor)
    private val getReservations = GetReservationsUseCase(repo)
    private var reservationsJob: Job? = null

    private val _uiState = MutableStateFlow(ReservationListUiState())
    val uiState: StateFlow<ReservationListUiState> = _uiState.asStateFlow()

    init {
        observeReservations()
        refresh()
        loadCreateFormData()
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

    fun createReservation(
        editeurId: Int,
        festivalId: Int,
        typeReservant: TypeReservant = TypeReservant.EDITEUR,
        nbPrisesElectriques: Int = 0,
        typeRemise: TypeRemise? = null,
        valeurRemise: Double? = null,
        notesClient: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repo.createReservation(
                editeurId = editeurId,
                festivalId = festivalId,
                typeReservant = typeReservant,
                notesClient = notesClient?.ifBlank { null }
            )
                .onSuccess { created ->
                    repo.updateReservationFlags(
                        id = created.id,
                        nbPrisesElectriques = nbPrisesElectriques,
                        typeRemise = typeRemise,
                        valeurRemise = if (typeRemise != null) (valeurRemise ?: 0.0) else null,
                        notesClient = notesClient?.ifBlank { null }
                    ).onSuccess {
                        refresh()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Réservation créée avec succès",
                                createdReservationId = created.id
                            )
                        }
                    }.onFailure { updateError ->
                        refresh()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Réservation créée, mais options supplémentaires non enregistrées",
                                error = updateError.message,
                                createdReservationId = created.id
                            )
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Erreur création réservation"
                        )
                    }
                }
        }
    }

    fun consumeCreatedReservationNavigation() {
        _uiState.update { it.copy(createdReservationId = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    private fun loadCreateFormData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreateFormLoading = true) }
            val editeurs = editeurRepo.getAllEditeurs()
                .sortedBy { it.libelle.lowercase() }
            val festivals = festivalRepo.getFestivals()
                .sortedByDescending { it.dateDebut }

            _uiState.update {
                it.copy(
                    isCreateFormLoading = false,
                    editeurs = editeurs,
                    festivals = festivals
                )
            }
        }
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
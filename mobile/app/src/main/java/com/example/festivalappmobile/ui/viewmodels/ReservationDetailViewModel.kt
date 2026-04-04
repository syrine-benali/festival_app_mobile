package com.example.festivalappmobile.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.data.local.AppDatabase
import com.example.festivalappmobile.data.remote.RetrofitClient
import com.example.festivalappmobile.data.repository.ReservationRepositoryImpl
import com.example.festivalappmobile.domain.models.*
import com.example.festivalappmobile.domain.usecases.reservation.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReservationDetailUiState(
    val reservation: Reservation? = null,
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ReservationDetailViewModel(
    private val reservationId: Int,
    context: Context
) : ViewModel() {

    private val repo = ReservationRepositoryImpl(
        api = RetrofitClient.instance,
        db = AppDatabase.getInstance(context),
        context = context.applicationContext  // ← plus de TODO
    )
    private val getDetail = GetReservationDetailUseCase(repo)
    private val updateWorkflow = UpdateWorkflowUseCase(repo)
    private val addContact = AddContactUseCase(repo)
    private val addJeu = AddJeuUseCase(repo)

    private val _uiState = MutableStateFlow(ReservationDetailUiState())
    val uiState: StateFlow<ReservationDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getDetail(reservationId)
                .onSuccess { res ->
                    _uiState.update {
                        it.copy(
                            reservation = res,
                            isLoading = false,
                            isOffline = !repo.isOnline()
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun updateStatus(status: WorkflowStatus) {
        viewModelScope.launch {
            updateWorkflow(reservationId, status)
                .onSuccess { res ->
                    _uiState.update { it.copy(reservation = res, successMessage = "Statut mis à jour") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun addContactEntry(date: String, commentaire: String?) {
        viewModelScope.launch {
            addContact(reservationId, date, commentaire)
                .onSuccess { load() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun updateFlag(
        viendraPresenteSesJeux: Boolean? = null,
        listeJeuxDemandee: Boolean? = null,
        listeJeuxObtenue: Boolean? = null,
        jeuxRecusPhysiquement: Boolean? = null,
        notesClient: String? = null
    ) {
        viewModelScope.launch {
            repo.updateReservationFlags(
                id = reservationId,
                viendraPresenteSesJeux = viendraPresenteSesJeux,
                listeJeuxDemandee = listeJeuxDemandee,
                listeJeuxObtenue = listeJeuxObtenue,
                jeuxRecusPhysiquement = jeuxRecusPhysiquement,
                notesClient = notesClient
            ).onSuccess { res ->
                _uiState.update { it.copy(reservation = res) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun addJeuEntry(jeuId: Int, nbExemplaires: Int, nbTables: Int, zonePlanId: Int?) {
        viewModelScope.launch {
            addJeu(reservationId, jeuId, nbExemplaires, nbTables, zonePlanId)
                .onSuccess { load() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(error = null, successMessage = null) }

    companion object {
        fun factory(context: Context, reservationId: Int): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ReservationDetailViewModel(reservationId, context.applicationContext) as T
            }
    }
}
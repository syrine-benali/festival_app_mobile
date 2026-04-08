package com.example.festivalappmobile.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.data.local.AppDatabase
import com.example.festivalappmobile.data.remote.RetrofitClient
import com.example.festivalappmobile.data.remote.dto.AddZoneTarifaireRequestDto
import com.example.festivalappmobile.data.repository.GameRepositoryImpl
import com.example.festivalappmobile.data.repository.ReservationRepositoryImpl
import com.example.festivalappmobile.utils.NetworkMonitor
import com.example.festivalappmobile.domain.models.*
import com.example.festivalappmobile.domain.usecases.reservation.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs

data class ReservationDetailUiState(
    val reservation: Reservation? = null,
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val isGamesLoading: Boolean = false,
    val editorGames: List<Game> = emptyList(),
    val isOffline: Boolean = false,
    val totalToPay: Double? = null,
    val deleted: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ReservationDetailViewModel(
    private val reservationId: Int,
    context: Context
) : ViewModel() {

    private val networkMonitor = NetworkMonitor(context.applicationContext)
    private val db = AppDatabase.getInstance(context)
    private val api = RetrofitClient.instance

    private val repo = ReservationRepositoryImpl(
        api = api,
        db = db,
        context = context.applicationContext
    )
    private val getDetail = GetReservationDetailUseCase(repo)
    private val updateWorkflow = UpdateWorkflowUseCase(repo)
    private val addContact = AddContactUseCase(repo)
    private val addJeu = AddJeuUseCase(repo)
    private val gameRepo = GameRepositoryImpl(api, db.gameDao(), networkMonitor)

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
                    loadEditorGames(res.editeurId)
                    calculatePrice()
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
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Contact ajouté") }
                    load()
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun deleteContactEntry(contactId: Int) {
        viewModelScope.launch {
            repo.deleteContact(contactId)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Contact supprimé") }
                    load()
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun updateReservation(
        typeReservant: TypeReservant? = null,
        dateFacturation: String? = null,
        viendraPresenteSesJeux: Boolean? = null,
        nousPresentons: Boolean? = null,
        listeJeuxDemandee: Boolean? = null,
        listeJeuxObtenue: Boolean? = null,
        jeuxRecusPhysiquement: Boolean? = null,
        notesClient: String? = null,
        notesWorkflow: String? = null,
        typeRemise: TypeRemise? = null,
        valeurRemise: Double? = null,
        nbPrisesElectriques: Int? = null
    ) {
        viewModelScope.launch {
            repo.updateReservationFlags(
                id = reservationId,
                typeReservant = typeReservant,
                dateFacturation = dateFacturation,
                viendraPresenteSesJeux = viendraPresenteSesJeux,
                nousPresentons = nousPresentons,
                listeJeuxDemandee = listeJeuxDemandee,
                listeJeuxObtenue = listeJeuxObtenue,
                jeuxRecusPhysiquement = jeuxRecusPhysiquement,
                notesClient = notesClient,
                notesWorkflow = notesWorkflow,
                typeRemise = typeRemise,
                valeurRemise = valeurRemise,
                nbPrisesElectriques = nbPrisesElectriques
            ).onSuccess { res ->
                _uiState.update { it.copy(reservation = res, successMessage = "Réservation mise à jour") }
                calculatePrice()
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun addJeuEntry(jeuId: Int, nbExemplaires: Int, nbTables: Int, placementId: Int?) {
        viewModelScope.launch {
            addJeu(reservationId, jeuId, nbExemplaires, nbTables, placementId)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Jeu ajouté") }
                    load()
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun updateJeuEntry(jeuId: Int, nbExemplaires: Int?, nbTables: Int?, placementId: Int?) {
        viewModelScope.launch {
            repo.updateJeu(jeuId, nbExemplaires, nbTables, placementId)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Jeu mis à jour") }
                    load()
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun deleteJeuEntry(jeuId: Int) {
        viewModelScope.launch {
            repo.deleteJeu(jeuId)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Jeu supprimé") }
                    load()
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun addLineEntry(tablePrice: Double, nbTables: Int, grandesTablesSouhaitees: Boolean) {
        viewModelScope.launch {
            val reservation = _uiState.value.reservation
            if (reservation == null) {
                _uiState.update { it.copy(error = "Réservation introuvable") }
                return@launch
            }

            val festivalResponse = try {
                RetrofitClient.instance.getFestivalById(reservation.festivalId)
            } catch (_: Exception) {
                null
            }

            val initialZones = if (festivalResponse?.isSuccessful == true) {
                festivalResponse.body()?.zoneTarifaires.orEmpty()
            } else {
                emptyList()
            }

            var selectedZone = initialZones.firstOrNull { abs(it.prixTable - tablePrice) < 0.01 }

            if (selectedZone == null) {
                val ratioM2ParTable = initialZones
                    .firstOrNull { it.prixTable > 0.0 }
                    ?.let { it.prixM2 / it.prixTable }
                    ?.takeIf { it > 0.0 }
                    ?: 0.5

                val createZoneResponse = try {
                    RetrofitClient.instance.addZoneTarifaire(
                        festivalId = reservation.festivalId,
                        body = AddZoneTarifaireRequestDto(
                            nom = "Classe ${tablePrice.toInt()} EUR/table",
                            prixTable = tablePrice,
                            prixM2 = tablePrice * ratioM2ParTable
                        )
                    )
                } catch (_: Exception) {
                    null
                }

                if (createZoneResponse?.isSuccessful == true) {
                    selectedZone = createZoneResponse.body()
                }
            }

            if (selectedZone == null) {
                val availablePrices = initialZones
                    .map { it.prixTable }
                    .sorted()
                    .joinToString(", ") { "${it.toInt()}" }

                _uiState.update {
                    it.copy(
                        error = if (availablePrices.isBlank()) {
                            "Impossible de trouver ou creer la classe tarifaire ${tablePrice.toInt()} EUR/table"
                        } else {
                            "Impossible de trouver ou creer ${tablePrice.toInt()} EUR/table. Prix disponibles: $availablePrices"
                        }
                    )
                }
                return@launch
            }

            repo.addLine(
                reservationId = reservationId,
                pricingId = selectedZone.id,
                nbTables = nbTables,
                grandesTablesSouhaitees = grandesTablesSouhaitees
            )
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Ligne ajoutée") }
                    load()
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun updateLineEntry(
        lineId: Int,
        nbTables: Int?,
        nbM2: Double?,
        grandesTablesSouhaitees: Boolean?
    ) {
        viewModelScope.launch {
            repo.updateLine(lineId, nbTables, nbM2, grandesTablesSouhaitees)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Ligne mise à jour") }
                    load()
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun deleteLineEntry(lineId: Int) {
        viewModelScope.launch {
            repo.deleteLine(lineId)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Ligne supprimée") }
                    load()
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun deleteReservation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            repo.deleteReservation(reservationId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            deleted = true,
                            successMessage = "Réservation supprimée"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            error = e.message ?: "Erreur suppression réservation"
                        )
                    }
                }
        }
    }

    fun consumeDeletedState() {
        _uiState.update { it.copy(deleted = false) }
    }

    private fun calculatePrice() {
        viewModelScope.launch {
            repo.calculatePrice(reservationId)
                .onSuccess { total ->
                    _uiState.update { it.copy(totalToPay = total) }
                }
                .onFailure {
                    // On garde le prix local en fallback si le calcul backend échoue.
                    val fallback = _uiState.value.reservation?.totalPrice
                    _uiState.update { it.copy(totalToPay = fallback) }
                }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(error = null, successMessage = null) }

    private fun loadEditorGames(editeurId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGamesLoading = true) }
            try {
                gameRepo.getGamesByEditeur(editeurId)
                    .onSuccess { games ->
                        val filtered = games
                            .sortedBy {
                                runCatching { it.libelle }
                                    .getOrDefault("")
                                    .lowercase()
                            }
                        _uiState.update {
                            it.copy(
                                editorGames = filtered,
                                isGamesLoading = false
                            )
                        }
                    }
                    .onFailure {
                        _uiState.update { it.copy(editorGames = emptyList(), isGamesLoading = false) }
                    }
            } catch (_: Exception) {
                _uiState.update { it.copy(editorGames = emptyList(), isGamesLoading = false) }
            }
        }
    }

    companion object {
        fun factory(context: Context, reservationId: Int): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ReservationDetailViewModel(reservationId, context.applicationContext) as T
            }
    }
}
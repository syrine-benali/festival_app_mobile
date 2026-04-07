package com.example.festivalappmobile.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.data.remote.dto.FestivalCreateRequestDto
import com.example.festivalappmobile.data.remote.dto.FestivalUpdateRequestDto
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.usecases.festival.CreateFestivalUseCase
import com.example.festivalappmobile.domain.usecases.festival.GetFestivalByIdUseCase
import com.example.festivalappmobile.domain.usecases.festival.UpdateFestivalUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class FestivalFormState(
    val id: Int? = null,
    val nom: String = "",
    val lieu: String = "",
    val dateDebut: String = "", // format DD/MM/YYYY
    val dateFin: String = "",   // format DD/MM/YYYY
    val nbTotalTable: String = "0",
    val nbTotalChaise: String = "0",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSubmitted: Boolean = false
)

sealed class FestivalFormEvent {
    object Success : FestivalFormEvent()
    data class Error(val message: String) : FestivalFormEvent()
}

class FestivalFormViewModel(
    private val getFestivalByIdUseCase: GetFestivalByIdUseCase,
    private val createFestivalUseCase: CreateFestivalUseCase,
    private val updateFestivalUseCase: UpdateFestivalUseCase
) : ViewModel() {

    private val _state = mutableStateOf(FestivalFormState())
    val state: State<FestivalFormState> = _state

    private val _eventFlow = MutableSharedFlow<FestivalFormEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    private val apiFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)

    fun loadFestival(id: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val festival = getFestivalByIdUseCase(id)
                if (festival != null) {
                    _state.value = _state.value.copy(
                        id = festival.id,
                        nom = festival.nom,
                        lieu = festival.lieu,
                        dateDebut = formatDateFromApi(festival.dateDebut),
                        dateFin = formatDateFromApi(festival.dateFin),
                        nbTotalTable = festival.nbTotalTable.toString(),
                        nbTotalChaise = festival.nbTotalChaise.toString(),
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "Festival non trouvé")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onEvent(event: FestivalFormUiEvent) {
        when (event) {
            is FestivalFormUiEvent.EnteredNom -> {
                _state.value = _state.value.copy(nom = event.value)
            }
            is FestivalFormUiEvent.EnteredLieu -> {
                _state.value = _state.value.copy(lieu = event.value)
            }
            is FestivalFormUiEvent.EnteredDateDebut -> {
                _state.value = _state.value.copy(dateDebut = event.value)
            }
            is FestivalFormUiEvent.EnteredDateFin -> {
                _state.value = _state.value.copy(dateFin = event.value)
            }
            is FestivalFormUiEvent.EnteredNbTotalTable -> {
                _state.value = _state.value.copy(nbTotalTable = event.value)
            }
            is FestivalFormUiEvent.EnteredNbTotalChaise -> {
                _state.value = _state.value.copy(nbTotalChaise = event.value)
            }
            is FestivalFormUiEvent.SaveFestival -> {
                saveFestival()
            }
        }
    }

    private fun saveFestival() {
        viewModelScope.launch {
            if (!validate()) return@launch

            _state.value = _state.value.copy(isLoading = true)
            try {
                val result: Festival? = if (_state.value.id == null) {
                    createFestivalUseCase(
                        FestivalCreateRequestDto(
                            nom = _state.value.nom,
                            lieu = _state.value.lieu,
                            dateDebut = formatDateToApi(_state.value.dateDebut),
                            dateFin = formatDateToApi(_state.value.dateFin),
                            nbTotalTable = _state.value.nbTotalTable.toIntOrNull() ?: 0,
                            nbTotalChaise = _state.value.nbTotalChaise.toIntOrNull() ?: 0
                        )
                    )
                } else {
                    updateFestivalUseCase(
                        _state.value.id!!,
                        FestivalUpdateRequestDto(
                            nom = _state.value.nom,
                            lieu = _state.value.lieu,
                            dateDebut = formatDateToApi(_state.value.dateDebut),
                            dateFin = formatDateToApi(_state.value.dateFin),
                            nbTotalTable = _state.value.nbTotalTable.toIntOrNull(),
                            nbTotalChaise = _state.value.nbTotalChaise.toIntOrNull()
                        )
                    )
                }

                if (result != null) {
                    _eventFlow.emit(FestivalFormEvent.Success)
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "Erreur lors de l'enregistrement")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun validate(): Boolean {
        if (_state.value.nom.isBlank()) {
            _state.value = _state.value.copy(error = "Le nom est obligatoire")
            return false
        }
        if (_state.value.dateDebut.isBlank() || _state.value.dateFin.isBlank()) {
            _state.value = _state.value.copy(error = "Les dates sont obligatoires")
            return false
        }
        
        try {
            val start = dateFormatter.parse(_state.value.dateDebut)
            val end = dateFormatter.parse(_state.value.dateFin)
            if (end != null && start != null && end.before(start)) {
                _state.value = _state.value.copy(error = "La date de fin ne peut pas être avant la date de début")
                return false
            }
        } catch (e: Exception) {
            // Ignore parse errors here as they should be handled by the picker
        }

        return true
    }

    private fun formatDateToApi(dateStr: String): String {
        return try {
            val date = dateFormatter.parse(dateStr)
            apiFormatter.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun formatDateFromApi(dateStr: String): String {
        return try {
            if (dateStr.contains("-")) {
                val date = apiFormatter.parse(dateStr)
                dateFormatter.format(date!!)
            } else {
                dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }
}

sealed class FestivalFormUiEvent {
    data class EnteredNom(val value: String) : FestivalFormUiEvent()
    data class EnteredLieu(val value: String) : FestivalFormUiEvent()
    data class EnteredDateDebut(val value: String) : FestivalFormUiEvent()
    data class EnteredDateFin(val value: String) : FestivalFormUiEvent()
    data class EnteredNbTotalTable(val value: String) : FestivalFormUiEvent()
    data class EnteredNbTotalChaise(val value: String) : FestivalFormUiEvent()
    object SaveFestival : FestivalFormUiEvent()
}

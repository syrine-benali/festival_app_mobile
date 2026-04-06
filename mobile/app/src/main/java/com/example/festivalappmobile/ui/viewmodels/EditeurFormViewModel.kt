package com.example.festivalappmobile.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.data.remote.dto.EditeurCreateRequestDto
import com.example.festivalappmobile.data.remote.dto.EditeurUpdateRequestDto
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.usecases.editeur.CreateEditeurUseCase
import com.example.festivalappmobile.domain.usecases.editeur.GetEditeurByIdUseCase
import com.example.festivalappmobile.domain.usecases.editeur.UpdateEditeurUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class EditeurFormState(
    val id: Int? = null,
    val libelle: String = "",
    val phone: String = "",
    val email: String = "",
    val logo: String = "",
    val notes: String = "",
    val exposant: Boolean = false,
    val distributeur: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class EditeurFormEvent {
    object Success : EditeurFormEvent()
    data class Error(val message: String) : EditeurFormEvent()
}

sealed class EditeurFormUiEvent {
    data class EnteredLibelle(val value: String) : EditeurFormUiEvent()
    data class EnteredPhone(val value: String) : EditeurFormUiEvent()
    data class EnteredEmail(val value: String) : EditeurFormUiEvent()
    data class EnteredLogo(val value: String) : EditeurFormUiEvent()
    data class EnteredNotes(val value: String) : EditeurFormUiEvent()
    data class ChangedExposant(val value: Boolean) : EditeurFormUiEvent()
    data class ChangedDistributeur(val value: Boolean) : EditeurFormUiEvent()
    object SaveEditeur : EditeurFormUiEvent()
}

class EditeurFormViewModel(
    private val getEditeurByIdUseCase: GetEditeurByIdUseCase,
    private val createEditeurUseCase: CreateEditeurUseCase,
    private val updateEditeurUseCase: UpdateEditeurUseCase
) : ViewModel() {

    private val _state = mutableStateOf(EditeurFormState())
    val state: State<EditeurFormState> = _state

    private val _eventFlow = MutableSharedFlow<EditeurFormEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun loadEditeur(id: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val editeur = getEditeurByIdUseCase(id)
                if (editeur != null) {
                    _state.value = _state.value.copy(
                        id = editeur.id,
                        libelle = editeur.libelle,
                        phone = editeur.phone ?: "",
                        email = editeur.email ?: "",
                        logo = editeur.logo ?: "",
                        notes = editeur.notes ?: "",
                        exposant = editeur.exposant,
                        distributeur = editeur.distributeur,
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "Éditeur non trouvé")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Une erreur est survenue")
            }
        }
    }

    fun onEvent(event: EditeurFormUiEvent) {
        when (event) {
            is EditeurFormUiEvent.EnteredLibelle -> {
                _state.value = _state.value.copy(libelle = event.value)
            }
            is EditeurFormUiEvent.EnteredPhone -> {
                _state.value = _state.value.copy(phone = event.value)
            }
            is EditeurFormUiEvent.EnteredEmail -> {
                _state.value = _state.value.copy(email = event.value)
            }
            is EditeurFormUiEvent.EnteredLogo -> {
                _state.value = _state.value.copy(logo = event.value)
            }
            is EditeurFormUiEvent.EnteredNotes -> {
                _state.value = _state.value.copy(notes = event.value)
            }
            is EditeurFormUiEvent.ChangedExposant -> {
                _state.value = _state.value.copy(exposant = event.value)
            }
            is EditeurFormUiEvent.ChangedDistributeur -> {
                _state.value = _state.value.copy(distributeur = event.value)
            }
            is EditeurFormUiEvent.SaveEditeur -> {
                saveEditeur()
            }
        }
    }

    private fun saveEditeur() {
        viewModelScope.launch {
            if (_state.value.libelle.isBlank()) {
                _state.value = _state.value.copy(error = "Le nom de l'éditeur est obligatoire")
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val id = _state.value.id
                val result: Editeur? = if (id == null) {
                    createEditeurUseCase(
                        EditeurCreateRequestDto(
                            libelle = _state.value.libelle,
                            phone = _state.value.phone.ifBlank { null },
                            email = _state.value.email.ifBlank { null },
                            logo = _state.value.logo.ifBlank { null },
                            notes = _state.value.notes.ifBlank { null },
                            exposant = _state.value.exposant,
                            distributeur = _state.value.distributeur
                        )
                    )
                } else {
                    updateEditeurUseCase(
                        id,
                        EditeurUpdateRequestDto(
                            phone = _state.value.phone.ifBlank { null },
                            email = _state.value.email.ifBlank { null },
                            logo = _state.value.logo.ifBlank { null },
                            notes = _state.value.notes.ifBlank { null }
                        )
                    )
                }

                if (result != null) {
                    _eventFlow.emit(EditeurFormEvent.Success)
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = "Erreur lors de l'enregistrement")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Une erreur est survenue")
            }
        }
    }
}

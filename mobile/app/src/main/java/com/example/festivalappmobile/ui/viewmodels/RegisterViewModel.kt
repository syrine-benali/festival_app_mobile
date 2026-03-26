package com.example.festivalappmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.data.remote.RetrofitClient
import com.example.festivalappmobile.domain.models.User
import com.example.festivalappmobile.domain.usecases.RegisterUseCase
import com.example.festivalappmobile.data.repository.AuthRepositoryImpl
import com.example.festivalappmobile.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class RegisterViewModel : ViewModel() {

    private val registerUseCase = RegisterUseCase(
        AuthRepositoryImpl(RetrofitClient.instance)
    )

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email) }
    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password) }
    fun onNomChange(nom: String) = _uiState.update { it.copy(nom = nom) }
    fun onPrenomChange(prenom: String) = _uiState.update { it.copy(prenom = prenom) }

    fun register() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            android.util.Log.d("REGISTER", "Tentative d'inscription pour : ${uiState.value.email}")

            registerUseCase(
                uiState.value.email,
                uiState.value.password,
                uiState.value.nom,
                uiState.value.prenom
            )
                .onSuccess { user ->
                    android.util.Log.d("REGISTER", "Succès ! User : ${user.email}")
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, user = user) }
                }
                .onFailure { e ->
                    android.util.Log.e("REGISTER", "Erreur : ${e.message}")
                    val errorMessage = ErrorHandler.parseErrorMessage(e.message)
                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                }
        }
    }
}

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val nom: String = "",
    val prenom: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val user: User? = null
)

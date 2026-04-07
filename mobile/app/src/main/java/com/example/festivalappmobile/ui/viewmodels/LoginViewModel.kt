package com.example.festivalappmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.data.local.TokenManager
import com.example.festivalappmobile.data.remote.RetrofitClient
import com.example.festivalappmobile.domain.models.User
import com.example.festivalappmobile.domain.repository.AuthRepositoryImpl

import com.example.festivalappmobile.domain.usecases.LoginUseCase
import com.example.festivalappmobile.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class LoginViewModel (private val tokenManager: TokenManager? = null) : ViewModel() {

    private val loginUseCase = LoginUseCase(
        AuthRepositoryImpl(RetrofitClient.instance)
    )

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email) }
    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password) }

    fun login() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            loginUseCase(uiState.value.email, uiState.value.password)
                .onSuccess { user ->
                    // Sauvegarder le token si disponible
                    // Note: votre API utilise des cookies httpOnly, donc le token
                    // est géré automatiquement par le cookie jar
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, user = user) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val user: User? = null
)
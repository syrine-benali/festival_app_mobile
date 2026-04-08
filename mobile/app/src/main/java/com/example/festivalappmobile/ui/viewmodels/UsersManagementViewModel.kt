package com.example.festivalappmobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivalappmobile.data.remote.RetrofitClient
import com.example.festivalappmobile.domain.models.User
import com.example.festivalappmobile.domain.usecases.GetUsersUseCase
import com.example.festivalappmobile.domain.usecases.UpdateUserUseCase
import com.example.festivalappmobile.data.repository.UserRepositoryImpl
import com.example.festivalappmobile.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class UsersManagementViewModel : ViewModel() {

    private val userRepository = UserRepositoryImpl(RetrofitClient.instance)
    private val getUsersUseCase = GetUsersUseCase(userRepository)
    private val updateUserUseCase = UpdateUserUseCase(userRepository)

    private val _uiState = MutableStateFlow(UsersManagementUiState())
    val uiState: StateFlow<UsersManagementUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getUsersUseCase()
                .onSuccess { users ->
                    android.util.Log.d("USERS_MGMT", "Succès ! ${users.size} utilisateurs")
                    _uiState.update { it.copy(isLoading = false, users = users) }
                }
                .onFailure { e ->
                    android.util.Log.e("USERS_MGMT", "Erreur : ${e.message}")
                    val errorMessage = ErrorHandler.parseErrorMessage(e.message)
                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                }
        }
    }

    fun updateUserValidation(userId: Int, valide: Boolean, role: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, updateError = null) }

            updateUserUseCase(userId, valide, role)
                .onSuccess { updatedUser ->
                    android.util.Log.d("USERS_MGMT", "Utilisateur mis à jour : ${updatedUser.email}")
                    
                    // Mettre à jour l'utilisateur dans la liste
                    val updatedUsers = _uiState.value.users.map {
                        if (it.id == userId) updatedUser else it
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isUpdating = false, 
                            users = updatedUsers,
                            successMessage = "Utilisateur ${updatedUser.email} mis à jour avec succès"
                        )
                    }
                    
                    // Effacer le message après 3 secondes
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(3000)
                        _uiState.update { it.copy(successMessage = null) }
                    }
                }
                .onFailure { e ->
                    android.util.Log.e("USERS_MGMT", "Erreur update : ${e.message}")
                    val errorMessage = ErrorHandler.parseErrorMessage(e.message)
                    _uiState.update { it.copy(isUpdating = false, updateError = errorMessage) }
                }
        }
    }
}

data class UsersManagementUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val updateError: String? = null,
    val successMessage: String? = null
)

package com.example.festivalappmobile.domain.usecases

import com.example.festivalappmobile.domain.models.User
import com.example.festivalappmobile.domain.repository.AuthRepository


class LoginUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank())
            return Result.failure(Exception("Email et mot de passe requis"))
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return Result.failure(Exception("Email invalide"))
        return authRepository.login(email, password)
    }
}
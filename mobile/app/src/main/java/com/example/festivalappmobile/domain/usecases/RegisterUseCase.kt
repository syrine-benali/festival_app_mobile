package com.example.festivalappmobile.domain.usecases

import com.example.festivalappmobile.domain.models.User
import com.example.festivalappmobile.data.repository.AuthRepositoryImpl


class RegisterUseCase(private val authRepository: AuthRepositoryImpl) {

    suspend operator fun invoke(
        email: String,
        password: String,
        nom: String,
        prenom: String
    ): Result<User> {
        // Validations
        if (email.isBlank() || password.isBlank() || nom.isBlank() || prenom.isBlank()) {
            return Result.failure(Exception("Tous les champs sont requis"))
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Email invalide"))
        }
        
        // Validations du password selon les exigences du backend
        if (password.length < 8) {
            return Result.failure(Exception("Le mot de passe doit contenir au moins 8 caractères"))
        }
        
        if (!password.any { it.isUpperCase() }) {
            return Result.failure(Exception("Le mot de passe doit contenir au moins une majuscule (A-Z)"))
        }
        
        if (!password.any { it.isLowerCase() }) {
            return Result.failure(Exception("Le mot de passe doit contenir au moins une minuscule (a-z)"))
        }
        
        if (!password.any { it.isDigit() }) {
            return Result.failure(Exception("Le mot de passe doit contenir au moins un chiffre (0-9)"))
        }
        
        if (!password.any { !it.isLetterOrDigit() }) {
            return Result.failure(Exception("Le mot de passe doit contenir au moins un caractère spécial (!@#$%^&* etc)"))
        }
        
        if (nom.length < 2) {
            return Result.failure(Exception("Le nom doit contenir au moins 2 caractères"))
        }
        
        if (prenom.length < 2) {
            return Result.failure(Exception("Le prénom doit contenir au moins 2 caractères"))
        }
        
        return authRepository.register(email, password, nom, prenom)
    }
}


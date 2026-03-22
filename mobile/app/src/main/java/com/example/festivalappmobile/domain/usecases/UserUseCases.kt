package com.example.festivalappmobile.domain.usecases

import com.example.festivalappmobile.domain.models.User
import com.example.festivalappmobile.data.repository.UserRepositoryImpl

class GetUsersUseCase(private val userRepository: UserRepositoryImpl) {
    
    suspend operator fun invoke(): Result<List<User>> {
        return userRepository.getUsers()
    }
}

class UpdateUserUseCase(private val userRepository: UserRepositoryImpl) {
    
    suspend operator fun invoke(userId: Int, valide: Boolean, role: String): Result<User> {
        // Validations
        if (role.isBlank()) {
            return Result.failure(Exception("Le rôle est requis"))
        }
        
        return userRepository.updateUser(userId, valide, role)
    }
}

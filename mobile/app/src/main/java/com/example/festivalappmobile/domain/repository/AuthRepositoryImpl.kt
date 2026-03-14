package com.example.festivalappmobile.domain.repository

import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.dto.LoginRequestDto
import com.example.festivalappmobile.data.remote.dto.toDomain
import com.example.festivalappmobile.domain.models.User


class AuthRepositoryImpl(private val apiService: ApiService) : AuthRepository {


override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = apiService.login(LoginRequestDto(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.toDomain())// Convertir le DTO en domaine
            } else {
                Result.failure(Exception(response.body()?.message ?: "Erreur de connexion"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
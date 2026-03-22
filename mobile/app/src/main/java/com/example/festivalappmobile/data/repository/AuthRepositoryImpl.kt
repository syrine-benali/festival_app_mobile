package com.example.festivalappmobile.data.repository

import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.dto.LoginRequestDto
import com.example.festivalappmobile.data.remote.dto.RegisterRequestDto
import com.example.festivalappmobile.data.remote.dto.toDomain
import com.example.festivalappmobile.domain.models.User
import com.example.festivalappmobile.domain.repository.AuthRepository
import com.google.gson.Gson

// appelle l'api et retourne le resultat
class AuthRepositoryImpl(private val apiService: ApiService) : AuthRepository {
    
    private val gson = Gson()
    
    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val request = LoginRequestDto(email = username, password = password)
            val response = apiService.login(request)
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                Result.success(loginResponse.toDomain())
            } else {
                // Extraire le message d'erreur du backend
                val errorMessage = extractErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        email: String,
        password: String,
        nom: String,
        prenom: String
    ): Result<User> {
        return try {
            val request = RegisterRequestDto(
                email = email,
                password = password,
                nom = nom,
                prenom = prenom
            )
            val response = apiService.register(request)
            
            if (response.isSuccessful && response.body() != null) {
                val registerResponse = response.body()!!
                Result.success(registerResponse.toDomain())
            } else {
                // Extraire le message d'erreur du backend
                val errorMessage = extractErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Extrait le message d'erreur du corps de la réponse JSON du backend
     */
    private fun extractErrorMessage(errorBody: String?): String {
        return try {
            if (errorBody.isNullOrEmpty()) {
                return "Une erreur est survenue"
            }
            
            // Parser le JSON avec Gson
            val jsonObject = gson.fromJson(errorBody, Map::class.java) as? Map<*, *>
            
            // Essayer différentes clés communes
            val message = when {
                jsonObject?.containsKey("message") == true -> jsonObject["message"].toString()
                jsonObject?.containsKey("error") == true -> jsonObject["error"].toString()
                jsonObject?.containsKey("msg") == true -> jsonObject["msg"].toString()
                jsonObject?.containsKey("detail") == true -> jsonObject["detail"].toString()
                else -> errorBody
            }
            
            message
        } catch (e: Exception) {
            android.util.Log.e("AUTH_REPO", "Erreur parsing: ${e.message}")
            errorBody ?: "Une erreur est survenue"
        }
    }
}
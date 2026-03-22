package com.example.festivalappmobile.data.repository

import com.example.festivalappmobile.data.remote.ApiService
import com.example.festivalappmobile.data.remote.dto.UpdateUserRequestDto
import com.example.festivalappmobile.domain.models.User
import com.google.gson.Gson

class UserRepositoryImpl(private val apiService: ApiService) {
    
    private val gson = Gson()
    
    suspend fun getUsers(): Result<List<User>> {
        return try {
            val response = apiService.getUsers()
            
            if (response.isSuccessful && response.body() != null) {
                val users = response.body()!!.users.map { userDto ->
                    User(
                        id = userDto.id,
                        email = userDto.email,
                        nom = userDto.nom,
                        prenom = userDto.prenom,
                        role = userDto.role,
                        valide = userDto.valide
                    )
                }
                Result.success(users)
            } else {
                val errorMessage = extractErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUser(userId: Int, valide: Boolean, role: String): Result<User> {
        return try {
            val request = UpdateUserRequestDto(valide = valide, role = role)
            val response = apiService.updateUser(userId, request)
            
            if (response.isSuccessful && response.body() != null) {
                val userDto = response.body()!!.user
                val user = User(
                    id = userDto.id,
                    email = userDto.email,
                    nom = userDto.nom,
                    prenom = userDto.prenom,
                    role = userDto.role,
                    valide = userDto.valide
                )
                Result.success(user)
            } else {
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
            
            val jsonObject = gson.fromJson(errorBody, Map::class.java) as? Map<*, *>
            
            val message = when {
                jsonObject?.containsKey("message") == true -> jsonObject["message"].toString()
                jsonObject?.containsKey("error") == true -> jsonObject["error"].toString()
                else -> errorBody
            }
            
            message
        } catch (e: Exception) {
            android.util.Log.e("USER_REPO", "Erreur parsing: ${e.message}")
            errorBody ?: "Une erreur est survenue"
        }
    }
}

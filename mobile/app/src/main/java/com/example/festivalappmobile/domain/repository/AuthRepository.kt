package com.example.festivalappmobile.domain.repository

import com.example.festivalappmobile.domain.models.User

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
}



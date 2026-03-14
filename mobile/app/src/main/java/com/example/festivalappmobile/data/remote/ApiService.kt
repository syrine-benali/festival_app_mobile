package com.example.festivalappmobile.data.remote

import com.example.festivalappmobile.data.remote.dto.LoginRequestDto
import com.example.festivalappmobile.data.remote.dto.LoginResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// La liste des endpoints
// liste tous les appels HTTP disponibles
//Chaque fonction = un endpoint de ton API Retrofit se charge de faire l'appel HTTP tout seul
interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>
}
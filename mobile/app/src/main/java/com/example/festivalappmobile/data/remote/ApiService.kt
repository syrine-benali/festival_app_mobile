package com.example.festivalappmobile.data.remote

import com.example.festivalappmobile.data.remote.dto.LoginRequestDto
import com.example.festivalappmobile.data.remote.dto.LoginResponseDto
import com.example.festivalappmobile.data.remote.dto.ReservationDto
import com.example.festivalappmobile.data.remote.dto.FestivalDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// La liste des endpoints
// liste tous les appels HTTP disponibles
//Chaque fonction = un endpoint de ton API Retrofit se charge de faire l'appel HTTP tout seul
interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @GET("api/reservations")
    suspend fun getReservations(): Response<List<ReservationDto>>

    @GET("api/festivals")
    suspend fun getFestivals(): Response<List<FestivalDto>>
}
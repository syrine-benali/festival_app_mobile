package com.example.festivalappmobile.data.remote

import com.example.festivalappmobile.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path

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

    @GET("api/festivals/{id}")
    suspend fun getFestivalById(@Path("id") id: Int): Response<FestivalDto>

    @POST("api/festivals")
    suspend fun createFestival(@Body body: FestivalCreateRequestDto): Response<FestivalDto>

    @PUT("api/festivals/{id}")
    suspend fun updateFestival(@Path("id") id: Int, @Body body: FestivalUpdateRequestDto): Response<FestivalDto>

    @DELETE("api/festivals/{id}")
    suspend fun deleteFestival(@Path("id") id: Int): Response<FestivalDto>

    @GET("api/editeurs")
    suspend fun getAllEditeurs(): Response<EditeurListResponseDto>

    @GET("api/editeurs/{id}")
    suspend fun getEditeurById(@Path("id") id: Int): Response<EditeurResponseDto>

    @POST("api/editeurs")
    suspend fun createEditeur(@Body body: EditeurCreateRequestDto): Response<EditeurResponseDto>

    @PUT("api/editeurs/{id}")
    suspend fun updateEditeur(@Path("id") id: Int, @Body body: EditeurUpdateRequestDto): Response<EditeurResponseDto>

    @DELETE("api/editeurs/{id}")
    suspend fun deleteEditeur(@Path("id") id: Int): Response<EditeurDeleteResponseDto>
}
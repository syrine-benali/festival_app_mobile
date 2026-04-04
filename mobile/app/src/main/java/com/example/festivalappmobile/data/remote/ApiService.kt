package com.example.festivalappmobile.data.remote

import com.example.festivalappmobile.data.remote.dto.LoginRequestDto
import com.example.festivalappmobile.data.remote.dto.LoginResponseDto
import com.example.festivalappmobile.data.remote.dto.ReservationDto
import com.example.festivalappmobile.data.remote.dto.FestivalDto
import com.example.festivalappmobile.data.remote.dto.RegisterRequestDto
import com.example.festivalappmobile.data.remote.dto.UsersResponseDto
import com.example.festivalappmobile.data.remote.dto.UpdateUserRequestDto
import com.example.festivalappmobile.data.remote.dto.UpdateUserResponseDto
import com.example.festivalappmobile.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Query

// La liste des endpoints
// liste tous les appels HTTP disponibles
//Chaque fonction = un endpoint de ton API Retrofit se charge de faire l'appel HTTP tout seul
interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    // ===== RESERVATIONS =====
    // Dans ApiService.kt, ajouter après login :

    // ========== RÉSERVATIONS ==========
    @GET("api/reservations")
    suspend fun getReservations(
        @Query("festivalId") festivalId: Int? = null,
        @Query("workflowStatus") workflowStatus: String? = null
    ): Response<ReservationListResponseDto>

    @GET("api/reservations/{id}")
    suspend fun getReservationById(@Path("id") id: Int): Response<ReservationResponseDto>

    @POST("api/reservations")
    suspend fun createReservation(
        @Body request: CreateReservationRequestDto
    ): Response<ReservationResponseDto>

    @PUT("api/reservations/{id}")
    suspend fun updateReservation(
        @Path("id") id: Int,
        @Body request: UpdateReservationRequestDto
    ): Response<ReservationResponseDto>

    // ========== CONTACTS ==========
    @POST("api/reservations/{id}/contacts")
    suspend fun addContact(
        @Path("id") id: Int,
        @Body request: AddContactRequestDto
    ): Response<ReservationResponseDto>

    @DELETE("api/reservations/contacts/{contactId}")
    suspend fun deleteContact(@Path("contactId") contactId: Int): Response<Any>

    // ========== LIGNES ==========
    @POST("api/reservations/{id}/lines")
    suspend fun addLine(
        @Path("id") id: Int,
        @Body request: AddLineRequestDto
    ): Response<ReservationResponseDto>

    @DELETE("api/reservations/lines/{lineId}")
    suspend fun deleteLine(@Path("lineId") lineId: Int): Response<Any>

    // ========== JEUX ==========
    @POST("api/reservations/{id}/jeux")
    suspend fun addJeu(
        @Path("id") id: Int,
        @Body request: AddJeuRequestDto
    ): Response<ReservationResponseDto>

    @PUT("api/reservations/jeux/{jeuId}")
    suspend fun updateJeu(
        @Path("jeuId") jeuId: Int,
        //@Body request: UpdateJeuRequestDto  // TODO: créer cet fichier
    ): Response<ReservationResponseDto>

    @DELETE("api/reservations/jeux/{jeuId}")
    suspend fun deleteJeu(@Path("jeuId") jeuId: Int): Response<Any>

    // ===== FESTIVALS =====
    @GET("api/festivals")
    suspend fun getFestivals(): Response<List<FestivalDto>>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<LoginResponseDto>
    
    @GET("api/users/")
    suspend fun getUsers(): Response<UsersResponseDto>
    
    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") userId: Int,
        @Body request: UpdateUserRequestDto
    ): Response<UpdateUserResponseDto>
    @GET("api/festivals/{id}")
    suspend fun getFestivalById(@Path("id") id: Int): Response<FestivalDto>

    @POST("api/festivals")
    suspend fun createFestival(@Body body: FestivalCreateRequestDto): Response<FestivalDto>

    @PUT("api/festivals/{id}")
    suspend fun updateFestival(@Path("id") id: Int, @Body body: FestivalUpdateRequestDto): Response<FestivalDto>

    @DELETE("api/festivals/{id}")
    suspend fun deleteFestival(@Path("id") id: Int): Response<FestivalDto>

    // ===== EDITEURS =====

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
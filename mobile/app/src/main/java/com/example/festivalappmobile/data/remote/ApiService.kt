package com.example.festivalappmobile.data.remote

import com.example.festivalappmobile.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    // ========== RÉSERVATIONS ==========
    @GET("api/reservations")
    suspend fun getReservations(
        @Query("festivalId") festivalId: Int? = null,
        @Query("workflowStatus") workflowStatus: String? = null
    ): Response<ReservationListResponseDto>

    @GET("api/reservations/{id}")
    suspend fun getReservationById(@Path("id") id: Int): Response<ReservationResponseDto>

    @POST("api/reservations")
    suspend fun createReservation(@Body request: CreateReservationRequestDto): Response<ReservationResponseDto>

    @PUT("api/reservations/{id}")
    suspend fun updateReservation(@Path("id") id: Int, @Body request: UpdateReservationRequestDto): Response<ReservationResponseDto>

    @DELETE("api/reservations/{id}")
    suspend fun deleteReservation(@Path("id") id: Int): Response<ReservationDeleteResponseDto>

    // ========== CONTACTS ==========
    @POST("api/reservations/{id}/contacts")
    suspend fun addContact(@Path("id") id: Int, @Body request: AddContactRequestDto): Response<AddContactResponseDto>

    @DELETE("api/reservations/contacts/{contactId}")
    suspend fun deleteContact(@Path("contactId") contactId: Int): Response<Any>

    // ========== LIGNES ==========
    @POST("api/reservations/{id}/lines")
    suspend fun addLine(@Path("id") id: Int, @Body request: AddLineRequestDto): Response<AddLineResponseDto>

    @PUT("api/reservations/lines/{lineId}")
    suspend fun updateLine(@Path("lineId") lineId: Int, @Body request: UpdateLineRequestDto): Response<AddLineResponseDto>

    @DELETE("api/reservations/lines/{lineId}")
    suspend fun deleteLine(@Path("lineId") lineId: Int): Response<Any>

    // ========== JEUX DANS RÉSERVATIONS ==========
    @POST("api/reservations/{id}/jeux")
    suspend fun addJeu(@Path("id") id: Int, @Body request: AddJeuRequestDto): Response<AddJeuResponseDto>

    @PUT("api/reservations/jeux/{jeuId}")
    suspend fun updateJeu(@Path("jeuId") jeuId: Int, @Body request: UpdateJeuRequestDto): Response<AddJeuResponseDto>

    @DELETE("api/reservations/jeux/{jeuId}")
    suspend fun deleteJeu(@Path("jeuId") jeuId: Int): Response<Any>

    @GET("api/reservations/{id}/calculate-price")
    suspend fun calculatePrice(@Path("id") id: Int): Response<PriceCalculationResponseDto>

    // ===== FESTIVALS =====
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

    @POST("api/festivals/{festivalId}/zones-tarifaires")
    suspend fun addZoneTarifaire(@Path("festivalId") festivalId: Int, @Body body: AddZoneTarifaireRequestDto): Response<FestivalZoneTarifaireDto>

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

    // ===== JEUX (Catalogue) - fusion feat-roomDB + main =====
    @GET("api/jeux")
    suspend fun getJeux(): Response<JeuxResponseDto>

    @GET("api/jeux/editeur/{editeurId}")
    suspend fun getGamesByEditeur(@Path("editeurId") editeurId: Int): Response<GameListResponseDto>

    @POST("api/jeux")
    suspend fun createGame(@Body body: GameCreateRequestDto): Response<GameResponseDto>

    @PUT("api/jeux/{id}")
    suspend fun updateGame(@Path("id") id: Int, @Body body: GameUpdateRequestDto): Response<GameResponseDto>

    @DELETE("api/jeux/{id}")
    suspend fun deleteGame(@Path("id") id: Int): Response<Unit>

    // ===== AUTH & USERS =====
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<LoginResponseDto>

    @GET("api/users/")
    suspend fun getUsers(): Response<UsersResponseDto>

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") userId: Int, @Body request: UpdateUserRequestDto): Response<UpdateUserResponseDto>

    // ===== LEGACY ENDPOINTS (feat-roomDB compatibility) =====
    @GET("api/reservations")
    suspend fun getReservationsWrapped(): Response<ReservationsListResponseDto>
}
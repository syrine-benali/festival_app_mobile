package com.example.festivalappmobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UsersResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("users") val users: List<UserDto>
)

data class UpdateUserRequestDto(
    @SerializedName("valide") val valide: Boolean,
    @SerializedName("role") val role: String
)

data class UpdateUserResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: UserDto
)

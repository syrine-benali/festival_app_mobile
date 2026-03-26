package com.example.festivalappmobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("nom") val nom: String,
    @SerializedName("prenom") val prenom: String
)
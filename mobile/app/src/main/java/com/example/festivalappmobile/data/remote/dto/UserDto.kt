package com.example.festivalappmobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("nom") val nom: String,
    @SerializedName("prenom") val prenom: String,
    @SerializedName("role") val role: String,
    @SerializedName("valide") val valide: Boolean
)

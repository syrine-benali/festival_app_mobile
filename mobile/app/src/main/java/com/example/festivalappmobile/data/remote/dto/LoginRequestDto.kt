package com.example.festivalappmobile.data.remote.dto

import com.google.gson.annotations.SerializedName

// dans dto y'a les objets de transfert , ce sont les classes qui reprente les donnes que l'api
// envoie et recoit

// ici c'est la request
data class LoginRequestDto(
    @SerializedName("email") val email : String,
    @SerializedName("password") val password : String
)

package com.example.festivalappmobile.data.remote.dto

import com.example.festivalappmobile.domain.models.User
import com.google.gson.annotations.SerializedName


// ici c'est la response
data class LoginResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: UserDto
)

// on ajoute le mapping
fun LoginResponseDto.toDomain(): User {
    return User(
        id = this.user.id,
        email = this.user.email,
        nom = this.user.nom,
        prenom = this.user.prenom,
        role = this.user.role,
        valide = this.user.valide
    )
}

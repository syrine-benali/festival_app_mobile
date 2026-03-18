package com.example.festivalappmobile.data.remote.dto

// Dto used to communicate with the API (matches the backend models)
data class FestivalDto(
    val id: Int,
    val nom: String,
    val lieu: String,
    val dateDebut: String,
    val dateFin: String
)

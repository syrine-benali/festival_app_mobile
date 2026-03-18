package com.example.festivalappmobile.domain.models

// dto used throughout the app for festival
data class Festival(
    val id: Int,
    val nom: String,
    val lieu: String,
    val dateDebut: String,
    val dateFin: String
)

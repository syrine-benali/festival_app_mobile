package com.example.festivalappmobile.domain.models

data class ReservationJeu(
    val id: Int,
    val jeuId: Int,
    val jeuLibelle: String,
    val editeurJeuId: Int?,
    val editeurJeuLibelle: String?,
    val nbExemplaires: Int,
    val nbTablesAllouees: Int
)
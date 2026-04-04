package com.example.festivalappmobile.domain.models

data class ReservationJeu(
    val id: Int,
    val jeuId: Int,
    val jeuLibelle: String,
    val editeurJeuId: Int?,
    val editeurJeuLibelle: String?,
    val zonePlanId: Int?,
    val zonePlanNom: String?,
    val nbExemplaires: Int,
    val nbTablesAllouees: Int
)
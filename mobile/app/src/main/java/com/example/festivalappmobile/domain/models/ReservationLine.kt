package com.example.festivalappmobile.domain.models

data class ReservationLine(
    val id: Int,
    val zoneTarifaireId: Int,
    val zoneTarifaireNom: String,
    val prixTable: Double,
    val nbTables: Int,
    val nbM2: Double,
    val grandesTablesSouhaitees: Boolean,
    val sousTotal: Double
)
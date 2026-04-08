package com.example.festivalappmobile.domain.models

data class ReservationLine(
    val id: Int,
    val pricingId: Int,
    val pricingLabel: String,
    val tablePrice: Double,
    val nbTables: Int,
    val nbM2: Double,
    val grandesTablesSouhaitees: Boolean,
    val sousTotal: Double
)
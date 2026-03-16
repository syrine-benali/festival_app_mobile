package com.example.festivalappmobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey val id: Int,
    val editeurId: Int,
    val festivalId: Int,
    val workflowStatus: String,
    val typeReservant: String,
    val viendraPresenteSesJeux: Boolean,
    val editeurNom: String?
)
package com.example.festivalappmobile.domain.models


data class Reservation(
    val id: Int,
    val editeurId: Int,
    val festivalId: Int,
    val workflowStatus: WorkflowStatus,
    val typeReservant: TypeReservant,
    val viendraPresenteSesJeux: Boolean,
    val editeurNom: String? = null
)
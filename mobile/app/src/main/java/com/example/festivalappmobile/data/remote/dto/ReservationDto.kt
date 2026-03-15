package com.example.festivalappmobile.data.remote.dto

import com.example.festivalappmobile.domain.models.TypeReservant
import com.example.festivalappmobile.domain.models.WorkflowStatus

data class ReservationDto(
    val id: Int,
    val editeurId: Int,
    val festivalId: Int,
    val workflowStatus: String,
    val typeReservant: String,
    val viendra_presente_ses_jeux: Boolean,
    val editeur: EditeurDto?
)

data class EditeurDto(val id: Int, val libelle: String)
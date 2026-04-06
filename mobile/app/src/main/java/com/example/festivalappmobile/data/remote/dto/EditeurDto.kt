package com.example.festivalappmobile.data.remote.dto

import com.example.festivalappmobile.domain.models.WorkflowStatus

data class EditeurDto(
    val id: Int,
    val libelle: String,
    val exposant: Boolean,
    val distributeur: Boolean,
    val logo: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val notes: String? = null,
    val workflowStatus: String? = null,
    val hasReservation: Boolean = false
)

data class EditeurListResponseDto(
    val success: Boolean,
    val editeurs: List<EditeurDto>,
    val total: Int
)

data class EditeurResponseDto(
    val success: Boolean,
    val editeur: EditeurDto,
    val message: String? = null
)

data class EditeurCreateRequestDto(
    val libelle: String,
    val exposant: Boolean? = null,
    val distributeur: Boolean? = null,
    val phone: String? = null,
    val email: String? = null,
    val logo: String? = null,
    val notes: String? = null
)

data class EditeurUpdateRequestDto(
    val phone: String? = null,
    val email: String? = null,
    val logo: String? = null,
    val notes: String? = null
)

data class EditeurDeleteResponseDto(
    val success: Boolean,
    val message: String
)

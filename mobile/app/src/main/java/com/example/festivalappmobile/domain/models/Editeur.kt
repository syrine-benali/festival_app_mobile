package com.example.festivalappmobile.domain.models

data class Editeur(
    val id: Int,
    val libelle: String,
    val exposant: Boolean,
    val distributeur: Boolean,
    val logo: String?,
    val phone: String?,
    val email: String?,
    val notes: String?,
    val workflowStatus: WorkflowStatus?,
    val hasReservation: Boolean
)

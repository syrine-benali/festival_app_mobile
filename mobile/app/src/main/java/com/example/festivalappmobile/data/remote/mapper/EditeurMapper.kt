package com.example.festivalappmobile.data.remote.mapper

import com.example.festivalappmobile.data.remote.dto.EditeurDto
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.models.WorkflowStatus

// // service used to make the API DTO and the app dto compatible. Used by the EditeurRepositoryImpl
fun EditeurDto.toDomain(): Editeur {
    return Editeur(
        id = id,
        libelle = libelle,
        exposant = exposant,
        distributeur = distributeur,
        logo = logo,
        phone = phone,
        email = email,
        notes = notes,
        workflowStatus = workflowStatus?.let { 
            try { 
                WorkflowStatus.valueOf(it) 
            } catch (e: IllegalArgumentException) { 
                null 
            } 
        },
        hasReservation = hasReservation
    )
}

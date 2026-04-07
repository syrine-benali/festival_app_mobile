package com.example.festivalappmobile.domain.models

enum class WorkflowStatus(val label: String) {
    PAS_DE_CONTACT("Pas encore de contact"),
    CONTACT_PRIS("Contact pris"),
    DISCUSSION_EN_COURS("Discussion en cours"),
    SERA_ABSENT("Sera absent"),
    CONSIDERE_ABSENT("Considéré absent"),
    PRESENT("Présent"),
    FACTURE("Facturé"),
    FACTURE_PAYEE("Facture payée");

    companion object {
        fun fromString(value: String): WorkflowStatus =
            entries.firstOrNull { it.name == value } ?: PAS_DE_CONTACT
    }
}
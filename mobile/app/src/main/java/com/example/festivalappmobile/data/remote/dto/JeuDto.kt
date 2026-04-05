package com.example.festivalappmobile.data.remote.dto

// DTO correspondant à la réponse de GET /api/jeux
data class JeuDto(
    val id: Int,
    val libelle: String,
    val auteur: String? = null,
    val nbMinJoueur: Int? = null,
    val nbMaxJoueur: Int? = null,
    val ageMin: Int? = null,
    val duree: Int? = null,
    val prototype: Boolean = false,
    val image: String? = null,
    val editeur: EditeurRefDto? = null,
    val typeJeu: TypeJeuRefDto? = null
)

// Référence simplifiée à un éditeur (telle que retournée dans les jeux)
data class EditeurRefDto(
    val id: Int,
    val libelle: String
)

// Référence simplifiée à un type de jeu
data class TypeJeuRefDto(
    val id: Int,
    val libelle: String
)

// Wrapper de la réponse de l'API : { success, jeux, total }
data class JeuxResponseDto(
    val success: Boolean,
    val jeux: List<JeuDto>,
    val total: Int
)

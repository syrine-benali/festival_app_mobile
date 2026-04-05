package com.example.festivalappmobile.domain.models

// Modèle domaine pour un jeu de société
data class Jeu(
    val id: Int,
    val libelle: String,
    val auteur: String? = null,
    val nbMinJoueur: Int? = null,
    val nbMaxJoueur: Int? = null,
    val ageMin: Int? = null,
    val duree: Int? = null,
    val prototype: Boolean = false,
    val image: String? = null,
    val editeurId: Int? = null,
    val editeurNom: String? = null,
    val typeJeuId: Int? = null,
    val typeJeuNom: String? = null
)

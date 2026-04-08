package com.example.festivalappmobile.domain.models

data class Game(
    val id: Int,
    val libelle: String,
    val auteur: String?,
    val nbMinJoueur: Int?,
    val nbMaxJoueur: Int?,
    val ageMin: Int?,
    val duree: Int?,
    val prototype: Boolean,
    val image: String?,
    val theme: String?,
    val description: String?,
    val idEditeur: Int?,
    val editeurName: String? = null,
    val idTypeJeu: Int?,
    val typeJeuName: String? = null
)

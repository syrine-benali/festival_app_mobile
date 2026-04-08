package com.example.festivalappmobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GameDto(
    @SerializedName("id") val id: Int,
    @SerializedName("libelle") val libelle: String,
    @SerializedName("auteur") val auteur: String?,
    @SerializedName("nbMinJoueur") val nbMinJoueur: Int?,
    @SerializedName("nbMaxJoueur") val nbMaxJoueur: Int?,
    @SerializedName("ageMin") val ageMin: Int?,
    @SerializedName("duree") val duree: Int?,
    @SerializedName("prototype") val prototype: Boolean,
    @SerializedName("image") val image: String?,
    @SerializedName("theme") val theme: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("idEditeur") val idEditeur: Int?,
    @SerializedName("idTypeJeu") val idTypeJeu: Int?,
    @SerializedName("editeur") val editeur: GameEditeurDto?,
    @SerializedName("typeJeu") val typeJeu: GameTypeJeuDto?
)

data class GameEditeurDto(
    @SerializedName("id") val id: Int,
    @SerializedName("libelle") val libelle: String
)

data class GameTypeJeuDto(
    @SerializedName("id") val id: Int,
    @SerializedName("libelle") val libelle: String
)

data class GameListResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("jeux") val jeux: List<GameDto>,
    @SerializedName("total") val total: Int
)

data class GameResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("jeu") val jeu: GameDto?
)

data class GameCreateRequestDto(
    @SerializedName("libelle") val libelle: String,
    @SerializedName("auteur") val auteur: String? = null,
    @SerializedName("nbMinJoueur") val nbMinJoueur: Int? = null,
    @SerializedName("nbMaxJoueur") val nbMaxJoueur: Int? = null,
    @SerializedName("ageMin") val ageMin: Int? = null,
    @SerializedName("duree") val duree: Int? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("theme") val theme: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("idEditeur") val idEditeur: Int? = null,
    @SerializedName("idTypeJeu") val idTypeJeu: Int? = null
)

data class GameUpdateRequestDto(
    @SerializedName("libelle") val libelle: String? = null,
    @SerializedName("auteur") val auteur: String? = null,
    @SerializedName("nbMinJoueur") val nbMinJoueur: Int? = null,
    @SerializedName("nbMaxJoueur") val nbMaxJoueur: Int? = null,
    @SerializedName("ageMin") val ageMin: Int? = null,
    @SerializedName("duree") val duree: Int? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("theme") val theme: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("idEditeur") val idEditeur: Int? = null,
    @SerializedName("idTypeJeu") val idTypeJeu: Int? = null
)

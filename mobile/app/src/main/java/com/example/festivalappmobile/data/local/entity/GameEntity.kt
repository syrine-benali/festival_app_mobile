package com.example.festivalappmobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.festivalappmobile.domain.models.Game

/**
 * Entité Room pour le cache local des jeux.
 */
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: Int,
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
    val editeurName: String?,
    val idTypeJeu: Int?,
    val typeJeuName: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)

fun GameEntity.toDomain() = Game(
    id = id,
    libelle = libelle,
    auteur = auteur,
    nbMinJoueur = nbMinJoueur,
    nbMaxJoueur = nbMaxJoueur,
    ageMin = ageMin,
    duree = duree,
    prototype = prototype,
    image = image,
    theme = theme,
    description = description,
    idEditeur = idEditeur,
    editeurName = editeurName,
    idTypeJeu = idTypeJeu,
    typeJeuName = typeJeuName
)

fun Game.toEntity() = GameEntity(
    id = id,
    libelle = libelle,
    auteur = auteur,
    nbMinJoueur = nbMinJoueur,
    nbMaxJoueur = nbMaxJoueur,
    ageMin = ageMin,
    duree = duree,
    prototype = prototype,
    image = image,
    theme = theme,
    description = description,
    idEditeur = idEditeur,
    editeurName = editeurName,
    idTypeJeu = idTypeJeu,
    typeJeuName = typeJeuName
)

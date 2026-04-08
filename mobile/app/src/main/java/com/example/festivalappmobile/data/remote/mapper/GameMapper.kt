package com.example.festivalappmobile.data.remote.mapper

import com.example.festivalappmobile.data.remote.dto.*
import com.example.festivalappmobile.domain.models.Game

fun GameDto.toDomain(): Game {
    return Game(
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
        editeurName = editeur?.libelle,
        idTypeJeu = idTypeJeu,
        typeJeuName = typeJeu?.libelle
    )
}

fun Game.toCreateRequest(): GameCreateRequestDto {
    return GameCreateRequestDto(
        libelle = libelle,
        auteur = auteur,
        nbMinJoueur = nbMinJoueur,
        nbMaxJoueur = nbMaxJoueur,
        ageMin = ageMin,
        duree = duree,
        image = image,
        theme = theme,
        description = description,
        idEditeur = idEditeur,
        idTypeJeu = idTypeJeu
    )
}

fun Game.toUpdateRequest(): GameUpdateRequestDto {
    return GameUpdateRequestDto(
        libelle = libelle,
        auteur = auteur,
        nbMinJoueur = nbMinJoueur,
        nbMaxJoueur = nbMaxJoueur,
        ageMin = ageMin,
        duree = duree,
        image = image,
        theme = theme,
        description = description,
        idEditeur = idEditeur,
        idTypeJeu = idTypeJeu
    )
}

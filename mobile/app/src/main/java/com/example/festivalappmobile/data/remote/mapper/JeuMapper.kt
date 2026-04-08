package com.example.festivalappmobile.data.remote.mapper

import com.example.festivalappmobile.data.remote.dto.JeuDto
import com.example.festivalappmobile.domain.models.Game
import com.example.festivalappmobile.domain.models.Jeu

// Convertit un JeuDto (réponse API) en Jeu (modèle domaine — branche feat-roomDB)
fun JeuDto.toDomain(): Jeu {
    return Jeu(
        id = id,
        libelle = libelle,
        auteur = auteur,
        nbMinJoueur = nbMinJoueur,
        nbMaxJoueur = nbMaxJoueur,
        ageMin = ageMin,
        duree = duree,
        prototype = prototype,
        image = image,
        editeurId = editeur?.id,
        editeurNom = editeur?.libelle,
        typeJeuId = typeJeu?.id,
        typeJeuNom = typeJeu?.libelle
    )
}

// Convertit un JeuDto en Game (modèle domaine — branche main des collègues)
// Game et Jeu représentent la même ressource /api/jeux avec des nommages différents.
fun JeuDto.toGame(): Game {
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
        theme = null,
        description = null,
        idEditeur = editeur?.id,
        editeurName = editeur?.libelle,
        idTypeJeu = typeJeu?.id,
        typeJeuName = typeJeu?.libelle
    )
}

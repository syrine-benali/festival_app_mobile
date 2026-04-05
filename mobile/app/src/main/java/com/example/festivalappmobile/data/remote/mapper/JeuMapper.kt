package com.example.festivalappmobile.data.remote.mapper

import com.example.festivalappmobile.data.remote.dto.JeuDto
import com.example.festivalappmobile.domain.models.Jeu

// Convertit un JeuDto (réponse API) en Jeu (modèle domaine)
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

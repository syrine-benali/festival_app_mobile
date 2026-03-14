package com.example.festivalappmobile.domain.models

// classe de donnnees pures , le coeur de l'application
// ici on a user
// on va travailler avec les models par example user ici , de cette maniere
//on touche pas a la vlauer de l'api, si un jour le token changer on aura pas de souci
// si on utilise loginresponseDTO partout dans l'application c tres dangereux
// si on chnage le backend de l'api le code va casser et tout tombe

data class User(
    val id: Int,
    val email: String,
    val nom: String,
    val prenom: String,
    val role: String,
    val valide: Boolean
)
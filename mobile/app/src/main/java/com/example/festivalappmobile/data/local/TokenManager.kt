package com.example.festivalappmobile.data.local

import android.content.Context
import android.content.SharedPreferences

//sert à assurer la persistance du jeton d'authentification JWT
class TokenManager(context: Context) {
    //enregistre les données dans un petit fihier physique dans un petit fichier
    //sur le disque du telephone (utilisé pour faire en sorte que l'user reste co
    // même s'il ferme et rouvre l'app
    private val prefs: SharedPreferences =
        context.getSharedPreferences("festival_prefs", Context.MODE_PRIVATE)

    //sauvegarder et récupérer le token après qu'une connexion a été effectuée
    fun saveToken(token: String) = prefs.edit().putString("jwt_token", token).apply()

    //utilisée à chaque fois que l'user doit prouver son identité pour accéder à une ressource protégée
    fun getToken(): String? = prefs.getString("jwt_token", null)

    //supprime le token de stockage (utilisée pour la déconnexion)
    fun clearToken() = prefs.edit().remove("jwt_token").apply()
}
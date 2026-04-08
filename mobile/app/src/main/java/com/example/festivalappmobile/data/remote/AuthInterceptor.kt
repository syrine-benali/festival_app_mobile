package com.example.festivalappmobile.data.remote

import okhttp3.Interceptor
import okhttp3.Response

//les routes back sont protégées et exignet qu'on prouve qui on est
// en envoyant un token. Donc au lieu de le faire manuellement dans
// chaque fonction dans ApiService, cette classe le fait une seule fois
//pour toutes les requêtes

class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        //récupérer le token
        val token = tokenProvider()
        val request = if (token != null) {
            //si l'user est co, on crée une nv version de la requête
            chain.request().newBuilder() //pour préparer une version modifiée de la requête
                //avec le token
                .addHeader("Authorization", "Bearer $token")
                .build() //pour valider la nouvelle version
        } else {
            //permet de voir l'objet qui arrive à ce niveau
            chain.request()
        }
        //ordre de laisser passer. sans ceci la requête s'arrête et n'atteint pas le serveur
        return chain.proceed(request)
    }
}
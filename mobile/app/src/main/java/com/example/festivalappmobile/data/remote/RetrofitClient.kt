package com.example.festivalappmobile.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// le dossier remote c'est pour tout ce qui est en relation avec l'api
// La configuration de la connexion avec l'api c'est ici
//C'est le fichier qui crée l'instance Retrofit configurée une seule fois pour toute l'app
// c'est comme le numero de telephone de l'api
object RetrofitClient {
    private const val BASE_URL = "https://api-festival-app.ferhatsn.fr/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .build()
            )
            .build()
            .create(ApiService::class.java)
    }
}
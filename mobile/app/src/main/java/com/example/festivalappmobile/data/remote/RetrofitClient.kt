package com.example.festivalappmobile.data.remote

import com.example.festivalappmobile.data.local.TokenManager
import okhttp3.CookieJar
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date


/**
 * Simple CookieJar pour gérer les cookies automatiquement
 * Nécessaire pour que OkHttp stocke et renvoie le cookie auth_token du backend
 */
class SimpleCookieJar : CookieJar {
    private val cookieStore = mutableListOf<Cookie>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore.removeAll { it.name == "auth_token" }
        cookieStore.addAll(cookies)
        if (cookies.isNotEmpty()) {
            android.util.Log.d("CookieJar", " Saved ${cookies.size} cookies")
            cookies.forEach { cookie ->
                android.util.Log.d("CookieJar", "  - ${cookie.name} = ${cookie.value}")
            }
        }
    }
    
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val validCookies = cookieStore.filter { it.expiresAt > Date().time }
        if (validCookies.isNotEmpty()) {
            android.util.Log.d("CookieJar", "Sending ${validCookies.size} cookies to ${url.host}")
            validCookies.forEach { cookie ->
                android.util.Log.d("CookieJar", "  - ${cookie.name}")
            }
        }
        return validCookies
    }
}


// le dossier remote c'est pour tout ce qui est en relation avec l'api
// La configuration de la connexion avec l'api c'est ici
//C'est le fichier qui crée l'instance Retrofit configurée une seule fois pour toute l'app
// c'est comme le numero de telephone de l'api


object RetrofitClient {
    private const val BASE_URL = "https://api-festival-app.ferhatsn.fr/"
    private var tokenManager: TokenManager? = null

    fun init(tm: TokenManager) {
        tokenManager = tm
    }

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(AuthInterceptor { tokenManager?.getToken() })
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .cookieJar(SimpleCookieJar())
                    .build()
            )
            .build()
            .create(ApiService::class.java)
    }
}
package com.example.festivalappmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import com.example.festivalappmobile.ui.screen.LoginScreen
import com.example.festivalappmobile.ui.theme.FestivalAppMobileTheme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.festivalappmobile.domain.models.User


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FestivalAppMobileTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    var loggedUser by remember { mutableStateOf<User?>(null) }

                    if (loggedUser == null) {
                        LoginScreen(
                            onLoginSuccess = { user ->
                                loggedUser = user
                            }
                        )
                    } else {
                        // Écran temporaire après login
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Bienvenue ${loggedUser!!.prenom} ${loggedUser!!.nom} !")
                            Text("Role : ${loggedUser!!.role}")
                            Text("Email : ${loggedUser!!.email}")
                        }
                    }
                }
            }
        }
    }
}
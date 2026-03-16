package com.example.festivalappmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.festivalappmobile.domain.models.User
import com.example.festivalappmobile.ui.screen.LoginScreen
import com.example.festivalappmobile.ui.theme.FestivalAppMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FestivalAppMobileTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val rootNavController = rememberNavController()
    var loggedUser by remember { mutableStateOf<User?>(null) }

    NavHost(navController = rootNavController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { user ->
                    loggedUser = user
                    rootNavController.navigate("main_screen") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("main_screen") {
            MainScreen(
                user = loggedUser,
                onLogout = {
                    loggedUser = null
                    rootNavController.navigate("login") {
                        popUpTo("main_screen") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun MainScreen(user: User?, onLogout: () -> Unit) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Réservations") },
                    label = { Text("Réservations") },
                    selected = currentRoute == "reservations",
                    onClick = {
                        bottomNavController.navigate("reservations") {
                            popUpTo(bottomNavController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Mon Compte") },
                    label = { Text("Mon Compte") },
                    selected = currentRoute == "profile",
                    onClick = {
                        bottomNavController.navigate("profile") {
                            popUpTo(bottomNavController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "reservations",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("reservations") {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("C'est ici qu'apparaîtra la liste des réservations !")
                }
            }

            composable("profile") {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (user != null) {
                        Text("Bienvenue ${user.prenom} ${user.nom} !")
                        Text("Role : ${user.role}")
                        Text("Email : ${user.email}")
                    }

                    Button(
                        onClick = onLogout,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Se déconnecter")
                    }
                }
            }
        }
    }
}
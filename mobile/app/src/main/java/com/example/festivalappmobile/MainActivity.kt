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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.Star
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.festivalappmobile.ui.screen.DashboardScreen
import com.example.festivalappmobile.ui.screen.LoginScreen
import com.example.festivalappmobile.ui.screen.RegisterScreen
import com.example.festivalappmobile.ui.screen.UsersAdminScreen
import com.example.festivalappmobile.ui.theme.FestivalAppMobileTheme
import com.example.festivalappmobile.data.local.AppDatabase
import com.example.festivalappmobile.data.remote.RetrofitClient
import com.example.festivalappmobile.data.repository.DashboardCacheRepository
import com.example.festivalappmobile.data.repository.EditeurRepositoryImpl
import com.example.festivalappmobile.data.repository.FestivalRepositoryImpl
import com.example.festivalappmobile.data.repository.JeuRepositoryImpl
import com.example.festivalappmobile.data.repository.ReservationDashboardRepositoryImpl
import com.example.festivalappmobile.domain.usecases.editeur.GetEditeursUseCase
import com.example.festivalappmobile.domain.usecases.festival.GetFestivalsUseCase
import com.example.festivalappmobile.domain.usecases.jeu.GetJeuxUseCase
import com.example.festivalappmobile.ui.screen.FestivalListScreen
import com.example.festivalappmobile.ui.viewmodels.DashboardViewModel
import com.example.festivalappmobile.ui.viewmodels.FestivalListViewModel
import com.example.festivalappmobile.ui.viewmodels.UsersManagementViewModel
import com.example.festivalappmobile.utils.NetworkChecker

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
    var showValidationDialog by remember { mutableStateOf(false) }
    var registeredUserName by remember { mutableStateOf("") }

    // Dépendances partagées pour le mode offline (Room + NetworkChecker)
    val context = androidx.compose.ui.platform.LocalContext.current
    val appDatabase = remember { AppDatabase.getInstance(context) }
    val networkChecker = remember { NetworkChecker(context) }
    val dashboardCacheRepo = remember {
        DashboardCacheRepository(
            festivalDao = appDatabase.dashboardFestivalDao(),
            jeuDao      = appDatabase.dashboardJeuDao(),
            editeurDao  = appDatabase.dashboardEditeurDao()
        )
    }

    if (showValidationDialog) {
        AlertDialog(
            onDismissRequest = {
                showValidationDialog = false
                rootNavController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
            },
            title = { Text("Inscription réussie") },
            text = { Text("Bienvenue $registeredUserName ! Votre compte a été créé avec succès.\n\nVeuillez attendre la validation de l'administrateur pour pouvoir avoir accès à l'application.") },
            confirmButton = {
                Button(
                    onClick = {
                        showValidationDialog = false
                        rootNavController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    NavHost(navController = rootNavController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { user ->
                    loggedUser = user
                    rootNavController.navigate("main_screen") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    rootNavController.navigate("register")
                },
                onNavigateToDashboard = {
                    rootNavController.navigate("dashboard")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegistrationSuccess = { user ->
                    registeredUserName = "${user.prenom} ${user.nom}"
                    showValidationDialog = true
                },
                onNavigateToLogin = {
                    rootNavController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    rootNavController.navigate("dashboard")
                }
            )
        }

        // Dashboard accessible publiquement depuis Login et Register
        composable("dashboard") {
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        val api = RetrofitClient.instance
                        val festivalRepo = FestivalRepositoryImpl(api)
                        val jeuRepo = JeuRepositoryImpl(api)
                        val editeurRepo = EditeurRepositoryImpl(api)
                        val reservationRepo = ReservationDashboardRepositoryImpl(api)
                        @Suppress("UNCHECKED_CAST")
                        return DashboardViewModel(
                            getFestivalsUseCase = GetFestivalsUseCase(festivalRepo),
                            getJeuxUseCase = GetJeuxUseCase(jeuRepo),
                            getEditeursUseCase = GetEditeursUseCase(editeurRepo),
                            reservationRepo = reservationRepo,
                            cacheRepo = dashboardCacheRepo,
                            networkChecker = networkChecker
                        ) as T
                    }
                }
            )
            DashboardScreen(viewModel = dashboardViewModel)
        }

        composable("main_screen") {
            MainScreen(
                user = loggedUser,
                dashboardCacheRepo = dashboardCacheRepo,
                networkChecker = networkChecker,
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
fun MainScreen(
    user: User?,
    dashboardCacheRepo: DashboardCacheRepository,
    networkChecker: NetworkChecker,
    onLogout: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (user != null) {
        android.util.Log.d("MAINSCREEN", "User logged in - Role: '${user.role}' (ADMIN check: ${user.role == "ADMIN"})")
    } else {
        android.util.Log.d("MAINSCREEN", "No user logged in")
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                // Tableau de bord (home)
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Tableau de bord") },
                    label = { Text("Accueil") },
                    selected = currentRoute == "dashboard_tab",
                    onClick = {
                        bottomNavController.navigate("dashboard_tab") {
                            popUpTo(bottomNavController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )

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
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Festivals") },
                    label = { Text("Festivals") },
                    selected = currentRoute == "festivals",
                    onClick = {
                        bottomNavController.navigate("festivals") {
                            popUpTo(bottomNavController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )

                // Onglet Utilisateurs (admins uniquement)
                if (user != null && (user.role == "ADMIN" || user.role == "SUPER_ORGANISATEUR")) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Utilisateurs") },
                        label = { Text("Utilisateurs") },
                        selected = currentRoute == "users",
                        onClick = {
                            bottomNavController.navigate("users") {
                                popUpTo(bottomNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }

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
            startDestination = "dashboard_tab",  // Le dashboard est l'écran d'accueil
            modifier = Modifier.padding(innerPadding)
        ) {

            // Tableau de bord — onglet principal
            composable("dashboard_tab") {
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            val api = RetrofitClient.instance
                            val festivalRepo = FestivalRepositoryImpl(api)
                            val jeuRepo = JeuRepositoryImpl(api)
                            val editeurRepo = EditeurRepositoryImpl(api)
                            val reservationRepo = ReservationDashboardRepositoryImpl(api)
                            @Suppress("UNCHECKED_CAST")
                            return DashboardViewModel(
                                getFestivalsUseCase = GetFestivalsUseCase(festivalRepo),
                                getJeuxUseCase = GetJeuxUseCase(jeuRepo),
                                getEditeursUseCase = GetEditeursUseCase(editeurRepo),
                                reservationRepo = reservationRepo,
                                cacheRepo = dashboardCacheRepo,
                                networkChecker = networkChecker
                            ) as T
                        }
                    }
                )
                DashboardScreen(viewModel = dashboardViewModel)
            }

            composable("reservations") {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("C'est ici qu'apparaîtra la liste des réservations !")
                }
            }

            composable("festivals") {
                val viewModel: FestivalListViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            val api = RetrofitClient.instance
                            val repo = FestivalRepositoryImpl(api)
                            val useCase = GetFestivalsUseCase(repo)
                            @Suppress("UNCHECKED_CAST")
                            return FestivalListViewModel(useCase) as T
                        }
                    }
                )
                FestivalListScreen(viewModel = viewModel)
            }

            composable("users") {
                val viewModel: UsersManagementViewModel = viewModel()
                UsersAdminScreen(viewModel = viewModel)
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

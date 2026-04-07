package com.example.festivalappmobile

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.festivalappmobile.ui.screen.RegisterScreen
import com.example.festivalappmobile.ui.screen.UsersAdminScreen
import com.example.festivalappmobile.ui.theme.FestivalAppMobileTheme
import com.example.festivalappmobile.data.remote.RetrofitClient
import com.example.festivalappmobile.data.repository.FestivalRepositoryImpl
import com.example.festivalappmobile.domain.usecases.festival.CreateFestivalUseCase
import com.example.festivalappmobile.domain.usecases.festival.GetFestivalByIdUseCase
import com.example.festivalappmobile.domain.usecases.festival.GetFestivalsUseCase
import com.example.festivalappmobile.domain.usecases.festival.UpdateFestivalUseCase
import com.example.festivalappmobile.ui.screen.FestivalListScreen
import com.example.festivalappmobile.ui.screen.details.FestivalDetailScreen
import com.example.festivalappmobile.ui.screen.forms.FestivalFormScreen
import com.example.festivalappmobile.ui.viewmodels.FestivalFormViewModel
import com.example.festivalappmobile.ui.viewmodels.FestivalListViewModel
import com.example.festivalappmobile.ui.viewmodels.UsersManagementViewModel
import com.example.festivalappmobile.domain.usecases.editeur.GetEditeursUseCase
import com.example.festivalappmobile.domain.usecases.editeur.CreateEditeurUseCase
import com.example.festivalappmobile.domain.usecases.editeur.UpdateEditeurUseCase
import com.example.festivalappmobile.domain.usecases.editeur.GetEditeurByIdUseCase
import com.example.festivalappmobile.data.repository.EditeurRepositoryImpl
import com.example.festivalappmobile.ui.screen.EditeurListScreen
import com.example.festivalappmobile.ui.screen.details.EditeurDetailScreen
import com.example.festivalappmobile.ui.screen.forms.EditeurFormScreen
import com.example.festivalappmobile.ui.viewmodels.EditeurFormViewModel
import com.example.festivalappmobile.ui.viewmodels.EditeurListViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument

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
    
    // Log the user role for debugging
    if (user != null) {
        android.util.Log.d("MAINSCREEN", "User logged in - Role: '${user.role}' (ADMIN check: ${user.role == "ADMIN"})")
    } else {
        android.util.Log.d("MAINSCREEN", "No user logged in")
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("festival_prefs", Context.MODE_PRIVATE)
    }

    val festivalRepository = remember {
        val api = RetrofitClient.instance
        FestivalRepositoryImpl(api)
    }

    val editeurRepository = remember {
        val api = RetrofitClient.instance
        EditeurRepositoryImpl(api)
    }

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
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Business, contentDescription = "Éditeurs") },
                    label = { Text("Éditeurs") },
                    selected = currentRoute == "editeurs",
                    onClick = {
                        bottomNavController.navigate("editeurs") {
                            popUpTo(bottomNavController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
                
                // Show Users tab only for admins or super-organisateurs
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
            
            composable("festivals") {
                val viewModel: FestivalListViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return FestivalListViewModel(festivalRepository, sharedPrefs) as T
                        }
                    }
                )
                FestivalListScreen(
                    viewModel = viewModel,
                    onAddClick = { bottomNavController.navigate("festival_create") },
                    onFestivalClick = { id -> bottomNavController.navigate("festival_detail/$id") }
                )
            }

            composable(
                route = "festival_detail/{festivalId}",
                arguments = listOf(navArgument("festivalId") { type = NavType.IntType })
            ) { backStackEntry ->
                val festivalId = backStackEntry.arguments?.getInt("festivalId") ?: 0
                val viewModel: FestivalListViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return FestivalListViewModel(festivalRepository, sharedPrefs) as T
                        }
                    }
                )
                FestivalDetailScreen(
                    festivalId = festivalId,
                    viewModel = viewModel,
                    onNavigateBack = { bottomNavController.popBackStack() },
                    onEditClick = { id -> bottomNavController.navigate("festival_edit/$id") }
                )
            }

            composable("festival_create") {
                val viewModel: FestivalFormViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return FestivalFormViewModel(
                                GetFestivalByIdUseCase(festivalRepository),
                                CreateFestivalUseCase(festivalRepository),
                                UpdateFestivalUseCase(festivalRepository)
                            ) as T
                        }
                    }
                )
                FestivalFormScreen(
                    viewModel = viewModel,
                    onNavigateBack = { bottomNavController.popBackStack() },
                    onSuccess = { 
                        bottomNavController.popBackStack()
                    }
                )
            }

            composable(
                route = "festival_edit/{festivalId}",
                arguments = listOf(navArgument("festivalId") { type = NavType.IntType })
            ) { backStackEntry ->
                val festivalId = backStackEntry.arguments?.getInt("festivalId") ?: 0
                val viewModel: FestivalFormViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return FestivalFormViewModel(
                                GetFestivalByIdUseCase(festivalRepository),
                                CreateFestivalUseCase(festivalRepository),
                                UpdateFestivalUseCase(festivalRepository)
                            ) as T
                        }
                    }
                )
                
                LaunchedEffect(festivalId) {
                    viewModel.loadFestival(festivalId)
                }

                FestivalFormScreen(
                    viewModel = viewModel,
                    onNavigateBack = { bottomNavController.popBackStack() },
                    onSuccess = { 
                        bottomNavController.popBackStack()
                    }
                )
            }

            composable("editeurs") {
                val viewModel: EditeurListViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return EditeurListViewModel(editeurRepository) as T
                        }
                    }
                )
                EditeurListScreen(
                    viewModel = viewModel,
                    onAddClick = { bottomNavController.navigate("editeur_create") },
                    onEditeurClick = { id -> bottomNavController.navigate("editeur_detail/$id") }
                )
            }

            composable(
                route = "editeur_detail/{editeurId}",
                arguments = listOf(navArgument("editeurId") { type = NavType.IntType })
            ) { backStackEntry ->
                val editeurId = backStackEntry.arguments?.getInt("editeurId") ?: return@composable
                val viewModel: EditeurListViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return EditeurListViewModel(editeurRepository) as T
                        }
                    }
                )
                EditeurDetailScreen(
                    editeurId = editeurId,
                    viewModel = viewModel,
                    onNavigateBack = { bottomNavController.popBackStack() },
                    onEditClick = { id -> bottomNavController.navigate("editeur_edit/$id") }
                )
            }

            composable("editeur_create") {
                val viewModel: EditeurFormViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return EditeurFormViewModel(
                                GetEditeurByIdUseCase(editeurRepository),
                                CreateEditeurUseCase(editeurRepository),
                                UpdateEditeurUseCase(editeurRepository)
                            ) as T
                        }
                    }
                )
                EditeurFormScreen(
                    viewModel = viewModel,
                    onNavigateBack = { bottomNavController.popBackStack() },
                    onSuccess = { 
                        bottomNavController.popBackStack()
                    }
                )
            }

            composable(
                route = "editeur_edit/{editeurId}",
                arguments = listOf(navArgument("editeurId") { type = NavType.IntType })
            ) { backStackEntry ->
                val editeurId = backStackEntry.arguments?.getInt("editeurId") ?: return@composable
                val viewModelBase: EditeurFormViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return EditeurFormViewModel(
                                GetEditeurByIdUseCase(editeurRepository),
                                CreateEditeurUseCase(editeurRepository),
                                UpdateEditeurUseCase(editeurRepository)
                            ) as T
                        }
                    }
                )
                
                LaunchedEffect(editeurId) {
                    viewModelBase.loadEditeur(editeurId)
                }

                EditeurFormScreen(
                    viewModel = viewModelBase,
                    onNavigateBack = { bottomNavController.popBackStack() },
                    onSuccess = { 
                        bottomNavController.popBackStack()
                    }
                )
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
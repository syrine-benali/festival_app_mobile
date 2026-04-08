package com.example.festivalappmobile

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.festivalappmobile.data.local.AppDatabase
import com.example.festivalappmobile.data.local.TokenManager
import com.example.festivalappmobile.data.remote.RetrofitClient
import com.example.festivalappmobile.data.repository.EditeurRepositoryImpl
import com.example.festivalappmobile.data.repository.FestivalRepositoryImpl
import com.example.festivalappmobile.data.repository.GameRepositoryImpl
import com.example.festivalappmobile.data.repository.ReservationRepositoryImpl
import com.example.festivalappmobile.domain.models.User
import com.example.festivalappmobile.domain.usecases.editeur.CreateEditeurUseCase
import com.example.festivalappmobile.domain.usecases.editeur.GetEditeurByIdUseCase
import com.example.festivalappmobile.domain.usecases.editeur.UpdateEditeurUseCase
import com.example.festivalappmobile.domain.usecases.festival.CreateFestivalUseCase
import com.example.festivalappmobile.domain.usecases.festival.GetFestivalByIdUseCase
import com.example.festivalappmobile.domain.usecases.festival.UpdateFestivalUseCase
import com.example.festivalappmobile.domain.usecases.game.DeleteGameUseCase
import com.example.festivalappmobile.domain.usecases.game.GetAllGamesUseCase
import com.example.festivalappmobile.ui.screen.*
import com.example.festivalappmobile.ui.screen.details.EditeurDetailScreen
import com.example.festivalappmobile.ui.screen.details.FestivalDetailScreen
import com.example.festivalappmobile.ui.screen.forms.EditeurFormScreen
import com.example.festivalappmobile.ui.screen.forms.FestivalFormScreen
import com.example.festivalappmobile.ui.screen.forms.GameFormScreen
import com.example.festivalappmobile.ui.theme.FestivalAppMobileTheme
import com.example.festivalappmobile.ui.viewmodels.*
import com.example.festivalappmobile.utils.NetworkMonitor

/**
 * Dashboard accessible sans connexion.
 * Affiche uniquement la liste des festivals et le détail d'un festival.
 * Aucune barre de navigation latérale, aucun accès aux autres modules.
 */
@Composable
private fun PublicDashboardShell(
    context: Context,
    onBack: () -> Unit
) {
    val publicNavController = rememberNavController()
    val networkMonitor = remember { NetworkMonitor(context) }
    val db = remember { AppDatabase.getInstance(context) }
    val api = remember { RetrofitClient.instance }

    val festivalRepository = remember {
        FestivalRepositoryImpl(api, db.festivalDao(), networkMonitor)
    }
    val editeurRepository = remember {
        EditeurRepositoryImpl(api, db.editeurDao(), networkMonitor)
    }
    val gameRepository = remember {
        GameRepositoryImpl(api, db.gameDao(), networkMonitor)
    }
    val reservationRepository = remember {
        ReservationRepositoryImpl(api, db, context)
    }

    NavHost(
        navController = publicNavController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            val viewModel: DashboardViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return DashboardViewModel(festivalRepository, networkMonitor) as T
                    }
                }
            )
            // On surcharge la TopAppBar avec un bouton retour
            PublicDashboardScreen(
                viewModel = viewModel,
                onFestivalClick = { id ->
                    publicNavController.navigate("festival_detail/$id")
                },
                onBack = onBack
            )
        }

        composable(
            route = "festival_detail/{festivalId}",
            arguments = listOf(navArgument("festivalId") { type = NavType.IntType })
        ) { backStackEntry ->
            val festivalId =
                backStackEntry.arguments?.getInt("festivalId") ?: return@composable
            val viewModel: DashboardDetailViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return DashboardDetailViewModel(
                            festivalId = festivalId,
                            festivalRepository = festivalRepository,
                            gameRepository = gameRepository,
                            reservationRepository = reservationRepository,
                            networkMonitor = networkMonitor
                        ) as T
                    }
                }
            )
            FestivalDashboardDetailScreen(
                viewModel = viewModel,
                onBackClick = { publicNavController.popBackStack() }
            )
        }
    }
}

private data class AppTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val tokenManager = TokenManager(applicationContext)
        RetrofitClient.init(tokenManager)

        setContent {
            FestivalAppMobileTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val context = applicationContext
                    var currentUser by remember { mutableStateOf<User?>(null) }

                    NavHost(
                        navController = navController,
                        startDestination = "welcome"   // ← page d'accueil avec 3 boutons
                    ) {
                        // ── Page d'accueil ──────────────────────────────
                        composable("welcome") {
                            WelcomeScreen(
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                },
                                onNavigateToDashboard = {
                                    // Dashboard public : pas d'AppShell, pas de nav rail
                                    navController.navigate("public_dashboard")
                                }
                            )
                        }

                        // ── Dashboard public (sans connexion, sans nav rail) ──
                        composable("public_dashboard") {
                            PublicDashboardShell(
                                context = context,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // ── Connexion ───────────────────────────────────
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = { user ->
                                    currentUser = user
                                    navController.navigate("app") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                }
                            )
                        }

                        // ── Inscription ─────────────────────────────────
                        composable("register") {
                            RegisterScreen(
                                onRegistrationSuccess = {
                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // ── App principale (shell + nav rail) ───────────
                        composable("app") {
                            AppShell(
                                context = context,
                                currentUser = currentUser,
                                onLogout = {
                                    currentUser = null
                                    TokenManager(context).clearToken()
                                    navController.navigate("welcome") {
                                        popUpTo("app") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppShell(
    context: Context,
    currentUser: User?,
    onLogout: () -> Unit
) {
    val appNavController = rememberNavController()
    val screenWidth = LocalConfiguration.current.screenWidthDp
    var isSidebarExpanded by rememberSaveable { mutableStateOf(screenWidth >= 600) }
    val sidebarWidth by animateDpAsState(
        targetValue = if (isSidebarExpanded) 108.dp else 72.dp,
        animationSpec = tween(durationMillis = 220),
        label = "sidebar-width"
    )

    val tabs = remember {
        listOf(
            AppTab(route = "dashboard", label = "Tableau de bord", icon = Icons.Default.Dashboard),
            AppTab(route = "festivals", label = "Festivals", icon = Icons.Default.Event),
            AppTab(route = "editeurs", label = "Editeurs", icon = Icons.Default.Business),
            AppTab(route = "jeux", label = "Jeux", icon = Icons.Default.VideogameAsset),
            AppTab(route = "reservations", label = "Reservations", icon = Icons.Default.TableRestaurant),
            AppTab(route = "users-management", label = "Users", icon = Icons.Default.Groups),
            AppTab(route = "mon-compte", label = "Mon compte", icon = Icons.Default.AccountCircle)
        )
    }

    val navBackStackEntry by appNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        when {
            currentRoute == "reservation/{id}" -> isSidebarExpanded = false
            currentRoute in setOf(
                "dashboard", "festivals", "editeurs", "jeux",
                "reservations", "mon-compte", "users-management"
            ) -> {
                if (screenWidth >= 600) isSidebarExpanded = true
            }
        }
    }

    val sharedPrefs = remember {
        context.getSharedPreferences("festival_prefs", Context.MODE_PRIVATE)
    }

    // ── NetworkMonitor (singleton par AppShell) ─────────────────────────────
    val networkMonitor = remember { NetworkMonitor(context) }

    // ── Repositories offline-first ──────────────────────────────────────────
    val db = remember { AppDatabase.getInstance(context) }
    val api = remember { RetrofitClient.instance }

    val festivalRepository = remember {
        FestivalRepositoryImpl(api, db.festivalDao(), networkMonitor)
    }
    val editeurRepository = remember {
        EditeurRepositoryImpl(api, db.editeurDao(), networkMonitor)
    }
    val gameRepository = remember {
        GameRepositoryImpl(api, db.gameDao(), networkMonitor)
    }
    val reservationRepository = remember {
        ReservationRepositoryImpl(api, db, context)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Barre latérale de navigation ────────────────────────────────────
        Surface(
            modifier = Modifier
                .width(sidebarWidth)
                .fillMaxHeight(),
            tonalElevation = 2.dp,
            shadowElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
                    horizontalArrangement = if (isSidebarExpanded) Arrangement.SpaceBetween else Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSidebarExpanded) {
                        Text(text = "Festival", style = MaterialTheme.typography.titleMedium)
                    }
                    IconButton(onClick = { isSidebarExpanded = !isSidebarExpanded }) {
                        Icon(
                            imageVector = if (isSidebarExpanded)
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft
                            else
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = if (isSidebarExpanded)
                                "Réduire la barre latérale"
                            else
                                "Déployer la barre latérale"
                        )
                    }
                }

                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    tabs.forEach { tab ->
                        val selected = currentRoute == tab.route ||
                            (tab.route == "reservations" && currentRoute == "reservation/{id}")

                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                appNavController.navigate(tab.route) {
                                    popUpTo(appNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = if (isSidebarExpanded) {
                                { Text(tab.label) }
                            } else null,
                            alwaysShowLabel = isSidebarExpanded
                        )
                    }
                }
            }
        }

        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Contenu principal ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            NavHost(
                navController = appNavController,
                startDestination = "dashboard"
            ) {
                // ── Dashboard ───────────────────────────────────────────
                composable("dashboard") {
                    val viewModel: DashboardViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return DashboardViewModel(festivalRepository, networkMonitor) as T
                            }
                        }
                    )
                    DashboardScreen(
                        viewModel = viewModel,
                        onFestivalClick = { id ->
                            appNavController.navigate("festival_dashboard_detail/$id")
                        }
                    )
                }

                composable(
                    route = "festival_dashboard_detail/{festivalId}",
                    arguments = listOf(navArgument("festivalId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val festivalId =
                        backStackEntry.arguments?.getInt("festivalId") ?: return@composable
                    val viewModel: DashboardDetailViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return DashboardDetailViewModel(
                                    festivalId = festivalId,
                                    festivalRepository = festivalRepository,
                                    gameRepository = gameRepository,
                                    reservationRepository = reservationRepository,
                                    networkMonitor = networkMonitor
                                ) as T
                            }
                        }
                    )
                    FestivalDashboardDetailScreen(
                        viewModel = viewModel,
                        onBackClick = { appNavController.popBackStack() }
                    )
                }

                // ── Réservations ────────────────────────────────────────
                composable("reservations") {
                    val vm: ReservationListViewModel = viewModel(
                        factory = ReservationListViewModel.factory(context)
                    )
                    ReservationListScreen(
                        viewModel = vm,
                        onReservationClick = { id ->
                            appNavController.navigate("reservation/$id")
                        }
                    )
                }

                composable("reservation/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments
                        ?.getString("id")
                        ?.toIntOrNull()
                        ?: return@composable
                    val vm: ReservationDetailViewModel = viewModel(
                        factory = ReservationDetailViewModel.factory(context, id)
                    )
                    ReservationDetailScreen(
                        viewModel = vm,
                        onBack = { appNavController.popBackStack() }
                    )
                }

                // ── Festivals ───────────────────────────────────────────
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
                        onAddClick = { appNavController.navigate("festival_create") },
                        onFestivalClick = { id -> appNavController.navigate("festival_detail/$id") }
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
                        onNavigateBack = { appNavController.popBackStack() },
                        onEditClick = { id -> appNavController.navigate("festival_edit/$id") }
                    )
                }

                // ── Éditeurs ────────────────────────────────────────────
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
                        onAddClick = { appNavController.navigate("editeur_create") },
                        onEditeurClick = { id -> appNavController.navigate("editeur_detail/$id") }
                    )
                }

                composable(
                    route = "editeur_detail/{editeurId}",
                    arguments = listOf(navArgument("editeurId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val editeurId =
                        backStackEntry.arguments?.getInt("editeurId") ?: return@composable
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
                        onNavigateBack = { appNavController.popBackStack() },
                        onEditClick = { id -> appNavController.navigate("editeur_edit/$id") }
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
                        onNavigateBack = { appNavController.popBackStack() },
                        onSuccess = { appNavController.popBackStack() }
                    )
                }

                composable(
                    route = "editeur_edit/{editeurId}",
                    arguments = listOf(navArgument("editeurId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val editeurId =
                        backStackEntry.arguments?.getInt("editeurId") ?: return@composable
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
                    LaunchedEffect(editeurId) { viewModel.loadEditeur(editeurId) }
                    EditeurFormScreen(
                        viewModel = viewModel,
                        onNavigateBack = { appNavController.popBackStack() },
                        onSuccess = { appNavController.popBackStack() }
                    )
                }

                // ── Festivals (création / édition) ──────────────────────
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
                        onNavigateBack = { appNavController.popBackStack() },
                        onSuccess = { appNavController.popBackStack() }
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
                    LaunchedEffect(festivalId) { viewModel.loadFestival(festivalId) }
                    FestivalFormScreen(
                        viewModel = viewModel,
                        onNavigateBack = { appNavController.popBackStack() },
                        onSuccess = { appNavController.popBackStack() }
                    )
                }

                // ── Gestion utilisateurs ────────────────────────────────
                composable("users-management") {
                    val vm: UsersManagementViewModel = viewModel()
                    UsersAdminScreen(viewModel = vm)
                }

                // ── Jeux ────────────────────────────────────────────────
                composable("jeux") {
                    val viewModel: GameListViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return GameListViewModel(
                                    GetAllGamesUseCase(gameRepository),
                                    DeleteGameUseCase(gameRepository)
                                ) as T
                            }
                        }
                    )
                    GameListScreen(
                        viewModel = viewModel,
                        onAddClick = { appNavController.navigate("game_create") },
                        onGameClick = { game -> appNavController.navigate("game_edit/${game.id}") }
                    )
                }

                composable("game_create") {
                    val viewModel: GameFormViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return GameFormViewModel(gameRepository, editeurRepository) as T
                            }
                        }
                    )
                    GameFormScreen(
                        viewModel = viewModel,
                        onNavigateBack = { appNavController.popBackStack() },
                        onSuccess = { appNavController.popBackStack() }
                    )
                }

                composable(
                    route = "game_edit/{gameId}",
                    arguments = listOf(navArgument("gameId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val gameId = backStackEntry.arguments?.getInt("gameId") ?: 0
                    val viewModel: GameFormViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return GameFormViewModel(gameRepository, editeurRepository) as T
                            }
                        }
                    )
                    LaunchedEffect(gameId) { viewModel.loadGameById(gameId) }
                    GameFormScreen(
                        viewModel = viewModel,
                        onNavigateBack = { appNavController.popBackStack() },
                        onSuccess = { appNavController.popBackStack() }
                    )
                }

                // ── Mon compte ──────────────────────────────────────────
                composable("mon-compte") {
                    MonCompteScreen(
                        user = currentUser,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

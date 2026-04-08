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
import androidx.compose.ui.platform.LocalContext
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
import com.example.festivalappmobile.data.repository.DashboardCacheRepository
import com.example.festivalappmobile.data.repository.EditeurRepositoryImpl
import com.example.festivalappmobile.data.repository.FestivalRepositoryImpl
import com.example.festivalappmobile.data.repository.GameRepositoryImpl
import com.example.festivalappmobile.data.repository.JeuRepositoryImpl
import com.example.festivalappmobile.data.repository.ReservationDashboardRepositoryImpl
import com.example.festivalappmobile.domain.models.User
import com.example.festivalappmobile.domain.usecases.editeur.CreateEditeurUseCase
import com.example.festivalappmobile.domain.usecases.editeur.GetEditeurByIdUseCase
import com.example.festivalappmobile.domain.usecases.editeur.GetEditeursUseCase
import com.example.festivalappmobile.domain.usecases.editeur.UpdateEditeurUseCase
import com.example.festivalappmobile.domain.usecases.festival.CreateFestivalUseCase
import com.example.festivalappmobile.domain.usecases.festival.GetFestivalByIdUseCase
import com.example.festivalappmobile.domain.usecases.festival.GetFestivalsUseCase
import com.example.festivalappmobile.domain.usecases.festival.UpdateFestivalUseCase
import com.example.festivalappmobile.domain.usecases.game.DeleteGameUseCase
import com.example.festivalappmobile.domain.usecases.game.GetAllGamesUseCase
import com.example.festivalappmobile.domain.usecases.jeu.GetJeuxUseCase
import com.example.festivalappmobile.ui.screen.DashboardScreen
import com.example.festivalappmobile.ui.screen.EditeurListScreen
import com.example.festivalappmobile.ui.screen.FestivalListScreen
import com.example.festivalappmobile.ui.screen.GameListScreen
import com.example.festivalappmobile.ui.screen.LoginScreen
import com.example.festivalappmobile.ui.screen.MonCompteScreen
import com.example.festivalappmobile.ui.screen.RegisterScreen
import com.example.festivalappmobile.ui.screen.ReservationDetailScreen
import com.example.festivalappmobile.ui.screen.ReservationListScreen
import com.example.festivalappmobile.ui.screen.UsersAdminScreen
import com.example.festivalappmobile.ui.screen.details.EditeurDetailScreen
import com.example.festivalappmobile.ui.screen.details.FestivalDetailScreen
import com.example.festivalappmobile.ui.screen.forms.EditeurFormScreen
import com.example.festivalappmobile.ui.screen.forms.FestivalFormScreen
import com.example.festivalappmobile.ui.screen.forms.GameFormScreen
import com.example.festivalappmobile.ui.theme.FestivalAppMobileTheme
import com.example.festivalappmobile.ui.viewmodels.DashboardViewModel
import com.example.festivalappmobile.ui.viewmodels.EditeurFormViewModel
import com.example.festivalappmobile.ui.viewmodels.EditeurListViewModel
import com.example.festivalappmobile.ui.viewmodels.FestivalFormViewModel
import com.example.festivalappmobile.ui.viewmodels.FestivalListViewModel
import com.example.festivalappmobile.ui.viewmodels.GameFormViewModel
import com.example.festivalappmobile.ui.viewmodels.GameListViewModel
import com.example.festivalappmobile.ui.viewmodels.ReservationDetailViewModel
import com.example.festivalappmobile.ui.viewmodels.ReservationListViewModel
import com.example.festivalappmobile.ui.viewmodels.UsersManagementViewModel
import com.example.festivalappmobile.utils.NetworkChecker

// ─────────────────────────────────────────────────────────────────────────────
// Modèle interne pour les onglets de la sidebar
// ─────────────────────────────────────────────────────────────────────────────
private data class AppTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val adminOnly: Boolean = false
)

// ─────────────────────────────────────────────────────────────────────────────
// Activity
// ─────────────────────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val tokenManager = TokenManager(applicationContext)
        RetrofitClient.init(tokenManager)

        setContent {
            FestivalAppMobileTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Navigation racine  (login → register → dashboard public → app shell)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppNavigation() {
    val rootNavController = rememberNavController()
    val context = LocalContext.current

    var currentUser by remember { mutableStateOf<User?>(null) }

    // Dialog affiché après une inscription réussie
    var showValidationDialog by remember { mutableStateOf(false) }
    var registeredUserName by remember { mutableStateOf("") }

    // Dépendances partagées pour le support offline
    val appDatabase = remember { AppDatabase.getInstance(context) }
    val networkChecker = remember { NetworkChecker(context) }
    val dashboardCacheRepo = remember {
        DashboardCacheRepository(
            festivalDao = appDatabase.dashboardFestivalDao(),
            jeuDao      = appDatabase.dashboardJeuDao(),
            editeurDao  = appDatabase.dashboardEditeurDao()
        )
    }

    // ── Dialog validation inscription ──────────────────────────────────────
    if (showValidationDialog) {
        AlertDialog(
            onDismissRequest = {
                showValidationDialog = false
                rootNavController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
            },
            title = { Text("Inscription réussie") },
            text = {
                Text(
                    "Bienvenue $registeredUserName ! Votre compte a été créé avec succès.\n\n" +
                    "Veuillez attendre la validation de l'administrateur pour pouvoir accéder à l'application."
                )
            },
            confirmButton = {
                Button(onClick = {
                    showValidationDialog = false
                    rootNavController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }) { Text("OK") }
            }
        )
    }

    // ── Graphe de navigation racine ────────────────────────────────────────
    NavHost(navController = rootNavController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onLoginSuccess = { user ->
                    currentUser = user
                    rootNavController.navigate("app") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    rootNavController.navigate("register")
                },
                onNavigateToDashboard = {
                    rootNavController.navigate("public_dashboard")
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
                    rootNavController.navigate("public_dashboard")
                }
            )
        }

        // Dashboard public (accessible sans connexion)
        composable("public_dashboard") {
            val vm: DashboardViewModel = viewModel(
                factory = dashboardViewModelFactory(
                    context         = context,
                    cacheRepo       = dashboardCacheRepo,
                    networkChecker  = networkChecker
                )
            )
            DashboardScreen(viewModel = vm)
        }

        // Application principale (après connexion)
        composable("app") {
            AppShell(
                context             = context,
                currentUser         = currentUser,
                dashboardCacheRepo  = dashboardCacheRepo,
                networkChecker      = networkChecker,
                onLogout            = {
                    currentUser = null
                    TokenManager(context).clearToken()
                    rootNavController.navigate("login") {
                        popUpTo("app") { inclusive = true }
                    }
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shell principal : sidebar + NavHost interne
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AppShell(
    context: Context,
    currentUser: User?,
    dashboardCacheRepo: DashboardCacheRepository,
    networkChecker: NetworkChecker,
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

    // Rôles avec droits d'administration
    val isAdmin = currentUser?.role == "ADMIN" || currentUser?.role == "SUPER_ORGANISATEUR"

    val allTabs = remember(isAdmin) {
        buildList {
            add(AppTab("dashboard",         "Accueil",       Icons.Default.Dashboard))
            add(AppTab("festivals",          "Festivals",     Icons.Default.Event))
            add(AppTab("editeurs",           "Editeurs",      Icons.Default.Business))
            add(AppTab("jeux",               "Jeux",          Icons.Default.VideogameAsset))
            add(AppTab("reservations",       "Réservations",  Icons.Default.TableRestaurant))
            if (isAdmin) {
                add(AppTab("users-management", "Users", Icons.Default.Groups, adminOnly = true))
            }
            add(AppTab("mon-compte",         "Mon compte",    Icons.Default.AccountCircle))
        }
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

    val festivalRepository = remember { FestivalRepositoryImpl(RetrofitClient.instance) }
    val editeurRepository  = remember { EditeurRepositoryImpl(RetrofitClient.instance) }
    val gameRepository     = remember { GameRepositoryImpl(RetrofitClient.instance) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Sidebar ────────────────────────────────────────────────────────
        Surface(
            modifier      = Modifier.width(sidebarWidth).fillMaxHeight(),
            tonalElevation   = 2.dp,
            shadowElevation  = 3.dp,
            color         = MaterialTheme.colorScheme.surface
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
                        Text("Festival", style = MaterialTheme.typography.titleMedium)
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
                    allTabs.forEach { tab ->
                        val selected = currentRoute == tab.route ||
                            (tab.route == "reservations" && currentRoute == "reservation/{id}")

                        NavigationRailItem(
                            selected  = selected,
                            onClick   = {
                                appNavController.navigate(tab.route) {
                                    popUpTo(appNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon      = { Icon(tab.icon, contentDescription = tab.label) },
                            label     = if (isSidebarExpanded) ({ Text(tab.label) }) else null,
                            alwaysShowLabel = isSidebarExpanded
                        )
                    }
                }
            }
        }

        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Contenu principal ───────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            NavHost(
                navController    = appNavController,
                startDestination = "dashboard"
            ) {

                // ── Dashboard (onglet d'accueil) ───────────────────────────
                composable("dashboard") {
                    val vm: DashboardViewModel = viewModel(
                        factory = dashboardViewModelFactory(
                            context        = context,
                            cacheRepo      = dashboardCacheRepo,
                            networkChecker = networkChecker
                        )
                    )
                    DashboardScreen(viewModel = vm)
                }

                // ── Réservations ───────────────────────────────────────────
                composable("reservations") {
                    val vm: ReservationListViewModel = viewModel(
                        factory = ReservationListViewModel.factory(context)
                    )
                    ReservationListScreen(
                        viewModel = vm,
                        onReservationClick = { id -> appNavController.navigate("reservation/$id") }
                    )
                }

                composable("reservation/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
                        ?: return@composable
                    val vm: ReservationDetailViewModel = viewModel(
                        factory = ReservationDetailViewModel.factory(context, id)
                    )
                    ReservationDetailScreen(
                        viewModel = vm,
                        onBack    = { appNavController.popBackStack() }
                    )
                }

                // ── Festivals ──────────────────────────────────────────────
                composable("festivals") {
                    val vm: FestivalListViewModel = viewModel(
                        factory = festivalListVmFactory(festivalRepository, sharedPrefs)
                    )
                    FestivalListScreen(
                        viewModel      = vm,
                        onAddClick     = { appNavController.navigate("festival_create") },
                        onFestivalClick = { id -> appNavController.navigate("festival_detail/$id") }
                    )
                }

                composable(
                    route     = "festival_detail/{festivalId}",
                    arguments = listOf(navArgument("festivalId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val festivalId = backStackEntry.arguments?.getInt("festivalId") ?: 0
                    val vm: FestivalListViewModel = viewModel(
                        factory = festivalListVmFactory(festivalRepository, sharedPrefs)
                    )
                    FestivalDetailScreen(
                        festivalId      = festivalId,
                        viewModel       = vm,
                        onNavigateBack  = { appNavController.popBackStack() },
                        onEditClick     = { id -> appNavController.navigate("festival_edit/$id") }
                    )
                }

                composable("festival_create") {
                    val vm: FestivalFormViewModel = viewModel(
                        factory = festivalFormVmFactory(festivalRepository)
                    )
                    FestivalFormScreen(
                        viewModel      = vm,
                        onNavigateBack = { appNavController.popBackStack() },
                        onSuccess      = { appNavController.popBackStack() }
                    )
                }

                composable(
                    route     = "festival_edit/{festivalId}",
                    arguments = listOf(navArgument("festivalId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val festivalId = backStackEntry.arguments?.getInt("festivalId") ?: 0
                    val vm: FestivalFormViewModel = viewModel(
                        factory = festivalFormVmFactory(festivalRepository)
                    )
                    LaunchedEffect(festivalId) { vm.loadFestival(festivalId) }
                    FestivalFormScreen(
                        viewModel      = vm,
                        onNavigateBack = { appNavController.popBackStack() },
                        onSuccess      = { appNavController.popBackStack() }
                    )
                }

                // ── Editeurs ───────────────────────────────────────────────
                composable("editeurs") {
                    val vm: EditeurListViewModel = viewModel(
                        factory = editeurListVmFactory(editeurRepository)
                    )
                    EditeurListScreen(
                        viewModel     = vm,
                        onAddClick    = { appNavController.navigate("editeur_create") },
                        onEditeurClick = { id -> appNavController.navigate("editeur_detail/$id") }
                    )
                }

                composable(
                    route     = "editeur_detail/{editeurId}",
                    arguments = listOf(navArgument("editeurId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val editeurId = backStackEntry.arguments?.getInt("editeurId") ?: return@composable
                    val vm: EditeurListViewModel = viewModel(
                        factory = editeurListVmFactory(editeurRepository)
                    )
                    EditeurDetailScreen(
                        editeurId      = editeurId,
                        viewModel      = vm,
                        onNavigateBack = { appNavController.popBackStack() },
                        onEditClick    = { id -> appNavController.navigate("editeur_edit/$id") }
                    )
                }

                composable("editeur_create") {
                    val vm: EditeurFormViewModel = viewModel(
                        factory = editeurFormVmFactory(editeurRepository)
                    )
                    EditeurFormScreen(
                        viewModel      = vm,
                        onNavigateBack = { appNavController.popBackStack() },
                        onSuccess      = { appNavController.popBackStack() }
                    )
                }

                composable(
                    route     = "editeur_edit/{editeurId}",
                    arguments = listOf(navArgument("editeurId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val editeurId = backStackEntry.arguments?.getInt("editeurId") ?: return@composable
                    val vm: EditeurFormViewModel = viewModel(
                        factory = editeurFormVmFactory(editeurRepository)
                    )
                    LaunchedEffect(editeurId) { vm.loadEditeur(editeurId) }
                    EditeurFormScreen(
                        viewModel      = vm,
                        onNavigateBack = { appNavController.popBackStack() },
                        onSuccess      = { appNavController.popBackStack() }
                    )
                }

                // ── Jeux ───────────────────────────────────────────────────
                composable("jeux") {
                    val vm: GameListViewModel = viewModel(
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
                        viewModel   = vm,
                        onAddClick  = { appNavController.navigate("game_create") },
                        onGameClick = { game -> appNavController.navigate("game_edit/${game.id}") }
                    )
                }

                composable("game_create") {
                    val vm: GameFormViewModel = viewModel(
                        factory = gameFormVmFactory(gameRepository, editeurRepository)
                    )
                    GameFormScreen(
                        viewModel      = vm,
                        onNavigateBack = { appNavController.popBackStack() },
                        onSuccess      = { appNavController.popBackStack() }
                    )
                }

                composable(
                    route     = "game_edit/{gameId}",
                    arguments = listOf(navArgument("gameId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val gameId = backStackEntry.arguments?.getInt("gameId") ?: 0
                    val vm: GameFormViewModel = viewModel(
                        factory = gameFormVmFactory(gameRepository, editeurRepository)
                    )
                    LaunchedEffect(gameId) { vm.loadGameById(gameId) }
                    GameFormScreen(
                        viewModel      = vm,
                        onNavigateBack = { appNavController.popBackStack() },
                        onSuccess      = { appNavController.popBackStack() }
                    )
                }

                // ── Gestion utilisateurs (admin) ───────────────────────────
                composable("users-management") {
                    val vm: UsersManagementViewModel = viewModel()
                    UsersAdminScreen(viewModel = vm)
                }

                // ── Mon compte ─────────────────────────────────────────────
                composable("mon-compte") {
                    MonCompteScreen(
                        user     = currentUser,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Factories extraites pour alléger AppShell
// ─────────────────────────────────────────────────────────────────────────────
private fun dashboardViewModelFactory(
    context: Context,
    cacheRepo: DashboardCacheRepository,
    networkChecker: NetworkChecker
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val api = RetrofitClient.instance
        @Suppress("UNCHECKED_CAST")
        return DashboardViewModel(
            getFestivalsUseCase = GetFestivalsUseCase(FestivalRepositoryImpl(api)),
            getJeuxUseCase      = GetJeuxUseCase(JeuRepositoryImpl(api)),
            getEditeursUseCase  = GetEditeursUseCase(EditeurRepositoryImpl(api)),
            reservationRepo     = ReservationDashboardRepositoryImpl(api),
            cacheRepo           = cacheRepo,
            networkChecker      = networkChecker
        ) as T
    }
}

private fun festivalListVmFactory(
    repo: FestivalRepositoryImpl,
    prefs: android.content.SharedPreferences
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FestivalListViewModel(repo, prefs) as T
    }
}

private fun festivalFormVmFactory(repo: FestivalRepositoryImpl) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FestivalFormViewModel(
                GetFestivalByIdUseCase(repo),
                CreateFestivalUseCase(repo),
                UpdateFestivalUseCase(repo)
            ) as T
        }
    }

private fun editeurListVmFactory(repo: EditeurRepositoryImpl) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return EditeurListViewModel(repo) as T
        }
    }

private fun editeurFormVmFactory(repo: EditeurRepositoryImpl) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return EditeurFormViewModel(
                GetEditeurByIdUseCase(repo),
                CreateEditeurUseCase(repo),
                UpdateEditeurUseCase(repo)
            ) as T
        }
    }

private fun gameFormVmFactory(
    gameRepo: GameRepositoryImpl,
    editeurRepo: EditeurRepositoryImpl
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return GameFormViewModel(gameRepo, editeurRepo) as T
    }
}
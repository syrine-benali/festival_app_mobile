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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.festivalappmobile.data.local.TokenManager
import com.example.festivalappmobile.data.remote.RetrofitClient
import com.example.festivalappmobile.data.repository.EditeurRepositoryImpl
import com.example.festivalappmobile.data.repository.FestivalRepositoryImpl
import com.example.festivalappmobile.domain.usecases.editeur.CreateEditeurUseCase
import com.example.festivalappmobile.domain.usecases.editeur.GetEditeurByIdUseCase
import com.example.festivalappmobile.domain.usecases.editeur.UpdateEditeurUseCase
import com.example.festivalappmobile.domain.usecases.festival.CreateFestivalUseCase
import com.example.festivalappmobile.domain.usecases.festival.GetFestivalByIdUseCase
import com.example.festivalappmobile.domain.usecases.festival.UpdateFestivalUseCase
import com.example.festivalappmobile.ui.screen.EditeurListScreen
import com.example.festivalappmobile.ui.screen.FestivalListScreen
import com.example.festivalappmobile.ui.screen.LoginScreen
import com.example.festivalappmobile.ui.screen.RegisterScreen
import com.example.festivalappmobile.ui.screen.ReservationDetailScreen
import com.example.festivalappmobile.ui.screen.ReservationListScreen
import com.example.festivalappmobile.ui.screen.UsersAdminScreen
import com.example.festivalappmobile.ui.screen.details.EditeurDetailScreen
import com.example.festivalappmobile.ui.screen.details.FestivalDetailScreen
import com.example.festivalappmobile.ui.screen.forms.EditeurFormScreen
import com.example.festivalappmobile.ui.screen.forms.FestivalFormScreen
import com.example.festivalappmobile.ui.theme.FestivalAppMobileTheme
import com.example.festivalappmobile.ui.viewmodels.EditeurFormViewModel
import com.example.festivalappmobile.ui.viewmodels.EditeurListViewModel
import com.example.festivalappmobile.ui.viewmodels.FestivalFormViewModel
import com.example.festivalappmobile.ui.viewmodels.FestivalListViewModel
import com.example.festivalappmobile.ui.viewmodels.ReservationDetailViewModel
import com.example.festivalappmobile.ui.viewmodels.ReservationListViewModel
import com.example.festivalappmobile.ui.viewmodels.UsersManagementViewModel

private data class AppTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialiser le TokenManager et Retrofit une seule fois
        val tokenManager = TokenManager(applicationContext)
        RetrofitClient.init(tokenManager)

        setContent {
            FestivalAppMobileTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val context = applicationContext

                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("app") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                }
                            )
                        }

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

                        composable("app") {
                            AppShell(context = context)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppShell(context: Context) {
    val bottomNavController = rememberNavController()
    val screenWidth = LocalConfiguration.current.screenWidthDp
    var isSidebarExpanded by rememberSaveable { mutableStateOf(screenWidth >= 600) }
    val sidebarWidth by animateDpAsState(
        targetValue = if (isSidebarExpanded) 108.dp else 72.dp,
        animationSpec = tween(durationMillis = 220),
        label = "sidebar-width"
    )

    val tabs = remember {
        listOf(
            AppTab(route = "festivals", label = "Festivals", icon = Icons.Default.Event),
            AppTab(route = "editeurs", label = "Editeurs", icon = Icons.Default.Business),
            AppTab(route = "reservations", label = "Reservations", icon = Icons.Default.TableRestaurant),
            AppTab(route = "users-management", label = "Users", icon = Icons.Default.Groups)
        )
    }
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        when {
            currentRoute == "reservation/{id}" -> isSidebarExpanded = false
            currentRoute in setOf("festivals", "editeurs", "reservations", "users-management") -> {
                if (screenWidth >= 600) {
                    isSidebarExpanded = true
                }
            }
        }
    }

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

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                        Text(
                            text = "Festival",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    IconButton(onClick = { isSidebarExpanded = !isSidebarExpanded }) {
                        Icon(
                            imageVector = if (isSidebarExpanded) {
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft
                            } else {
                                Icons.AutoMirrored.Filled.KeyboardArrowRight
                            },
                            contentDescription = if (isSidebarExpanded) {
                                "Réduire la barre latérale"
                            } else {
                                "Déployer la barre latérale"
                            }
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
                                bottomNavController.navigate(tab.route) {
                                    popUpTo(bottomNavController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = if (isSidebarExpanded) {
                                { Text(tab.label) }
                            } else {
                                null
                            },
                            alwaysShowLabel = isSidebarExpanded
                        )
                    }
                }
            }
        }

        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            NavHost(
                navController = bottomNavController,
                startDestination = "festivals"
            ) {
                composable("reservations") {
                    val vm: ReservationListViewModel = viewModel(
                        factory = ReservationListViewModel.factory(context)
                    )
                    ReservationListScreen(
                        viewModel = vm,
                        onReservationClick = { id ->
                            bottomNavController.navigate("reservation/$id")
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
                        onBack = { bottomNavController.popBackStack() }
                    )
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

                composable("users-management") {
                    val vm: UsersManagementViewModel = viewModel()
                    UsersAdminScreen(viewModel = vm)
                }
            }
        }
    }
}

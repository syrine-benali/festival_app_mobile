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
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.festivalappmobile.data.local.TokenManager
import com.example.festivalappmobile.data.remote.RetrofitClient
import com.example.festivalappmobile.domain.models.User
import com.example.festivalappmobile.ui.screen.EditeurListScreen
import com.example.festivalappmobile.ui.screen.FestivalListScreen
import com.example.festivalappmobile.ui.screen.LoginScreen
import com.example.festivalappmobile.ui.screen.MonCompteScreen
import com.example.festivalappmobile.ui.screen.RegisterScreen
import com.example.festivalappmobile.ui.screen.ReservationDetailScreen
import com.example.festivalappmobile.ui.screen.ReservationListScreen
import com.example.festivalappmobile.ui.screen.UsersAdminScreen
import com.example.festivalappmobile.ui.theme.FestivalAppMobileTheme
import com.example.festivalappmobile.ui.viewmodels.EditeurListViewModel
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
                    var currentUser by remember { mutableStateOf<User?>(null) }

                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {

                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = { user ->
                                    currentUser = user
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
                            AppShell(
                                context = context,
                                currentUser = currentUser,
                                onLogout = {
                                    currentUser = null
                                    TokenManager(context).clearToken()
                                    navController.navigate("login") {
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
            AppTab(route = "festivals", label = "Festivals", icon = Icons.Default.Event),
            AppTab(route = "editeurs", label = "Editeurs", icon = Icons.Default.Storefront),
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
            currentRoute in setOf("festivals", "editeurs", "reservations", "mon-compte", "users-management") -> {
                if (screenWidth >= 600) {
                    isSidebarExpanded = true
                }
            }
        }
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
                navController = appNavController,
                startDestination = "festivals"
            ) {
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

                composable("editeurs") {
                    val vm: EditeurListViewModel = viewModel(
                        factory = EditeurListViewModel.factory()
                    )
                    EditeurListScreen(viewModel = vm)
                }

                composable("festivals") {
                    val vm: FestivalListViewModel = viewModel(
                        factory = FestivalListViewModel.factory()
                    )
                    FestivalListScreen(viewModel = vm)
                }

                composable("users-management") {
                    val vm: UsersManagementViewModel = viewModel()
                    UsersAdminScreen(viewModel = vm)
                }

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

package com.example.festivalappmobile

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.festivalappmobile.data.local.TokenManager
import com.example.festivalappmobile.data.remote.RetrofitClient
import com.example.festivalappmobile.ui.screen.EditeurListScreen
import com.example.festivalappmobile.ui.screen.FestivalListScreen
import com.example.festivalappmobile.ui.screen.LoginScreen
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
import kotlinx.coroutines.launch

private data class AppTab(
    val route: String,
    val label: String,
    val marker: String
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
    val appNavController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()
    val tabs = remember {
        listOf(
            AppTab(route = "festivals", label = "Festivals", marker = "F"),
            AppTab(route = "editeurs", label = "Editeurs", marker = "E"),
            AppTab(route = "reservations", label = "Reservations", marker = "R"),
            AppTab(route = "users-management", label = "User Management", marker = "M")
        )
    }
    val navBackStackEntry by appNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(240.dp)) {
                Text(
                    text = "Festival App",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                )
                tabs.forEach { tab ->
                    val selected = currentRoute == tab.route ||
                        (tab.route == "reservations" && currentRoute == "reservation/{id}")
                    NavigationDrawerItem(
                        selected = selected,
                        onClick = {
                            appNavController.navigate(tab.route) {
                                popUpTo(appNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            drawerScope.launch { drawerState.close() }
                        },
                        icon = { Text(tab.marker) },
                        label = { Text(tab.label) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
            }

            ExtendedFloatingActionButton(
                onClick = {
                    drawerScope.launch {
                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                    }
                },
                icon = { Icon(Icons.Default.Menu, contentDescription = null) },
                text = { Text("Menu") },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}

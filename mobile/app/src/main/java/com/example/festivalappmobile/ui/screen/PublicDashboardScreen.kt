package com.example.festivalappmobile.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.ui.viewmodels.DashboardUiState
import com.example.festivalappmobile.ui.viewmodels.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Version publique du tableau de bord — accessible sans connexion.
 * Affiche la liste des festivals avec un bouton retour vers la page d'accueil.
 * Pas de barre de navigation latérale, pas d'accès aux autres modules.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicDashboardScreen(
    viewModel: DashboardViewModel,
    onFestivalClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    // Rafraîchissement au retour en foreground
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.loadFestivals()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tableau de bord") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // ── Bandeau hors ligne ──────────────────────────────────────
            AnimatedVisibility(
                visible = !isOnline,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                OfflineBanner()
            }

            // ── Contenu ─────────────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    is DashboardUiState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is DashboardUiState.Error -> {
                        val message = (state as DashboardUiState.Error).message
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Erreur : $message",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Button(onClick = { viewModel.loadFestivals() }) {
                                Text("Réessayer")
                            }
                        }
                    }

                    is DashboardUiState.Success -> {
                        val festivals = (state as DashboardUiState.Success).festivals
                        if (festivals.isEmpty()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Aucun festival disponible",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (!isOnline) {
                                    Text(
                                        text = "Connectez-vous à internet pour charger les données.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(festivals) { festival ->
                                    PublicFestivalCard(
                                        festival = festival,
                                        onClick = { onFestivalClick(festival.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicFestivalCard(
    festival: Festival,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = festival.nom,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Lieu",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = festival.lieu, style = MaterialTheme.typography.bodyMedium)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Dates",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)
                val dateDebut = try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.FRENCH).parse(festival.dateDebut)
                        ?.let { dateFormat.format(it) } ?: festival.dateDebut
                } catch (e: Exception) { festival.dateDebut }
                val dateFin = try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.FRENCH).parse(festival.dateFin)
                        ?.let { dateFormat.format(it) } ?: festival.dateFin
                } catch (e: Exception) { festival.dateFin }
                Text(
                    text = "$dateDebut – $dateFin",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tables",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = festival.nbTotalTable.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Chaises",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = festival.nbTotalChaise.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Cliquez pour voir les détails →",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

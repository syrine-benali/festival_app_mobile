package com.example.festivalappmobile.ui.screen

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
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.models.Game
import com.example.festivalappmobile.ui.viewmodels.DashboardDetailUiState
import com.example.festivalappmobile.ui.viewmodels.DashboardDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalDashboardDetailScreen(
    viewModel: DashboardDetailViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails du Festival") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is DashboardDetailUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is DashboardDetailUiState.Error -> {
                    val message = (state as DashboardDetailUiState.Error).message
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Erreur: $message",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(onClick = { viewModel.reload() }) {
                            Text("Réessayer")
                        }
                    }
                }
                is DashboardDetailUiState.Success -> {
                    val successState = state as DashboardDetailUiState.Success
                    val festival = successState.festival
                    val games = successState.games
                    val editeurs = successState.editeurs

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            FestivalHeaderCard(festival = festival)
                        }

                        item {
                            sectionTitle("Jeux du festival (${games.size})")
                        }

                        if (games.isNotEmpty()) {
                            items(games) { game ->
                                GameItemCard(game = game)
                            }
                        } else {
                            item {
                                Text(
                                    text = "Aucun jeu pour ce festival",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        item {
                            sectionTitle("Éditeurs (${editeurs.size})")
                        }

                        if (editeurs.isNotEmpty()) {
                            items(editeurs) { editeur ->
                                EditeurItemCard(editeur = editeur)
                            }
                        } else {
                            item {
                                Text(
                                    text = "Aucun éditeur enregistré",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FestivalHeaderCard(festival: Festival) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.padding(bottom = 12.dp)
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
                Text(
                    text = festival.lieu,
                    style = MaterialTheme.typography.bodyMedium
                )
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
                    SimpleDateFormat("yyyy-MM-dd", Locale.FRENCH).parse(festival.dateDebut)?.let {
                        dateFormat.format(it)
                    } ?: festival.dateDebut
                } catch (e: Exception) {
                    festival.dateDebut
                }
                val dateFin = try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.FRENCH).parse(festival.dateFin)?.let {
                        dateFormat.format(it)
                    } ?: festival.dateFin
                } catch (e: Exception) {
                    festival.dateFin
                }
                Text(
                    text = "$dateDebut - $dateFin",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tables", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(festival.nbTotalTable.toString(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Chaises", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(festival.nbTotalChaise.toString(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GameItemCard(game: Game) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = game.libelle,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (!game.auteur.isNullOrEmpty()) {
                Text(
                    text = "Auteur: ${game.auteur}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (!game.editeurName.isNullOrEmpty()) {
                Text(
                    text = "Éditeur: ${game.editeurName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (game.nbMinJoueur != null && game.nbMaxJoueur != null) {
                Text(
                    text = "Joueurs: ${game.nbMinJoueur} - ${game.nbMaxJoueur}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun EditeurItemCard(editeur: Editeur) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = editeur.libelle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (editeur.exposant) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Exposant") },
                            enabled = false
                        )
                    }
                    if (editeur.distributeur) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Distributeur") },
                            enabled = false
                        )
                    }
                }
            }
            if (!editeur.email.isNullOrEmpty()) {
                Text(
                    text = "Email: ${editeur.email}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (!editeur.phone.isNullOrEmpty()) {
                Text(
                    text = "Téléphone: ${editeur.phone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun sectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    )
}

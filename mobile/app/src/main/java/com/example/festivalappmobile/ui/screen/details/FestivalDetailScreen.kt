package com.example.festivalappmobile.ui.screen.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.ui.viewmodels.FestivalListViewModel
import com.example.festivalappmobile.ui.viewmodels.FestivalUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalDetailScreen(
    festivalId: Int,
    viewModel: FestivalListViewModel,
    onNavigateBack: () -> Unit,
    onEditClick: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    val displayFormatter = remember { SimpleDateFormat("d MMM yyyy", Locale.FRANCE) }
    val apiFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE) }

    val festival = when (state) {
        is FestivalUiState.Success -> (state as FestivalUiState.Success).festivals.find { it.id == festivalId }
        else -> null
    }

    fun formatReadableDate(dateStr: String): String {
        return try {
            val date = apiFormatter.parse(dateStr)
            displayFormatter.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    if (showDeleteDialog && festival != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer le festival") },
            text = { Text("Êtes-vous sûr de vouloir supprimer le festival \"${festival.nom}\" ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFestival(festivalId)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(festival?.nom ?: "Détails du Festival") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (festival != null) {
                        IconButton(onClick = { onEditClick(festival.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete, 
                                contentDescription = "Supprimer",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (state) {
                is FestivalUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
                is FestivalUiState.Error -> {
                    Text(
                        text = "Erreur: ${(state as FestivalUiState.Error).message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is FestivalUiState.Success -> {
                    if (festival == null) {
                        Text(text = "Festival non trouvé.", modifier = Modifier.padding(16.dp))
                    } else {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DetailSection("Information Générale") {
                                DetailRow("Nom", festival.nom)
                                DetailRow("Lieu", festival.lieu)
                                DetailRow("Date", "Du ${formatReadableDate(festival.dateDebut)} au ${formatReadableDate(festival.dateFin)}")
                            }

                            DetailSection("Logistique") {
                                DetailRow("Total Tables", festival.nbTotalTable.toString())
                                DetailRow("Total Chaises", festival.nbTotalChaise.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

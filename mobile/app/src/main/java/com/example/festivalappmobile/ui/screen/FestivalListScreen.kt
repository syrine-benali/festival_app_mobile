package com.example.festivalappmobile.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.ui.viewmodels.FestivalListViewModel
import com.example.festivalappmobile.ui.viewmodels.FestivalUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FestivalListScreen(
    viewModel: FestivalListViewModel,
    onAddClick: () -> Unit,
    onFestivalClick: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Festivals") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un festival")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is FestivalUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is FestivalUiState.Error -> {
                    val message = (state as FestivalUiState.Error).message
                    Text(text = "Error: \$message", color = MaterialTheme.colorScheme.error)
                }
                is FestivalUiState.Success -> {
                    val festivals = (state as FestivalUiState.Success).festivals
                    if (festivals.isEmpty()) {
                        Text(text = "Aucun festival trouvé.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(festivals) { festival ->
                                FestivalCard(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalCard(festival: Festival, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = festival.nom,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Text(
                        text = "${festival.nbTotalTable} tables",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            val displayFormatter = remember { SimpleDateFormat("d MMM yyyy", Locale.FRANCE) }
            val apiFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE) }
            
            fun formatReadableDate(dateStr: String): String {
                return try {
                    val date = apiFormatter.parse(dateStr)
                    displayFormatter.format(date!!)
                } catch (e: Exception) {
                    dateStr
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Lieu : ${festival.lieu}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Du ${formatReadableDate(festival.dateDebut)} au ${formatReadableDate(festival.dateFin)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

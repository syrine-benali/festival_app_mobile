package com.example.festivalappmobile.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.ui.viewmodels.FestivalListViewModel
import com.example.festivalappmobile.ui.viewmodels.FestivalUiState

@Composable
fun FestivalListScreen(viewModel: FestivalListViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Festivals") })
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
                    Text(text = "Erreur: $message", color = MaterialTheme.colorScheme.error)
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
                                FestivalCard(festival)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FestivalCard(festival: Festival) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = festival.nom, style = MaterialTheme.typography.titleMedium)
            Text(text = "Lieu : ${festival.lieu}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Du ${festival.dateDebut} au ${festival.dateFin}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

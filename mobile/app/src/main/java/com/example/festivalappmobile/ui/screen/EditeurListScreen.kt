package com.example.festivalappmobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.ui.viewmodels.EditeurListViewModel
import com.example.festivalappmobile.ui.viewmodels.EditeurUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditeurListScreen(viewModel: EditeurListViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Editeurs") })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is EditeurUiState.Loading -> CircularProgressIndicator()
                is EditeurUiState.Error -> {
                    val message = (state as EditeurUiState.Error).message
                    Text(text = "Erreur: $message", color = MaterialTheme.colorScheme.error)
                }
                is EditeurUiState.Success -> {
                    val editeurs = (state as EditeurUiState.Success).editeurs
                    if (editeurs.isEmpty()) {
                        Text(text = "Aucun editeur trouve")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(editeurs) { editeur ->
                                EditeurCard(editeur)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditeurCard(editeur: Editeur) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = editeur.libelle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "Email: ${editeur.email ?: "-"}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Telephone: ${editeur.phone ?: "-"}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Reservation: ${if (editeur.hasReservation) "Oui" else "Non"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

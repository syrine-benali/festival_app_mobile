package com.example.festivalappmobile.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.festivalappmobile.ui.viewmodels.ReservationViewModel
import com.example.festivalappmobile.domain.models.Reservation

@Composable
fun ReservationListScreen(viewModel: ReservationViewModel) {
    // Collecte de l'état (UDF)
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Naviguer vers formulaire de création */ }) {
                Text("+")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            if (state.isLoading && state.reservations.isEmpty()) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            state.errorMessage?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(state.reservations) { reservation ->
                    ReservationCard(reservation)
                }
            }
        }
    }
}

@Composable
fun ReservationCard(reservation: Reservation) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = reservation.editeurNom ?: "Éditeur inconnu", style = MaterialTheme.typography.titleMedium)
            Text(text = "Statut : ${reservation.workflowStatus}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Type : ${reservation.typeReservant}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
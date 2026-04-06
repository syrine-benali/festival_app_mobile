package com.example.festivalappmobile.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.ui.viewmodels.EditeurListState
import com.example.festivalappmobile.ui.viewmodels.EditeurListViewModel
import com.example.festivalappmobile.ui.viewmodels.EditeurUiState
import com.example.festivalappmobile.ui.viewmodels.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditeurListScreen(
    viewModel: EditeurListViewModel
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Éditeurs") },
                actions = {
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (state.viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.Default.ViewList,
                            contentDescription = "Changer de vue"
                        )
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
            when (val uiState = state.uiState) {
                is EditeurUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is EditeurUiState.Error -> {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is EditeurUiState.Success -> {
                    if (uiState.editeurs.isEmpty()) {
                        Text(
                            text = "Aucun éditeur trouvé.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        if (state.viewMode == ViewMode.LIST) {
                            EditeurListView(uiState.editeurs)
                        } else {
                            EditeurGridView(uiState.editeurs)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditeurListView(editeurs: List<Editeur>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(editeurs) { editeur ->
            EditeurListItem(editeur)
        }
    }
}

@Composable
fun EditeurGridView(editeurs: List<Editeur>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(editeurs) { editeur ->
            EditeurGridItem(editeur)
        }
    }
}

@Composable
fun EditeurListItem(editeur: Editeur) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Business)
            AsyncImage(
                model = editeur.logo,
                contentDescription = "Logo de ${editeur.libelle}",
                modifier = Modifier
                    .size(64.dp)
                    .padding(4.dp),
                contentScale = ContentScale.Fit,
                error = painter,
                placeholder = painter
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = editeur.libelle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = getEditeurTypeLabel(editeur),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun EditeurGridItem(editeur: Editeur) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Business)
            AsyncImage(
                model = editeur.logo,
                contentDescription = "Logo de ${editeur.libelle}",
                modifier = Modifier
                    .size(100.dp)
                    .padding(4.dp),
                contentScale = ContentScale.Fit,
                error = painter,
                placeholder = painter
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = editeur.libelle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = getEditeurTypeLabel(editeur),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun getEditeurTypeLabel(editeur: Editeur): String {
    return when {
        editeur.exposant && editeur.distributeur -> "Exposant & Distributeur"
        editeur.exposant -> "Exposant"
        editeur.distributeur -> "Distributeur"
        else -> "Inconnu"
    }
}

// Wait, painterResource needs a resource ID. I'll use Icons.Default.Business instead.
// I'll fix the AsyncImage calls to use a custom painter if needed or just handle it better.

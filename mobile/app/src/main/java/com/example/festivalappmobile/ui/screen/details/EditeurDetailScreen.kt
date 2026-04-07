package com.example.festivalappmobile.ui.screen.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.festivalappmobile.ui.viewmodels.EditeurListViewModel
import com.example.festivalappmobile.ui.viewmodels.EditeurUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditeurDetailScreen(
    editeurId: Int,
    viewModel: EditeurListViewModel,
    onNavigateBack: () -> Unit,
    onEditClick: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    val editeur = when (val uiState = state.uiState) {
        is EditeurUiState.Success -> uiState.editeurs.find { it.id == editeurId }
        else -> null
    }

    if (showDeleteDialog && editeur != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer l'éditeur") },
            text = { Text("Êtes-vous sûr de vouloir supprimer l'éditeur \"${editeur.libelle}\" ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEditeur(editeurId)
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
                title = { Text(editeur?.libelle ?: "Détails de l'Éditeur") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (editeur != null) {
                        IconButton(onClick = { onEditClick(editeur.id) }) {
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
            when (val uiState = state.uiState) {
                is EditeurUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is EditeurUiState.Error -> {
                    Text(
                        text = "Erreur: ${uiState.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp).align(Alignment.Center)
                    )
                }
                is EditeurUiState.Success -> {
                    if (editeur == null) {
                        Text(text = "Éditeur non trouvé.", modifier = Modifier.padding(16.dp).align(Alignment.Center))
                    } else {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Logo Section
                            Card(
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                val painter = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Business)
                                AsyncImage(
                                    model = editeur.logo,
                                    contentDescription = "Logo de ${editeur.libelle}",
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    contentScale = ContentScale.Fit,
                                    error = painter,
                                    placeholder = painter
                                )
                            }

                            DetailSection("Information de l'Éditeur") {
                                DetailRow("Nom", editeur.libelle)
                                DetailRow("Type", getEditeurTypeLabel(editeur))
                            }

                            DetailSection("Contact") {
                                DetailRow("Téléphone", editeur.phone ?: "Non renseigné")
                                DetailRow("Email", editeur.email ?: "Non renseigné")
                            }

                            if (!editeur.notes.isNullOrBlank()) {
                                DetailSection("Notes") {
                                    Text(
                                        text = editeur.notes,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 4.dp)
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
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

fun getEditeurTypeLabel(editeur: Editeur): String {
    return when {
        editeur.exposant && editeur.distributeur -> "Exposant & Distributeur"
        editeur.exposant -> "Exposant"
        editeur.distributeur -> "Distributeur"
        else -> "Inconnu"
    }
}

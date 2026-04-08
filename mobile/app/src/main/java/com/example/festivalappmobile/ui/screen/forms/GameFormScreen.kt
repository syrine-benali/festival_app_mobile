package com.example.festivalappmobile.ui.screen.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.festivalappmobile.ui.viewmodels.GameFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameFormScreen(
    viewModel: GameFormViewModel,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(state.success) {
        if (state.success) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.id == 0) "Ajouter un jeu" else "Modifier le jeu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.libelle,
                onValueChange = { viewModel.onLibelleChange(it) },
                label = { Text("Nom du jeu *") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.error != null && state.libelle.isBlank()
            )

            OutlinedTextField(
                value = state.auteur,
                onValueChange = { viewModel.onAuteurChange(it) },
                label = { Text("Auteur") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = state.nbMinJoueur,
                    onValueChange = { viewModel.onMinPlayersChange(it) },
                    label = { Text("Nb Min Joueurs") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = state.nbMaxJoueur,
                    onValueChange = { viewModel.onMaxPlayersChange(it) },
                    label = { Text("Nb Max Joueurs") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = state.ageMin,
                    onValueChange = { viewModel.onAgeMinChange(it) },
                    label = { Text("Âge Min") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = state.duree,
                    onValueChange = { viewModel.onDureeChange(it) },
                    label = { Text("Durée (min)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = state.prototype, onCheckedChange = { viewModel.onPrototypeChange(it) })
                Text("Prototype")
            }

            OutlinedTextField(
                value = state.theme,
                onValueChange = { viewModel.onThemeChange(it) },
                label = { Text("Thème") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Editeur Selection
            var expandedEditeur by remember { mutableStateOf(false) }
            val selectedEditeur = state.editeurs.find { it.id == state.idEditeur }

            ExposedDropdownMenuBox(
                expanded = expandedEditeur,
                onExpandedChange = { expandedEditeur = !expandedEditeur },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedEditeur?.libelle ?: "Sélectionner un éditeur",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Éditeur") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEditeur) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedEditeur,
                    onDismissRequest = { expandedEditeur = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Aucun") },
                        onClick = {
                            viewModel.onEditeurChange(null)
                            expandedEditeur = false
                        }
                    )
                    state.editeurs.forEach { editeur ->
                        DropdownMenuItem(
                            text = { Text(editeur.libelle) },
                            onClick = {
                                viewModel.onEditeurChange(editeur.id)
                                expandedEditeur = false
                            }
                        )
                    }
                }
            }

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving && state.libelle.isNotBlank()
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Enregistrer")
                }
            }
        }
    }
}

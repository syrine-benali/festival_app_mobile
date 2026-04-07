package com.example.festivalappmobile.ui.screen.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.festivalappmobile.ui.viewmodels.EditeurFormEvent
import com.example.festivalappmobile.ui.viewmodels.EditeurFormUiEvent
import com.example.festivalappmobile.ui.viewmodels.EditeurFormViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditeurFormScreen(
    viewModel: EditeurFormViewModel,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val state = viewModel.state.value
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is EditeurFormEvent.Success -> {
                    onSuccess()
                }
                is EditeurFormEvent.Error -> {
                    // Error message is already in state as 'error'
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer un éditeur") },
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
                onValueChange = { viewModel.onEvent(EditeurFormUiEvent.EnteredLibelle(it)) },
                label = { Text("Nom (obligatoire)") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.error != null && state.libelle.isBlank()
            )

            OutlinedTextField(
                value = state.phone,
                onValueChange = { viewModel.onEvent(EditeurFormUiEvent.EnteredPhone(it)) },
                label = { Text("Téléphone") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onEvent(EditeurFormUiEvent.EnteredEmail(it)) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.onEvent(EditeurFormUiEvent.EnteredNotes(it)) },
                label = { Text("Notes supplémentaires") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = state.exposant,
                    onCheckedChange = { viewModel.onEvent(EditeurFormUiEvent.ChangedExposant(it)) }
                )
                Text(text = "Exposant")
                
                Spacer(modifier = Modifier.width(32.dp))

                Checkbox(
                    checked = state.distributeur,
                    onCheckedChange = { viewModel.onEvent(EditeurFormUiEvent.ChangedDistributeur(it)) }
                )
                Text(text = "Distributeur")
            }

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = { viewModel.onEvent(EditeurFormUiEvent.SaveEditeur) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Enregistrer")
                }
            }
        }
    }
}

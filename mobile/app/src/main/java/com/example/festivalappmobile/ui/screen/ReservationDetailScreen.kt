package com.example.festivalappmobile.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.festivalappmobile.domain.models.WorkflowStatus
import com.example.festivalappmobile.ui.components.WorkflowStatusChip
import com.example.festivalappmobile.ui.viewmodels.ReservationDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    viewModel: ReservationDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showWorkflowDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage, uiState.error) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it) }
        uiState.error?.let { snackbarHostState.showSnackbar("Erreur : $it") }
        viewModel.clearMessages()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.reservation?.editeurLibelle ?: "Réservation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.reservation != null -> {
                val res = uiState.reservation!!
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Section : Statut workflow
                    Card {
                        Column(modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Statut", style = MaterialTheme.typography.titleSmall)
                            WorkflowStatusChip(res.workflowStatus) { showWorkflowDialog = true }
                            Text("Festival : ${res.festivalNom}",
                                style = MaterialTheme.typography.bodyMedium)
                            Text("Type : ${res.typeReservant.label}",
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    // Section : Contacts
                    Card {
                        Column(modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Contacts / Relances",
                                    style = MaterialTheme.typography.titleSmall)
                                IconButton(onClick = { showContactDialog = true }) {
                                    Icon(Icons.Default.Add, "Ajouter contact")
                                }
                            }
                            if (res.reservationContacts.isEmpty()) {
                                Text("Aucun contact enregistré",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            res.reservationContacts.forEach { contact ->
                                Row(Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(contact.dateContact,
                                        style = MaterialTheme.typography.bodyMedium)
                                    contact.commentaire?.let {
                                        Text(it, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }

                    // Section : Flags
                    Card {
                        Column(modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Suivi jeux", style = MaterialTheme.typography.titleSmall)
                            CheckboxRow("Viendra présenter ses jeux",
                                res.viendraPresenteSesJeux) {
                                viewModel.updateFlag(viendraPresenteSesJeux = it)
                            }
                            CheckboxRow("Liste des jeux demandée",
                                res.listeJeuxDemandee) {
                                viewModel.updateFlag(listeJeuxDemandee = it)
                            }
                            CheckboxRow("Liste des jeux obtenue",
                                res.listeJeuxObtenue) {
                                viewModel.updateFlag(listeJeuxObtenue = it)
                            }
                            CheckboxRow("Jeux reçus physiquement",
                                res.jeuxRecusPhysiquement) {
                                viewModel.updateFlag(jeuxRecusPhysiquement = it)
                            }
                        }
                    }

                    // Section : Lignes tarifaires
                    Card {
                        Column(modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Réservation tables", style = MaterialTheme.typography.titleSmall)
                            res.reservationLines.forEach { line ->
                                Row(Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(line.zoneTarifaireNom)
                                    Text("${line.nbTables} table(s) — ${line.sousTotal}€")
                                }
                            }
                            Divider()
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total", style = MaterialTheme.typography.titleSmall)
                                Text("${res.totalTables} tables — ${res.totalPrice}€",
                                    style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }

                    // Section : Jeux
                    Card {
                        Column(modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Jeux présentés", style = MaterialTheme.typography.titleSmall)
                            if (res.reservationJeux.isEmpty()) {
                                Text("Aucun jeu enregistré",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            res.reservationJeux.forEach { jeu ->
                                Row(Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(jeu.jeuLibelle,
                                            style = MaterialTheme.typography.bodyMedium)
                                        jeu.zonePlanNom?.let {
                                            Text("Zone : $it",
                                                style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    Text("×${jeu.nbExemplaires} / ${jeu.nbTablesAllouees} table(s)",
                                        style = MaterialTheme.typography.bodySmall)
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog : Changer statut workflow
    if (showWorkflowDialog) {
        AlertDialog(
            onDismissRequest = { showWorkflowDialog = false },
            title = { Text("Changer le statut") },
            text = {
                Column {
                    WorkflowStatus.entries.forEach { status ->
                        TextButton(
                            onClick = {
                                viewModel.updateStatus(status)
                                showWorkflowDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(status.label) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showWorkflowDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Dialog : Ajouter contact
    if (showContactDialog) {
        var date by remember { mutableStateOf("") }
        var commentaire by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("Ajouter un contact") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (AAAA-MM-JJ)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = commentaire,
                        onValueChange = { commentaire = it },
                        label = { Text("Commentaire (optionnel)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addContactEntry(date, commentaire.ifBlank { null })
                    showContactDialog = false
                }) { Text("Ajouter") }
            },
            dismissButton = {
                TextButton(onClick = { showContactDialog = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun CheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
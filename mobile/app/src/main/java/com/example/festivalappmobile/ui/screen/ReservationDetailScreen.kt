package com.example.festivalappmobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.festivalappmobile.domain.models.ReservationJeu
import com.example.festivalappmobile.domain.models.ReservationLine
import com.example.festivalappmobile.domain.models.TypeRemise
import com.example.festivalappmobile.domain.models.TypeReservant
import com.example.festivalappmobile.domain.models.WorkflowStatus
import com.example.festivalappmobile.ui.components.WorkflowStatusChip
import com.example.festivalappmobile.ui.viewmodels.ReservationDetailViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    viewModel: ReservationDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val reservation = uiState.reservation

    var showWorkflowDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showLineDialog by remember { mutableStateOf(false) }
    var showJeuDialog by remember { mutableStateOf(false) }
    var showEditReservationDialog by remember { mutableStateOf(false) }
    var showDeleteReservationDialog by remember { mutableStateOf(false) }

    var editingLine by remember { mutableStateOf<ReservationLine?>(null) }
    var editingJeu by remember { mutableStateOf<ReservationJeu?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage, uiState.error) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it) }
        uiState.error?.let { snackbarHostState.showSnackbar("Erreur : $it") }
        viewModel.clearMessages()
    }

    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) {
            viewModel.consumeDeletedState()
            onBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(reservation?.editeurLibelle ?: "Réservation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (reservation != null) {
                        IconButton(onClick = { showEditReservationDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier la réservation")
                        }
                        IconButton(onClick = { showDeleteReservationDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer la réservation")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            reservation != null -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Statut", style = MaterialTheme.typography.titleSmall)
                            WorkflowStatusChip(reservation.workflowStatus) { showWorkflowDialog = true }
                            Text(
                                "Festival : ${reservation.festivalNom}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Type : ${reservation.typeReservant.label}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Contacts / Relances", style = MaterialTheme.typography.titleSmall)
                                IconButton(onClick = { showContactDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = "Ajouter contact")
                                }
                            }

                            if (reservation.reservationContacts.isEmpty()) {
                                Text(
                                    "Aucun contact enregistré",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            reservation.reservationContacts.forEach { contact ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(contact.dateContact, style = MaterialTheme.typography.bodyMedium)
                                        contact.commentaire?.let {
                                            Text(
                                                it,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteContactEntry(contact.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Supprimer contact")
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }

                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Suivi jeux", style = MaterialTheme.typography.titleSmall)
                            CheckboxRow(
                                "Viendra présenter ses jeux",
                                reservation.viendraPresenteSesJeux
                            ) {
                                viewModel.updateReservation(viendraPresenteSesJeux = it)
                            }
                            CheckboxRow(
                                "Nous présentons ses jeux",
                                reservation.nousPresentons
                            ) {
                                viewModel.updateReservation(nousPresentons = it)
                            }
                            CheckboxRow(
                                "Liste des jeux demandée",
                                reservation.listeJeuxDemandee
                            ) {
                                viewModel.updateReservation(listeJeuxDemandee = it)
                            }
                            CheckboxRow(
                                "Liste des jeux obtenue",
                                reservation.listeJeuxObtenue
                            ) {
                                viewModel.updateReservation(listeJeuxObtenue = it)
                            }
                            CheckboxRow(
                                "Jeux reçus physiquement",
                                reservation.jeuxRecusPhysiquement
                            ) {
                                viewModel.updateReservation(jeuxRecusPhysiquement = it)
                            }
                        }
                    }

                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Réservation tables", style = MaterialTheme.typography.titleSmall)
                                IconButton(onClick = { showLineDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = "Ajouter ligne")
                                }
                            }

                            if (reservation.reservationLines.isEmpty()) {
                                Text(
                                    "Aucune zone tarifaire",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            reservation.reservationLines.forEach { line ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(line.zoneTarifaireNom)
                                        Text(
                                            "${line.nbTables} table(s) - ${line.sousTotal}€",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(onClick = { editingLine = line }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Modifier ligne")
                                    }
                                    IconButton(onClick = { viewModel.deleteLineEntry(line.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Supprimer ligne")
                                    }
                                }
                                HorizontalDivider()
                            }

                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total tables", style = MaterialTheme.typography.titleSmall)
                                Text("${reservation.totalTables}", style = MaterialTheme.typography.titleSmall)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total à payer", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${uiState.totalToPay ?: reservation.totalPrice}€",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                    }

                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Jeux présentés", style = MaterialTheme.typography.titleSmall)
                                IconButton(onClick = { showJeuDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = "Ajouter jeu")
                                }
                            }

                            if (reservation.reservationJeux.isEmpty()) {
                                Text(
                                    "Aucun jeu enregistré",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            reservation.reservationJeux.forEach { jeu ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(jeu.jeuLibelle, style = MaterialTheme.typography.bodyMedium)
                                        jeu.zonePlanNom?.let {
                                            Text("Zone : $it", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text(
                                            "x${jeu.nbExemplaires} / ${jeu.nbTablesAllouees} table(s)",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(onClick = { editingJeu = jeu }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Modifier jeu")
                                    }
                                    IconButton(onClick = { viewModel.deleteJeuEntry(jeu.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Supprimer jeu")
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Réservation introuvable", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

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
                        ) {
                            Text(status.label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showWorkflowDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showContactDialog) {
        var date by remember { mutableStateOf("") }
        var commentaire by remember { mutableStateOf("") }
        var showContactDatePicker by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("Ajouter un contact") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date") },
                        placeholder = { Text("Choisir une date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { showContactDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choisir la date")
                    }
                    OutlinedTextField(
                        value = commentaire,
                        onValueChange = { commentaire = it },
                        label = { Text("Commentaire (optionnel)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = date.isNotBlank(),
                    onClick = {
                        viewModel.addContactEntry(date, commentaire.ifBlank { null })
                        showContactDialog = false
                    }
                ) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showContactDialog = false }) { Text("Annuler") }
            }
        )

        if (showContactDatePicker) {
            AppDatePickerDialog(
                initialDate = date,
                onDismiss = { showContactDatePicker = false },
                onDateSelected = { selected -> date = selected }
            )
        }
    }

    if (showLineDialog) {
        var zoneTarifaireId by remember { mutableStateOf("") }
        var nbTables by remember { mutableStateOf("") }
        var grandesTables by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showLineDialog = false },
            title = { Text("Ajouter une ligne tarifaire") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = zoneTarifaireId,
                        onValueChange = { zoneTarifaireId = it },
                        label = { Text("ID zone tarifaire") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nbTables,
                        onValueChange = { nbTables = it },
                        label = { Text("Nombre de tables") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    CheckboxRow("Grandes tables souhaitées", grandesTables) {
                        grandesTables = it
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val zoneId = zoneTarifaireId.toIntOrNull()
                        val tables = nbTables.toIntOrNull()
                        if (zoneId != null && tables != null) {
                            viewModel.addLineEntry(zoneId, tables, grandesTables)
                            showLineDialog = false
                        }
                    },
                    enabled = zoneTarifaireId.toIntOrNull() != null && nbTables.toIntOrNull() != null
                ) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLineDialog = false }) { Text("Annuler") }
            }
        )
    }

    editingLine?.let { line ->
        var nbTables by remember(line.id) { mutableStateOf(line.nbTables.toString()) }
        var nbM2 by remember(line.id) { mutableStateOf(line.nbM2.toString()) }
        var grandesTables by remember(line.id) { mutableStateOf(line.grandesTablesSouhaitees) }

        AlertDialog(
            onDismissRequest = { editingLine = null },
            title = { Text("Modifier la ligne") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nbTables,
                        onValueChange = { nbTables = it },
                        label = { Text("Nombre de tables") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nbM2,
                        onValueChange = { nbM2 = it },
                        label = { Text("Surface (m2)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    CheckboxRow("Grandes tables souhaitées", grandesTables) {
                        grandesTables = it
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateLineEntry(
                            lineId = line.id,
                            nbTables = nbTables.toIntOrNull(),
                            nbM2 = nbM2.toDoubleOrNull(),
                            grandesTablesSouhaitees = grandesTables
                        )
                        editingLine = null
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingLine = null }) { Text("Annuler") }
            }
        )
    }

    if (showJeuDialog) {
        var jeuId by remember { mutableStateOf("") }
        var nbExemplaires by remember { mutableStateOf("1") }
        var nbTablesAllouees by remember { mutableStateOf("1") }
        var zonePlanId by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showJeuDialog = false },
            title = { Text("Ajouter un jeu") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = jeuId,
                        onValueChange = { jeuId = it },
                        label = { Text("ID jeu") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nbExemplaires,
                        onValueChange = { nbExemplaires = it },
                        label = { Text("Nombre d'exemplaires") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nbTablesAllouees,
                        onValueChange = { nbTablesAllouees = it },
                        label = { Text("Tables allouées") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = zonePlanId,
                        onValueChange = { zonePlanId = it },
                        label = { Text("ID zone plan (optionnel)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedJeuId = jeuId.toIntOrNull()
                        val parsedNbExemplaires = nbExemplaires.toIntOrNull()
                        val parsedNbTables = nbTablesAllouees.toIntOrNull()
                        if (parsedJeuId != null && parsedNbExemplaires != null && parsedNbTables != null) {
                            viewModel.addJeuEntry(
                                jeuId = parsedJeuId,
                                nbExemplaires = parsedNbExemplaires,
                                nbTables = parsedNbTables,
                                zonePlanId = zonePlanId.toIntOrNull()
                            )
                            showJeuDialog = false
                        }
                    },
                    enabled = jeuId.toIntOrNull() != null &&
                        nbExemplaires.toIntOrNull() != null &&
                        nbTablesAllouees.toIntOrNull() != null
                ) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJeuDialog = false }) { Text("Annuler") }
            }
        )
    }

    editingJeu?.let { jeu ->
        var nbExemplaires by remember(jeu.id) { mutableStateOf(jeu.nbExemplaires.toString()) }
        var nbTablesAllouees by remember(jeu.id) { mutableStateOf(jeu.nbTablesAllouees.toString()) }
        var zonePlanId by remember(jeu.id) { mutableStateOf(jeu.zonePlanId?.toString() ?: "") }

        AlertDialog(
            onDismissRequest = { editingJeu = null },
            title = { Text("Modifier le jeu") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nbExemplaires,
                        onValueChange = { nbExemplaires = it },
                        label = { Text("Nombre d'exemplaires") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nbTablesAllouees,
                        onValueChange = { nbTablesAllouees = it },
                        label = { Text("Tables allouées") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = zonePlanId,
                        onValueChange = { zonePlanId = it },
                        label = { Text("ID zone plan (optionnel)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateJeuEntry(
                            jeuId = jeu.id,
                            nbExemplaires = nbExemplaires.toIntOrNull(),
                            nbTables = nbTablesAllouees.toIntOrNull(),
                            zonePlanId = zonePlanId.toIntOrNull()
                        )
                        editingJeu = null
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingJeu = null }) { Text("Annuler") }
            }
        )
    }

    if (showEditReservationDialog && reservation != null) {
        var typeReservant by remember(reservation.id) { mutableStateOf(reservation.typeReservant) }
        var typeRemise by remember(reservation.id) { mutableStateOf(reservation.typeRemise) }

        var dateFacturation by remember(reservation.id) {
            mutableStateOf(normalizeDateString(reservation.dateFacturation))
        }
        var notesClient by remember(reservation.id) { mutableStateOf(reservation.notesClient ?: "") }
        var notesWorkflow by remember(reservation.id) { mutableStateOf(reservation.notesWorkflow ?: "") }
        var nbPrises by remember(reservation.id) { mutableStateOf(reservation.nbPrisesElectriques.toString()) }
        var valeurRemise by remember(reservation.id) { mutableStateOf(reservation.valeurRemise.toString()) }
        var showFacturationDatePicker by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showEditReservationDialog = false },
            title = { Text("Modifier la réservation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val options = TypeReservant.entries
                            val index = options.indexOf(typeReservant)
                            typeReservant = options[(index + 1) % options.size]
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Type réservant : ${typeReservant.label}")
                    }

                    Button(
                        onClick = {
                            typeRemise = when (typeRemise) {
                                null -> TypeRemise.TABLES_OFFERTES
                                TypeRemise.TABLES_OFFERTES -> TypeRemise.SOMME_ARGENT
                                TypeRemise.SOMME_ARGENT -> null
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Type remise : ${typeRemise?.label ?: "Aucune"}")
                    }

                    OutlinedTextField(
                        value = valeurRemise,
                        onValueChange = { valeurRemise = it },
                        label = { Text("Valeur remise") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nbPrises,
                        onValueChange = { nbPrises = it },
                        label = { Text("Nombre de prises électriques") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dateFacturation,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date facturation") },
                        placeholder = { Text("Optionnel") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showFacturationDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Choisir date")
                        }
                        TextButton(
                            onClick = { dateFacturation = "" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Effacer")
                        }
                    }
                    OutlinedTextField(
                        value = notesClient,
                        onValueChange = { notesClient = it },
                        label = { Text("Notes client") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notesWorkflow,
                        onValueChange = { notesWorkflow = it },
                        label = { Text("Notes workflow") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateReservation(
                        typeReservant = typeReservant,
                        dateFacturation = dateFacturation.ifBlank { null },
                        notesClient = notesClient.ifBlank { null },
                        notesWorkflow = notesWorkflow.ifBlank { null },
                        typeRemise = typeRemise,
                        valeurRemise = valeurRemise.toDoubleOrNull(),
                        nbPrisesElectriques = nbPrises.toIntOrNull()
                    )
                    showEditReservationDialog = false
                }) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditReservationDialog = false }) { Text("Annuler") }
            }
        )

        if (showFacturationDatePicker) {
            AppDatePickerDialog(
                initialDate = dateFacturation,
                onDismiss = { showFacturationDatePicker = false },
                onDateSelected = { selected -> dateFacturation = selected }
            )
        }
    }

    if (showDeleteReservationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteReservationDialog = false },
            title = { Text("Supprimer la réservation") },
            text = { Text("Cette action est irréversible. Continuer ?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReservation()
                        showDeleteReservationDialog = false
                    },
                    enabled = !uiState.isDeleting
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteReservationDialog = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun CheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun normalizeDateString(rawDate: String?): String {
    if (rawDate.isNullOrBlank()) return ""
    return if (rawDate.length >= 10) rawDate.substring(0, 10) else rawDate
}

private fun dateStringToEpochMillis(dateString: String?): Long? {
    val normalized = normalizeDateString(dateString)
    if (normalized.isBlank()) return null

    return try {
        LocalDate.parse(normalized)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun epochMillisToDateString(epochMillis: Long?): String? {
    if (epochMillis == null) return null
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDatePickerDialog(
    initialDate: String?,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateStringToEpochMillis(initialDate)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selected = epochMillisToDateString(pickerState.selectedDateMillis)
                if (selected != null) {
                    onDateSelected(selected)
                }
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    ) {
        DatePicker(state = pickerState)
    }
}

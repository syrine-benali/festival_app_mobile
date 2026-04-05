package com.example.festivalappmobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.example.festivalappmobile.domain.models.TypeRemise
import com.example.festivalappmobile.domain.models.TypeReservant
import com.example.festivalappmobile.domain.models.WorkflowStatus
import com.example.festivalappmobile.ui.components.ReservationCard
import com.example.festivalappmobile.ui.viewmodels.ReservationListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationListScreen(
    viewModel: ReservationListViewModel,
    onReservationClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showFestivalPicker by remember { mutableStateOf(false) }
    var showTypePicker by remember { mutableStateOf(false) }
    var showRemisePicker by remember { mutableStateOf(false) }

    var editeurNameQuery by remember { mutableStateOf("") }
    var selectedEditeurId by remember { mutableStateOf<Int?>(null) }
    var selectedEditeurLabel by remember { mutableStateOf("") }

    var selectedFestivalId by remember { mutableStateOf<Int?>(null) }
    var selectedFestivalLabel by remember { mutableStateOf("") }

    var selectedTypeReservant by remember { mutableStateOf(TypeReservant.EDITEUR) }

    var selectedTypeRemise by remember { mutableStateOf<TypeRemise?>(null) }
    var nbPrisesText by remember { mutableStateOf("0") }
    var valeurRemiseText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    val filteredEditeurs = remember(editeurNameQuery, uiState.editeurs) {
        uiState.editeurs
            .filter { it.libelle.contains(editeurNameQuery, ignoreCase = true) }
            .take(20)
    }

    val nbPrises = nbPrisesText.toIntOrNull()
    val valeurRemise = valeurRemiseText.toDoubleOrNull()

    val canCreate = selectedEditeurId != null &&
        selectedFestivalId != null &&
        nbPrises != null &&
        nbPrises >= 0 &&
        (selectedTypeRemise == null || valeurRemise != null)

    val remiseLabel = when (selectedTypeRemise) {
        null -> "Aucune remise"
        TypeRemise.TABLES_OFFERTES -> TypeRemise.TABLES_OFFERTES.label
        TypeRemise.SOMME_ARGENT -> TypeRemise.SOMME_ARGENT.label
    }

    fun resetCreateForm() {
        editeurNameQuery = ""
        selectedEditeurId = null
        selectedEditeurLabel = ""
        selectedFestivalId = null
        selectedFestivalLabel = ""
        selectedTypeReservant = TypeReservant.EDITEUR
        selectedTypeRemise = null
        nbPrisesText = "0"
        valeurRemiseText = ""
        notesText = ""
    }

    LaunchedEffect(uiState.successMessage, uiState.error) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it) }
        uiState.error?.let { snackbarHostState.showSnackbar("Erreur : $it") }
        viewModel.clearMessages()
    }

    LaunchedEffect(uiState.createdReservationId) {
        val createdId = uiState.createdReservationId ?: return@LaunchedEffect
        onReservationClick(createdId)
        viewModel.consumeCreatedReservationNavigation()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Réservations") },
                actions = {
                    if (uiState.isOffline) {
                        Icon(
                            Icons.Default.WifiOff,
                            contentDescription = "Hors-ligne",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Créer une réservation")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (uiState.isOffline) {
                Surface(color = MaterialTheme.colorScheme.errorContainer) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "Mode hors-ligne - données en cache",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            val tabCount = WorkflowStatus.entries.size + 1
            val selectedTabIndex = (WorkflowStatus.entries.indexOf(uiState.filterStatus) + 1)
                .coerceIn(0, tabCount - 1)

            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = uiState.filterStatus == null,
                    onClick = { viewModel.setFilter(null) },
                    label = { Text("Tous") },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                WorkflowStatus.entries.forEach { status ->
                    FilterChip(
                        selected = uiState.filterStatus == status,
                        onClick = { viewModel.setFilter(status) },
                        label = { Text(status.label) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            when {
                uiState.isLoading && uiState.reservations.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
                uiState.reservations.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aucune réservation")
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.reservations) { res ->
                            ReservationCard(res) { onReservationClick(res.id) }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nouvelle réservation - étape 1") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editeurNameQuery,
                        onValueChange = {
                            editeurNameQuery = it
                            val exactMatch = uiState.editeurs.firstOrNull { e ->
                                e.libelle.equals(it, ignoreCase = true)
                            }
                            selectedEditeurId = exactMatch?.id
                            selectedEditeurLabel = exactMatch?.libelle ?: ""
                        },
                        singleLine = true,
                        label = { Text("Nom de l'éditeur") },
                        placeholder = { Text("Tapez le nom puis sélectionnez") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = if (selectedEditeurId != null)
                            "Éditeur sélectionné : $selectedEditeurLabel"
                        else
                            "Suggestions éditeurs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (filteredEditeurs.isEmpty()) {
                            Text(
                                "Aucun éditeur trouvé",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        filteredEditeurs.forEach { editeur ->
                            TextButton(
                                onClick = {
                                    selectedEditeurId = editeur.id
                                    selectedEditeurLabel = editeur.libelle
                                    editeurNameQuery = editeur.libelle
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(editeur.libelle)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = selectedFestivalLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Festival") },
                        placeholder = { Text("Choisir un festival") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { showFestivalPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choisir le festival")
                    }

                    OutlinedTextField(
                        value = selectedTypeReservant.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type d'éditeur / réservant") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { showTypePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choisir le type")
                    }

                    OutlinedTextField(
                        value = nbPrisesText,
                        onValueChange = { nbPrisesText = it },
                        singleLine = true,
                        label = { Text("Nombre de prises électriques") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = remiseLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Remise") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { showRemisePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choisir remise : oui/non/type")
                    }

                    if (selectedTypeRemise != null) {
                        OutlinedTextField(
                            value = valeurRemiseText,
                            onValueChange = { valeurRemiseText = it },
                            singleLine = true,
                            label = { Text("Valeur de la remise") },
                            placeholder = { Text("Ex: 1 (table) ou 150") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        minLines = 4,
                        maxLines = 8,
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.isCreateFormLoading) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createReservation(
                            editeurId = selectedEditeurId!!,
                            festivalId = selectedFestivalId!!,
                            typeReservant = selectedTypeReservant,
                            nbPrisesElectriques = nbPrises ?: 0,
                            typeRemise = selectedTypeRemise,
                            valeurRemise = valeurRemise,
                            notesClient = notesText.ifBlank { null }
                        )
                        showCreateDialog = false
                        resetCreateForm()
                    },
                    enabled = canCreate && !uiState.isLoading
                ) {
                    Text("Créer")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    resetCreateForm()
                }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showFestivalPicker) {
        AlertDialog(
            onDismissRequest = { showFestivalPicker = false },
            title = { Text("Choisir le festival") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    uiState.festivals.forEach { festival ->
                        TextButton(
                            onClick = {
                                selectedFestivalId = festival.id
                                selectedFestivalLabel = festival.nom
                                showFestivalPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${festival.nom} (${festival.dateDebut})")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFestivalPicker = false }) { Text("Fermer") }
            }
        )
    }

    if (showTypePicker) {
        AlertDialog(
            onDismissRequest = { showTypePicker = false },
            title = { Text("Choisir le type") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    TypeReservant.entries.forEach { type ->
                        TextButton(
                            onClick = {
                                selectedTypeReservant = type
                                showTypePicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(type.label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTypePicker = false }) { Text("Fermer") }
            }
        )
    }

    if (showRemisePicker) {
        AlertDialog(
            onDismissRequest = { showRemisePicker = false },
            title = { Text("Appliquer une remise ?") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    TextButton(
                        onClick = {
                            selectedTypeRemise = null
                            valeurRemiseText = ""
                            showRemisePicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Non - aucune remise")
                    }
                    TextButton(
                        onClick = {
                            selectedTypeRemise = TypeRemise.TABLES_OFFERTES
                            showRemisePicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Oui - tables offertes")
                    }
                    TextButton(
                        onClick = {
                            selectedTypeRemise = TypeRemise.SOMME_ARGENT
                            showRemisePicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Oui - somme d'argent")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRemisePicker = false }) { Text("Fermer") }
            }
        )
    }
}

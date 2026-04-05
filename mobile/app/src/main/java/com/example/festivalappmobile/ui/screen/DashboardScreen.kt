package com.example.festivalappmobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.festivalappmobile.domain.models.Editeur
import com.example.festivalappmobile.domain.models.Festival
import com.example.festivalappmobile.domain.models.Jeu
import com.example.festivalappmobile.ui.viewmodels.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        "Festivals (${uiState.festivals.size})",
        "Jeux (${uiState.jeux.size})",
        "Éditeurs (${uiState.editeurs.size})"
    )

    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar(
            title = { Text("Tableau de bord", style = MaterialTheme.typography.titleLarge) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            actions = {
                IconButton(onClick = { viewModel.loadAll() }) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Rafraîchir",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        )

        // Bannière mode hors ligne
        if (uiState.isOffline) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.WifiOff,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Mode hors ligne — données du dernier accès",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        ScrollableTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        when (selectedTab) {
            0 -> FestivalsTab(
                isLoading = uiState.isLoading,
                error = uiState.error,
                festivals = uiState.festivals,
                onRetry = { viewModel.loadAll() }
            )
            1 -> JeuxTab(
                isLoading = uiState.isLoading,
                error = uiState.error,
                jeux = uiState.jeux,
                noActiveFestival = uiState.noActiveFestival,
                isOffline = uiState.isOffline,
                onRetry = { viewModel.loadAll() }
            )
            2 -> EditeursTab(
                isLoading = uiState.isLoading,
                error = uiState.error,
                editeurs = uiState.editeurs,
                noActiveFestival = uiState.noActiveFestival,
                isOffline = uiState.isOffline,
                onRetry = { viewModel.loadAll() }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Onglet Festivals
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FestivalsTab(
    isLoading: Boolean,
    error: String?,
    festivals: List<Festival>,
    onRetry: () -> Unit
) {
    TabContent(
        isLoading = isLoading,
        error = error,
        isEmpty = festivals.isEmpty(),
        emptyMessage = "Aucun festival trouvé",
        onRetry = onRetry
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(festivals) { festival ->
                DashFestivalCard(festival)
            }
        }
    }
}

@Composable
private fun DashFestivalCard(festival: Festival) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                festival.nom,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            IconTextRow(icon = Icons.Filled.LocationOn, text = festival.lieu)
            Spacer(modifier = Modifier.height(4.dp))
            IconTextRow(
                icon = Icons.Filled.CalendarToday,
                text = "${formatDate(festival.dateDebut)}  →  ${formatDate(festival.dateFin)}"
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(
                    icon = Icons.Filled.Edit,
                    label = "${festival.nbTotalTable} tables"
                )
                InfoChip(
                    icon = Icons.Filled.Group,
                    label = "${festival.nbTotalChaise} chaises"
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Onglet Jeux
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun JeuxTab(
    isLoading: Boolean,
    error: String?,
    jeux: List<Jeu>,
    noActiveFestival: Boolean,
    isOffline: Boolean,
    onRetry: () -> Unit
) {
    val emptyMessage = if (noActiveFestival)
        "Aucun festival en cours aujourd'hui"
    else
        "Aucun jeu enregistré pour ce festival"

    TabContent(
        isLoading = isLoading,
        error = error,
        isEmpty = jeux.isEmpty(),
        emptyMessage = emptyMessage,
        onRetry = onRetry
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(jeux) { jeu ->
                DashJeuCard(jeu, isOffline = isOffline)
            }
        }
    }
}

@Composable
private fun DashJeuCard(jeu: Jeu, isOffline: Boolean = false) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {

            // Image du jeu (masquée en mode hors ligne)
            if (!isOffline) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(jeu.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = jeu.libelle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = jeu.libelle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (jeu.prototype) {
                        Spacer(modifier = Modifier.width(4.dp))
                        InfoChip(label = "Prototype", isHighlighted = true)
                    }
                }

                if (!jeu.auteur.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    IconTextRow(icon = Icons.Filled.Edit, text = jeu.auteur)
                }
                if (!jeu.editeurNom.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    IconTextRow(icon = Icons.Filled.Business, text = jeu.editeurNom)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (jeu.nbMinJoueur != null || jeu.nbMaxJoueur != null) {
                        val joueurs = buildString {
                            if (jeu.nbMinJoueur != null) append("${jeu.nbMinJoueur}")
                            if (jeu.nbMinJoueur != null && jeu.nbMaxJoueur != null) append("–")
                            if (jeu.nbMaxJoueur != null) append("${jeu.nbMaxJoueur}")
                            append(" joueurs")
                        }
                        InfoChip(icon = Icons.Filled.Group, label = joueurs)
                    }
                    if (jeu.duree != null) {
                        InfoChip(icon = Icons.Filled.AccessTime, label = "${jeu.duree} min")
                    }
                    if (jeu.ageMin != null) {
                        InfoChip(icon = Icons.Filled.Person, label = "${jeu.ageMin}+")
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Onglet Éditeurs
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EditeursTab(
    isLoading: Boolean,
    error: String?,
    editeurs: List<Editeur>,
    noActiveFestival: Boolean,
    isOffline: Boolean,
    onRetry: () -> Unit
) {
    val emptyMessage = if (noActiveFestival)
        "Aucun festival en cours aujourd'hui"
    else
        "Aucun éditeur enregistré pour ce festival"

    TabContent(
        isLoading = isLoading,
        error = error,
        isEmpty = editeurs.isEmpty(),
        emptyMessage = emptyMessage,
        onRetry = onRetry
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(editeurs) { editeur ->
                DashEditeurCard(editeur, isOffline = isOffline)
            }
        }
    }
}

@Composable
private fun DashEditeurCard(editeur: Editeur, isOffline: Boolean = false) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {

            // Logo de l'éditeur (masqué en mode hors ligne)
            if (!isOffline) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(editeur.logo)
                        .crossfade(true)
                        .build(),
                    contentDescription = editeur.libelle,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Business,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    editeur.libelle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (editeur.exposant) InfoChip(label = "Exposant", isHighlighted = true)
                    if (editeur.distributeur) InfoChip(label = "Distributeur")
                    if (editeur.hasReservation) {
                        InfoChip(
                            icon = Icons.Filled.Check,
                            label = "Réservé",
                            isHighlighted = true
                        )
                    }
                }

                if (!editeur.email.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    IconTextRow(icon = Icons.Filled.Email, text = editeur.email)
                }
                if (!editeur.phone.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    IconTextRow(icon = Icons.Filled.Phone, text = editeur.phone)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Composants utilitaires
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TabContent(
    isLoading: Boolean,
    error: String?,
    isEmpty: Boolean,
    emptyMessage: String = "Aucune donnée disponible",
    onRetry: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            error != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRetry) { Text("Réessayer") }
                }
            }
            isEmpty -> {
                Text(
                    emptyMessage,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> content()
        }
    }
}

// Ligne icône + texte (utilisée pour lieu, dates, auteur, éditeur, email, téléphone)
@Composable
private fun IconTextRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Chip compact avec icône optionnelle (utilisée pour les métadonnées)
@Composable
private fun InfoChip(
    label: String,
    icon: ImageVector? = null,
    isHighlighted: Boolean = false
) {
    val bgColor = if (isHighlighted)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val contentColor = if (isHighlighted)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = contentColor
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

// Formate une date ISO (2024-03-15T00:00:00.000Z → 15/03/2024)
private fun formatDate(dateStr: String): String {
    return try {
        val parts = dateStr.take(10).split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else dateStr
    } catch (e: Exception) {
        dateStr
    }
}

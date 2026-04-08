package com.example.festivalappmobile.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Écran d'accueil avec 3 boutons de navigation :
 * - Connexion → LoginScreen
 * - Inscription → RegisterScreen
 * - Tableau de bord → DashboardScreen (accès direct sans authentification)
 */
@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // ── Titre ──────────────────────────────────────────────────
            Text(
                text = "🎲", //TODO : mettre un bon logo au futur mais pas grave on garde celui ci 
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "Festival App",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Gérez vos festivals de jeux de société",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── Bouton Connexion ───────────────────────────────────────
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Login,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 0.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Connexion",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // ── Bouton Inscription ─────────────────────────────────────
            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Inscription",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // ── Séparateur ─────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "  ou  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            // ── Bouton Tableau de bord (accès direct) ──────────────────
            FilledTonalButton(
                onClick = onNavigateToDashboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Dashboard,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Tableau de bord",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Text(
                text = "Clique c'est magique ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

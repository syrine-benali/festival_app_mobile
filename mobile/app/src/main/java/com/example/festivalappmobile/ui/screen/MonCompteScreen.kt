package com.example.festivalappmobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.festivalappmobile.domain.models.User

@Composable
fun MonCompteScreen(
    user: User?,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Mon compte",
            style = MaterialTheme.typography.headlineSmall
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (user != null) {
                    Text("Email : ${user.email}", style = MaterialTheme.typography.bodyLarge)
                    Text("Nom : ${user.nom}", style = MaterialTheme.typography.bodyMedium)
                    Text("Prénom : ${user.prenom}", style = MaterialTheme.typography.bodyMedium)
                    Text("Rôle : ${user.role}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = if (user.valide) "Compte validé" else "Compte en attente de validation",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "Les informations du compte ne sont pas disponibles.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Reconnectez-vous pour recharger vos informations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Se déconnecter")
        }
    }
}

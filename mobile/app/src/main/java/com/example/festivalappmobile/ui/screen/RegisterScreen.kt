package com.example.festivalappmobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.festivalappmobile.domain.models.User
import com.example.festivalappmobile.ui.viewmodels.RegisterViewModel
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(),
    onRegistrationSuccess: (User) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && uiState.user != null) {
            delay(1000) // on attend 1 secnde pour voir ce message 
            onRegistrationSuccess(uiState.user!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Créer un compte", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Veuillez remplir tous les champs pour vous inscrire",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.prenom,
            onValueChange = { viewModel.onPrenomChange(it) },
            label = { Text("Prénom") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.nom,
            onValueChange = { viewModel.onNomChange(it) },
            label = { Text("Nom") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            singleLine = true
        )
        
        // Afficher les exigences du password
        if (uiState.password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Exigences du mot de passe:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            PasswordRequirement(
                label = "Au moins 8 caractères",
                isMet = uiState.password.length >= 8
            )
            PasswordRequirement(
                label = "Au moins une majuscule (A-Z)",
                isMet = uiState.password.any { c -> c.isUpperCase() }
            )
            PasswordRequirement(
                label = "Au moins une minuscule (a-z)",
                isMet = uiState.password.any { c -> c.isLowerCase() }
            )
            PasswordRequirement(
                label = "Au moins un chiffre (0-9)",
                isMet = uiState.password.any { c -> c.isDigit() }
            )
            PasswordRequirement(
                label = "Au moins un caractère spécial (!@#$%^&* etc)",
                isMet = uiState.password.any { c -> !c.isLetterOrDigit() }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.register() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("S'inscrire")
            }
        }

        if (uiState.isSuccess) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "✓ Compte créé avec succès. Veuillez attendre la validation par l'administrateur pour pouvoir avoir accès à l'application.",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Lien pour retourner à la connexion
        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = !uiState.isLoading
        ) {
            Text("Vous avez déjà un compte ? Se connecter")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onNavigateToDashboard,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = !uiState.isLoading
        ) {
            Text("Voir le tableau de bord →")
        }
    }
}

@Composable
fun PasswordRequirement(label: String, isMet: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isMet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isMet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

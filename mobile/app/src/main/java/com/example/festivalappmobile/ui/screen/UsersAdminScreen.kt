package com.example.festivalappmobile.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.festivalappmobile.domain.models.User
import com.example.festivalappmobile.ui.viewmodels.UsersManagementViewModel
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api


@Composable
fun UsersAdminScreen(viewModel: UsersManagementViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Gestion des Utilisateurs",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Success Message
        if (uiState.successMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD4EDDA))
            ) {
                Text(
                    text = uiState.successMessage ?: "",
                    modifier = Modifier.padding(12.dp),
                    color = Color(0xFF155724),
                    fontSize = 14.sp
                )
            }
        }

        // Error Message
        if (uiState.error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8D7DA))
            ) {
                Text(
                    text = uiState.error ?: "",
                    modifier = Modifier.padding(12.dp),
                    color = Color(0xFF721C24),
                    fontSize = 14.sp
                )
            }
        }

        // Update Error Message
        if (uiState.updateError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8D7DA))
            ) {
                Text(
                    text = uiState.updateError ?: "",
                    modifier = Modifier.padding(12.dp),
                    color = Color(0xFF721C24),
                    fontSize = 14.sp
                )
            }
        }

        // Loading State
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.users.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                Text("Aucun utilisateur trouvé")
            }
        } else {
            // Users List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.users) { user ->
                    UserCard(
                        user = user,
                        isUpdating = uiState.isUpdating,
                        onUpdateUser = { valide, role ->
                            viewModel.updateUserValidation(user.id, valide, role)
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCard(
    user: User,
    isUpdating: Boolean,
    onUpdateUser: (Boolean, String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(user.role) }
    var isValidated by remember { mutableStateOf(user.valide) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // User Email (Primary)
            Text(
                text = user.email,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // User Names
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Nom: ${user.nom}",
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Prénom: ${user.prenom}",
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // Current Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Validation Status Chip
                val validationBgColor = if (user.valide) Color(0xFFD4EDDA) else Color(0xFFF8D7DA)
                val validationTextColor = if (user.valide) Color(0xFF155724) else Color(0xFF721C24)

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = validationBgColor)
                ) {
                    Text(
                        text = if (user.valide) "✓ Validé" else "✗ Non validé",
                        fontSize = 12.sp,
                        color = validationTextColor,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Role Chip
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Text(
                        text = "Rôle: ${user.role}",
                        fontSize = 12.sp,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Edit Button
            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier.align(Alignment.End),
                enabled = !isUpdating
            ) {
                Text("Modifier")
            }
        }
    }

    // Edit Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Modifier l'utilisateur: ${user.email}") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Validation Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Valider l'utilisateur")
                        Switch(
                            checked = isValidated,
                            onCheckedChange = { isValidated = it }
                        )
                    }

                    // Role Dropdown
                    Text("Rôle", fontWeight = FontWeight.Bold)
                    var expandedRole by remember { mutableStateOf(false) }
                    
                    Box {
                        OutlinedTextField(
                            value = selectedRole,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sélectionner un rôle") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) }
                        )
                        DropdownMenu(
                            expanded = expandedRole,
                            onDismissRequest = { expandedRole = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("USER", "ADMIN", "EDITEUR").forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role) },
                                    onClick = {
                                        selectedRole = role
                                        expandedRole = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateUser(isValidated, selectedRole)
                        showEditDialog = false
                    },
                    enabled = !isUpdating
                ) {
                    Text("Valider")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showEditDialog = false },
                    enabled = !isUpdating
                ) {
                    Text("Annuler")
                }
            }
        )
    }
}

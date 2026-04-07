package com.example.festivalappmobile.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.festivalappmobile.domain.models.WorkflowStatus

@Composable
fun WorkflowStatusChip(status: WorkflowStatus, onClick: (() -> Unit)? = null) {
    val scheme = MaterialTheme.colorScheme
    val (bg, content) = when (status) {
        WorkflowStatus.PAS_DE_CONTACT -> scheme.surfaceVariant to scheme.onSurfaceVariant
        WorkflowStatus.CONTACT_PRIS -> scheme.primaryContainer to scheme.onPrimaryContainer
        WorkflowStatus.DISCUSSION_EN_COURS -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        WorkflowStatus.SERA_ABSENT -> scheme.errorContainer to scheme.onErrorContainer
        WorkflowStatus.CONSIDERE_ABSENT -> scheme.errorContainer to scheme.onErrorContainer
        WorkflowStatus.PRESENT -> scheme.secondaryContainer to scheme.onSecondaryContainer
        WorkflowStatus.FACTURE -> scheme.primaryContainer to scheme.onPrimaryContainer
        WorkflowStatus.FACTURE_PAYEE -> scheme.secondaryContainer to scheme.onSecondaryContainer
    }
    if (onClick != null) {
        FilterChip(
            selected = true,
            onClick = onClick,
            label = { Text(status.label, color = content) },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = bg)
        )
    } else {
        SuggestionChip(
            onClick = {},
            label = { Text(status.label, color = content) },
            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = bg)
        )
    }
}
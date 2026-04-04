package com.example.festivalappmobile.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.festivalappmobile.domain.models.WorkflowStatus

@Composable
fun WorkflowStatusChip(status: WorkflowStatus, onClick: (() -> Unit)? = null) {
    val (bg, content) = when (status) {
        WorkflowStatus.PAS_DE_CONTACT   -> Color(0xFFE0E0E0) to Color(0xFF333333)
        WorkflowStatus.CONTACT_PRIS     -> Color(0xFFBBDEFB) to Color(0xFF0D47A1)
        WorkflowStatus.DISCUSSION_EN_COURS -> Color(0xFFFFF9C4) to Color(0xFF827717)
        WorkflowStatus.SERA_ABSENT      -> Color(0xFFFFCDD2) to Color(0xFFB71C1C)
        WorkflowStatus.CONSIDERE_ABSENT -> Color(0xFFFFCDD2) to Color(0xFF7F0000)
        WorkflowStatus.PRESENT          -> Color(0xFFC8E6C9) to Color(0xFF1B5E20)
        WorkflowStatus.FACTURE          -> Color(0xFFE1BEE7) to Color(0xFF4A148C)
        WorkflowStatus.FACTURE_PAYEE    -> Color(0xFF81D4FA) to Color(0xFF01579B)
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
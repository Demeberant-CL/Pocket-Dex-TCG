package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.PocketEnergyType
import com.example.ui.viewmodel.SortOption

@Composable
fun PocketFilterBar(
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    selectedExpansion: String,
    onExpansionSelected: (String) -> Unit,
    selectedEnergy: String,
    onEnergySelected: (String) -> Unit,
    currentSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val expansions = listOf(
        "ALL" to "Todas las Expansiones",
        "Genetic Apex" to "Genetic Apex (A1)",
        "Mythical Island" to "Mythical Island (A1a)",
        "Space-Time Smackdown" to "Space-Time Smackdown (A2)",
        "Promo-A" to "Promo-A"
    )

    val energies = listOf(
        "ALL" to "Todos los Tipos",
        "Grass" to "🍃 Planta",
        "Fire" to "🔥 Fuego",
        "Water" to "💧 Agua",
        "Lightning" to "⚡ Rayo",
        "Psychic" to "🔮 Psíquico",
        "Fighting" to "🥊 Lucha",
        "Darkness" to "🌑 Oscuridad",
        "Metal" to "⚙️ Metal",
        "Colorless" to "⚪ Incoloro",
        "Trainer" to "🎒 Entrenador"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Barra de búsqueda con selector de orden
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_card_input"),
                placeholder = { Text("Buscar Pokémon o código (ej: A1-096)...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar búsqueda"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Botón de ordenamiento
            IconButton(
                onClick = { sortMenuExpanded = true },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("sort_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Ordenar catálogo",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false }
            ) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.displayName,
                                color = if (option == currentSort) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onSortSelected(option)
                            sortMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Fila de Expansiones desplazable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            expansions.forEach { (key, label) ->
                val isSelected = selectedExpansion == key
                FilterChip(
                    selected = isSelected,
                    onClick = { onExpansionSelected(key) },
                    label = { Text(label) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Fila de Tipos de Energía desplazable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            energies.forEach { (key, label) ->
                val isSelected = selectedEnergy == key
                FilterChip(
                    selected = isSelected,
                    onClick = { onEnergySelected(key) },
                    label = { Text(label) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }
    }
}

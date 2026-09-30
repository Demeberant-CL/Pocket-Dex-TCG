package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PocketEnergyType
import com.example.data.local.entity.PocketRarity
import com.example.ui.viewmodel.SortOption

/**
 * Barra superior de filtros reactivos para la colección de Pokémon TCG Pocket:
 * - Filtros rápidos por Tipo de Energía (con orbes elementales).
 * - Filtros rápidos por Rareza oficial (Diamantes, Estrellas, Corona dorada).
 * - Selector de columnas de cuadrícula (2 columnas detalladas vs 3 columnas álbum).
 * - Ordenamiento reactivo por StateFlow.
 */
@Composable
fun CollectionFilterHeader(
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    selectedEnergy: String,
    onEnergySelected: (String) -> Unit,
    selectedRarity: String,
    onRaritySelected: (String) -> Unit,
    currentSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    gridColumns: Int,
    onToggleGridColumns: () -> Unit,
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    // Tipos de energía con sus colores característicos
    val energyList = listOf(
        Triple("ALL", "Todos", "✨"),
        Triple("Grass", "Planta", "🍃"),
        Triple("Fire", "Fuego", "🔥"),
        Triple("Water", "Agua", "💧"),
        Triple("Lightning", "Rayo", "⚡"),
        Triple("Psychic", "Psíquico", "🔮"),
        Triple("Fighting", "Lucha", "🥊"),
        Triple("Darkness", "Oscuridad", "🌑"),
        Triple("Metal", "Metal", "⚙️"),
        Triple("Colorless", "Incoloro", "⚪"),
        Triple("Trainer", "Entrenador", "🎒")
    )

    // Rarezas oficiales de Pokémon TCG Pocket
    val rarityList = listOf(
        Pair("ALL", "Todas las Rarezas"),
        Pair("ONE_DIAMOND", "♢ 1D"),
        Pair("TWO_DIAMONDS", "♢♢ 2D"),
        Pair("THREE_DIAMONDS", "♢♢♢ 3D"),
        Pair("FOUR_DIAMONDS", "♢♢♢♢ 4D (ex)"),
        Pair("ONE_STAR", "☆ 1★"),
        Pair("TWO_STARS", "☆☆ 2★"),
        Pair("THREE_STARS", "☆☆☆ Inmersiva"),
        Pair("CROWN", "👑 Corona"),
        Pair("PROMO", "Promo-A")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        // 1. Barra de Búsqueda con selector de columnas y botón de orden
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
                placeholder = { Text("Buscar carta o ID (ej: Charizard, A1-096)...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar texto",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Toggle para alternar entre 2 y 3 columnas (cuadrícula detallada vs álbum)
            IconButton(
                onClick = onToggleGridColumns,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .testTag("toggle_grid_columns_button")
            ) {
                Icon(
                    imageVector = if (gridColumns == 2) Icons.Default.GridView else Icons.Default.ViewAgenda,
                    contentDescription = "Cambiar vista de cuadrícula",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Botón de Ordenamiento
            Box {
                IconButton(
                    onClick = { sortMenuExpanded = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .testTag("sort_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Ordenar catálogo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
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
                                    fontWeight = if (option == currentSort) FontWeight.Bold else FontWeight.Normal,
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
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Fila reactiva: Filtro por Tipo de Energía (con orbes)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TIPO DE ENERGÍA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            // Indicador si hay filtros aplicados
            AnimatedVisibility(
                visible = hasActiveFilters,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                        .clickable { onClearFilters() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Limpiar filtros",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            energyList.forEach { (code, name, emoji) ->
                val isSelected = selectedEnergy == code
                EnergyFilterChip(
                    label = name,
                    emoji = emoji,
                    isSelected = isSelected,
                    onClick = { onEnergySelected(code) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Fila reactiva: Filtro por Rareza Oficial de Pocket
        Text(
            text = "RAREZA TCG POCKET",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFB800),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            rarityList.forEach { (code, label) ->
                val isSelected = selectedRarity == code
                RarityFilterChip(
                    code = code,
                    label = label,
                    isSelected = isSelected,
                    onClick = { onRaritySelected(code) }
                )
            }
        }
    }
}

@Composable
private fun EnergyFilterChip(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) {
        Brush.horizontalGradient(listOf(Color(0xFF0070F3), Color(0xFF06B6D4)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF1E293B)))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(
                width = if (isSelected) 1.2.dp else 0.5.dp,
                color = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun RarityFilterChip(
    code: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isCrown = code == "CROWN"
    val isImmersive = code == "THREE_STARS"

    val bg = when {
        isSelected && isCrown -> Brush.horizontalGradient(listOf(Color(0xFFFFB800), Color(0xFFFF8F00)))
        isSelected && isImmersive -> Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))
        isSelected -> Brush.horizontalGradient(listOf(Color(0xFF0070F3), Color(0xFF0284C7)))
        else -> Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF1E293B)))
    }

    val borderColor = when {
        isSelected && isCrown -> Color(0xFFFFD54F)
        isSelected && isImmersive -> Color(0xFFF472B6)
        isSelected -> Color(0xFF38BDF8)
        else -> Color.White.copy(alpha = 0.1f)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(
                width = if (isSelected) 1.2.dp else 0.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f)
        )
    }
}

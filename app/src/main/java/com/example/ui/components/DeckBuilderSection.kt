package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.deck.DeckCardEntry
import com.example.data.deck.DeckStrategy
import com.example.data.deck.GeneratedDeck
import com.example.data.local.entity.DeckEntity
import com.example.data.local.entity.PocketEnergyType

/**
 * Interfaz interactiva del Asistente de IA para creación de mazos equilibrados de 20 cartas
 * optimizado para Pokémon TCG Pocket (Módulo 3).
 */
@Composable
fun DeckBuilderSection(
    generatedDeck: GeneratedDeck?,
    savedDecks: List<DeckEntity>,
    isGenerating: Boolean,
    selectedStrategy: DeckStrategy,
    onStrategySelected: (DeckStrategy) -> Unit,
    selectedEnergy: String?,
    onEnergySelected: (String) -> Unit,
    onGenerateDeck: () -> Unit,
    onSaveDeck: () -> Unit,
    onDeleteSavedDeck: (String) -> Unit,
    onSelectCardForDetail: (com.example.data.local.entity.CardEntity) -> Unit,
    noticeMessage: String?,
    onDismissNotice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val energyOptions = listOf(
        "Psychic" to "🔮 Psíquico",
        "Lightning" to "⚡ Rayo",
        "Water" to "💧 Agua",
        "Fire" to "🔥 Fuego",
        "Grass" to "🍃 Planta",
        "Metal" to "⚙️ Metal"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Banner Superior del Asistente Deck Builder IA
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF0070F3), Color(0xFFFFB800)))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF0070F3)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Asistente AI Deck Builder",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Optimizado para 20 cartas • Pokémon TCG Pocket",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Text(
                            text = "20 CARTAS",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selector de Estrategia
                Text(
                    text = "ESTRATEGIA DE CONSTRUCCIÓN",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DeckStrategy.entries.forEach { strat ->
                        val isSelected = selectedStrategy == strat
                        FilterChip(
                            selected = isSelected,
                            onClick = { onStrategySelected(strat) },
                            label = { Text(strat.displayName) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Selector de Tipo de Energía si la estrategia es Monotipo
                if (selectedStrategy == DeckStrategy.MONO_TYPE) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ENERGÍA MONOTIPO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB800)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        energyOptions.forEach { (key, label) ->
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

                Spacer(modifier = Modifier.height(14.dp))

                // Botón Generar
                Button(
                    onClick = onGenerateDeck,
                    enabled = !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("generate_deck_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analizando meta y sinergias...")
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generar Mazo Meta (20 Cartas)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Mensaje de notificación / aviso
        if (noticeMessage != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = noticeMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismissNotice,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("✕", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Mazo Generado
        if (generatedDeck != null) {
            GeneratedDeckCard(
                deck = generatedDeck,
                onSave = onSaveDeck,
                onCardClick = onSelectCardForDetail,
                onCopyDecklist = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val text = buildString {
                        appendLine("=== MAZO POKÉMON TCG POCKET ===")
                        appendLine("Arquetipo: ${generatedDeck.name} (${generatedDeck.analysis.metaTier})")
                        appendLine("Sinergia: ${generatedDeck.analysis.synergyScore}/100 • Total: 20 cartas")
                        appendLine("-------------------------------")
                        generatedDeck.cards.forEach { entry ->
                            appendLine("${entry.count}x ${entry.card.name} (${entry.card.id})")
                        }
                    }
                    clipboard.setPrimaryClip(ClipData.newPlainText("DeckList", text))
                    Toast.makeText(context, "¡Lista de 20 cartas copiada al portapapeles!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 3. Mazos Guardados Previamente
        if (savedDecks.isNotEmpty()) {
            Text(
                text = "MIS MAZOS GUARDADOS (${savedDecks.size})",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            savedDecks.forEach { saved ->
                SavedDeckItem(
                    deck = saved,
                    onDelete = { onDeleteSavedDeck(saved.id) }
                )
            }
        }
    }
}

@Composable
private fun GeneratedDeckCard(
    deck: GeneratedDeck,
    onSave: () -> Unit,
    onCardClick: (com.example.data.local.entity.CardEntity) -> Unit,
    onCopyDecklist: () -> Unit
) {
    val analysis = deck.analysis

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        ),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera: Nombre de Arquetipo, Tier y Energía
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deck.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Arquetipo Meta • Formato 20 cartas",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (analysis.metaTier) {
                        "Tier S" -> Color(0xFFFFB800)
                        "Tier A" -> Color(0xFF8B5CF6)
                        else -> Color(0xFF0070F3)
                    }
                ) {
                    Text(
                        text = analysis.metaTier,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Medidor de Sinergia y Curva
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Índice de Sinergia del Mazo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                        Text(
                            text = "${analysis.synergyScore}/100",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { analysis.synergyScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF38BDF8),
                        trackColor = Color(0xFF0F172A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cuatro Métricas Clave del Mazo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniStatItem(
                    label = "Total Cartas",
                    value = "${deck.totalCardCount}/20",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                MiniStatItem(
                    label = "Pokémon (Bás./Evo)",
                    value = "${analysis.pokemonCount} (${analysis.basicCount}/${analysis.evolutionCount})",
                    color = Color(0xFF06B6D4),
                    modifier = Modifier.weight(1f)
                )
                MiniStatItem(
                    label = "Entrenadores",
                    value = "${analysis.trainerCount}",
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                MiniStatItem(
                    label = "Coste Medio",
                    value = "${analysis.averageEnergyCost} ⚡",
                    color = Color(0xFFEC4899),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Lista de 20 cartas del mazo
            Text(
                text = "COMPOSICIÓN DEL MAZO (20 CARTAS)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                deck.cards.forEach { entry ->
                    DeckCardRow(entry = entry, onClick = { onCardClick(entry.card) })
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Análisis Táctico de Fortalezas
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ventajas y Puntos Fuertes Tácticos",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    analysis.strengths.forEach { str ->
                        Text(
                            text = "• $str",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }

                    if (analysis.missingCardsRecommendations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Estado de tu Colección",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        analysis.missingCardsRecommendations.forEach { rec ->
                            Text(
                                text = "• $rec",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción del mazo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCopyDecklist,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copiar Lista", fontSize = 12.sp)
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar Mazo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DeckCardRow(
    entry: DeckCardEntry,
    onClick: () -> Unit
) {
    val card = entry.card
    val energy = PocketEnergyType.fromString(card.energyType)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.6f))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Badge Cantidad (1x o 2x)
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF0070F3).copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
            ) {
                Text(
                    text = "${entry.count}x",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = energy.emoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = card.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Color.White
            )
            if (card.hp > 0) {
                Text(
                    text = " (${card.hp} HP)",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }

        // Estado de posesión en la colección
        if (entry.isOwnedInCollection) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "En colección",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(16.dp)
            )
        } else {
            Text(
                text = "Falta en álbum",
                fontSize = 10.sp,
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MiniStatItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.8f))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SavedDeckItem(
    deck: DeckEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sinergia: ${deck.synergyScore}/100 • ${deck.metaTier} • Energía ${deck.primaryEnergy}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar mazo",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CardEntity
import com.example.data.local.entity.PocketEnergyType
import com.example.data.trade.BalancedTradePair
import com.example.data.trade.TradeComparisonResult
import com.example.data.trade.TradeMatch

/**
 * Interfaz interactiva de la Zona de Intercambios (Módulo 4: Trade Matcher).
 * Permite comparar la Wishlist de un amigo contra los duplicados del usuario (quantity > 1)
 * y calcular intercambios justos 1:1 en tiempo real.
 */
@Composable
fun TradeZoneSection(
    comparisonResult: TradeComparisonResult,
    onFriendWishlistInputChanged: (String) -> Unit,
    currentWishlistInput: String,
    onApplyPreset: (name: String, wishlistIds: Set<String>, duplicates: List<CardEntity>) -> Unit,
    onSelectCardForDetail: (CardEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Presets de amigos de prueba para testing instantáneo
    val presets = listOf(
        Triple("Rival Gary", setOf("A1-047", "A1-096", "A1-089"), listOf(
            CardEntity("A1-103", "Zapdos ex", "Genetic Apex", "FOUR_DIAMONDS", "Lightning", isOwned = true, quantity = 2),
            CardEntity("A1-056", "Blastoise ex", "Genetic Apex", "FOUR_DIAMONDS", "Water", isOwned = true, quantity = 2)
        )),
        Triple("Entrenador Red", setOf("A1-084", "A1-129", "A1-073"), listOf(
            CardEntity("A1-137", "Gengar ex", "Genetic Apex", "FOUR_DIAMONDS", "Psychic", isOwned = true, quantity = 2),
            CardEntity("A1a-086", "Mew ex (Inmersiva)", "Mythical Island", "THREE_STARS", "Psychic", isOwned = true, quantity = 2)
        )),
        Triple("Líder Erika", setOf("A1-004", "A1-177"), listOf(
            CardEntity("A2-021", "Palkia ex", "Space-Time Smackdown", "FOUR_DIAMONDS", "Water", isOwned = true, quantity = 2)
        ))
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Tarjeta de Entrada de la Wishlist del Amigo
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF0070F3), Color(0xFF06B6D4)))
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
                                .background(Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF0070F3)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Zona de Intercambios (Trades)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Comparador de Duplicados vs Wishlist",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF059669).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Text(
                            text = "${comparisonResult.totalPossibleTrades} TRADES",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selector rápido de amigos de prueba
                Text(
                    text = "AMIGOS DE PRUEBA (TEST PRESETS)",
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
                    presets.forEach { (name, wishlist, dupes) ->
                        val isSelected = comparisonResult.friendName == name
                        FilterChip(
                            selected = isSelected,
                            onClick = { onApplyPreset(name, wishlist, dupes) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            label = { Text(name) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Campo de texto para Wishlist personalizada
                OutlinedTextField(
                    value = currentWishlistInput,
                    onValueChange = onFriendWishlistInputChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("friend_wishlist_input"),
                    label = { Text("Wishlist del amigo (IDs separados por coma)") },
                    placeholder = { Text("ej: A1-047, A1-096, A1-129, A1-084") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    ),
                    singleLine = false,
                    maxLines = 2
                )
            }
        }

        // 2. Sección: Intercambios Óptimos 1 a 1 Equitativos (Fair Trades)
        if (comparisonResult.optimalFairTrades.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF10B981).copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Intercambios 1:1 Equitativos (Win-Win)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA7F3D0)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF059669)
                        ) {
                            Text(
                                text = "${comparisonResult.optimalFairTrades.size} PARES",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Coincidencias de idéntica rareza donde ambos entrenadores ganan una carta deseada.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                    )

                    comparisonResult.optimalFairTrades.forEach { pair ->
                        BalancedPairRow(pair = pair, onSelectCard = onSelectCardForDetail)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        // 3. Sección: Tus duplicados que tu amigo busca
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tus Duplicados que ${comparisonResult.friendName} Busca (${comparisonResult.matchesToGive.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (comparisonResult.matchesToGive.isEmpty()) {
                    Text(
                        text = "No tienes duplicados (quantity > 1) que coincidan con la Wishlist de tu amigo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    comparisonResult.matchesToGive.forEach { match ->
                        TradeMatchRow(match = match, isGiving = true, onClick = { onSelectCardForDetail(match.card) })
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // 4. Sección: Duplicados del amigo que TÚ buscas
        if (comparisonResult.matchesToReceive.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Cartas de ${comparisonResult.friendName} para tu Wishlist (${comparisonResult.matchesToReceive.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    comparisonResult.matchesToReceive.forEach { match ->
                        TradeMatchRow(match = match, isGiving = false, onClick = { onSelectCardForDetail(match.card) })
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // 5. Botón Copiar Propuesta de Intercambio
        if (comparisonResult.hasTradesAvailable) {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val proposalText = buildString {
                        appendLine("🤝 PROPUESTA DE INTERCAMBIO POKÉMON TCG POCKET")
                        appendLine("Para: ${comparisonResult.friendName}")
                        appendLine("-------------------------------------------------")
                        if (comparisonResult.optimalFairTrades.isNotEmpty()) {
                            appendLine("✨ INTERCAMBIOS EQUITATIVOS RECOMENDADOS (1:1):")
                            comparisonResult.optimalFairTrades.forEach { pair ->
                                appendLine("• Yo te doy: ${pair.cardGiven.name} (${pair.cardGiven.id}) ⇄ Tú me das: ${pair.cardReceived.name} (${pair.cardReceived.id})")
                            }
                            appendLine()
                        }
                        if (comparisonResult.matchesToGive.isNotEmpty()) {
                            appendLine("📦 Tengo estos duplicados de tu Wishlist:")
                            comparisonResult.matchesToGive.forEach { m ->
                                appendLine("• ${m.card.name} (${m.card.id}) [${m.extraCopiesAvailable} copias extras]")
                            }
                        }
                    }
                    clipboard.setPrimaryClip(ClipData.newPlainText("TradeProposal", proposalText))
                    Toast.makeText(context, "¡Propuesta de intercambio copiada al portapapeles!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("copy_trade_proposal_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copiar Propuesta para Compartir", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BalancedPairRow(
    pair: BalancedTradePair,
    onSelectCard: (CardEntity) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.8f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Tu carta
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectCard(pair.cardGiven) }
            ) {
                Text(
                    text = "Tú entregas:",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    text = pair.cardGiven.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF38BDF8)
                )
                Text(
                    text = "${pair.cardGiven.id} • ${pair.cardGiven.rarity}",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Icono de intercambio
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF059669)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Carta del amigo
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectCard(pair.cardReceived) }
            ) {
                Text(
                    text = "Tú recibes:",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    text = pair.cardReceived.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFFA7F3D0)
                )
                Text(
                    text = "${pair.cardReceived.id} • ${pair.cardReceived.rarity}",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun TradeMatchRow(
    match: TradeMatch,
    isGiving: Boolean,
    onClick: () -> Unit
) {
    val card = match.card
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
            Text(text = energy.emoji, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = card.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "(${card.id})",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            RarityBadge(rarityStr = card.rarity)
            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isGiving) Color(0xFF059669) else Color(0xFF0284C7)
            ) {
                Text(
                    text = if (isGiving) "${match.extraCopiesAvailable} extra" else "Deseada",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

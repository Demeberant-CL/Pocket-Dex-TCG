package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CardEntity
import com.example.data.local.entity.PocketEnergyType
import com.example.data.local.entity.PocketRarity

/**
 * Tarjeta estilizada para el catálogo de cartas en formato cuadrícula (LazyVerticalGrid).
 * Diseñada siguiendo la estética visual de las cartas digitales de Pokémon TCG Pocket:
 * - Proporción estándar de tarjeta coleccionable (~0.71).
 * - Bordes con acabados holográficos/dorados según la rareza (Corona 👑, Inmersiva ☆☆☆, ex ♢♢♢♢).
 * - Indicadores visuales claros de posesión (iluminada vs silueta desaturada con candado).
 * - Contador de copias y distintivo de duplicados para intercambios ("Trade").
 */
@Composable
fun PocketCardGridItem(
    card: CardEntity,
    onClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onToggleWishlist: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false // Modo compacto para cuadrícula de 3 columnas
) {
    val isOwned = card.isOwned && card.quantity > 0
    val isDuplicate = card.quantity > 1
    val rarity = PocketRarity.fromString(card.rarity)
    val energy = PocketEnergyType.fromString(card.energyType)

    // Borde según la rareza digital de Pocket
    val borderBrush = when (rarity) {
        PocketRarity.CROWN -> Brush.sweepGradient(
            listOf(
                Color(0xFFFFD700),
                Color(0xFFFFB300),
                Color(0xFFFFF176),
                Color(0xFFFFA000),
                Color(0xFFFFD700)
            )
        )
        PocketRarity.THREE_STARS -> Brush.linearGradient(
            listOf(
                Color(0xFF8B5CF6),
                Color(0xFFEC4899),
                Color(0xFF06B6D4),
                Color(0xFF8B5CF6)
            )
        )
        PocketRarity.TWO_STARS, PocketRarity.ONE_STAR -> Brush.linearGradient(
            listOf(
                Color(0xFF38BDF8),
                Color(0xFFA855F7),
                Color(0xFFF472B6)
            )
        )
        PocketRarity.FOUR_DIAMONDS -> Brush.linearGradient(
            listOf(
                Color(0xFF00F0FF),
                Color(0xFF0284C7),
                Color(0xFF38BDF8)
            )
        )
        else -> Brush.linearGradient(
            listOf(
                Color(0xFF334155),
                Color(0xFF475569)
            )
        )
    }

    val borderWidth = when {
        !isOwned -> 0.8.dp
        rarity == PocketRarity.CROWN || rarity == PocketRarity.THREE_STARS -> 2.2.dp
        rarity == PocketRarity.FOUR_DIAMONDS -> 1.8.dp
        else -> 1.2.dp
    }

    // Fondo según tipo de energía
    val energyCardGradient = when (energy) {
        PocketEnergyType.FIRE -> listOf(Color(0xFF450A0A), Color(0xFF1E293B))
        PocketEnergyType.WATER -> listOf(Color(0xFF082F49), Color(0xFF1E293B))
        PocketEnergyType.GRASS -> listOf(Color(0xFF052E16), Color(0xFF1E293B))
        PocketEnergyType.LIGHTNING -> listOf(Color(0xFF422006), Color(0xFF1E293B))
        PocketEnergyType.PSYCHIC -> listOf(Color(0xFF3B0764), Color(0xFF1E293B))
        PocketEnergyType.FIGHTING -> listOf(Color(0xFF451A03), Color(0xFF1E293B))
        PocketEnergyType.DARKNESS -> listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
        PocketEnergyType.METAL -> listOf(Color(0xFF1E293B), Color(0xFF334155))
        PocketEnergyType.COLORLESS -> listOf(Color(0xFF1F2937), Color(0xFF111827))
        PocketEnergyType.TRAINER -> listOf(Color(0xFF042F2E), Color(0xFF134E4A))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("grid_card_${card.id}")
            .border(borderWidth, borderBrush, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOwned) Color(0xFF1E293B) else Color(0xFF0F172A).copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOwned) 4.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(energyCardGradient))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isCompact) 8.dp else 10.dp)
            ) {
                // 1. Cabecera de la carta: Energía + Rareza + Wishlist
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge de Energía
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = energy.emoji,
                            fontSize = if (isCompact) 12.sp else 14.sp
                        )
                    }

                    // Símbolo de Rareza (Diamantes / Estrellas / Corona)
                    RarityEmblem(rarity = rarity, isCompact = isCompact)

                    // Botón Wishlist
                    IconButton(
                        onClick = onToggleWishlist,
                        modifier = Modifier
                            .size(if (isCompact) 24.dp else 28.dp)
                            .testTag("grid_wishlist_${card.id}")
                    ) {
                        Icon(
                            imageVector = if (card.isWishlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito / Wishlist",
                            tint = if (card.isWishlist) Color(0xFFEF4444) else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(if (isCompact) 14.dp else 16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 2. Marco de Arte / Ilustración Pokémon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.25f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.radialGradient(
                                colors = if (isOwned) listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.Black.copy(alpha = 0.45f)
                                ) else listOf(
                                    Color.White.copy(alpha = 0.05f),
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .border(
                            width = 0.8.dp,
                            color = if (isOwned) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Si no está obtenida, atenuar visualmente
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.alpha(if (isOwned) 1f else 0.4f)
                    ) {
                        Text(
                            text = energy.emoji,
                            fontSize = if (isCompact) 32.sp else 40.sp
                        )
                        if (!isCompact && card.packName.isNotEmpty()) {
                            Text(
                                text = "Sobre ${card.packName}",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Candado si no es obtenida
                    if (!isOwned) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "No obtenida",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Badge de Duplicado para Trade (quantity > 1)
                    if (isDuplicate) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF059669))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "+${card.quantity - 1}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 3. Nombre y HP
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = if (isCompact) 13.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOwned) Color.White else Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Subtítulo con Fase y Vida
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (card.stage.isNotEmpty()) card.stage else "Básico",
                        fontSize = if (isCompact) 10.sp else 11.sp,
                        color = Color.White.copy(alpha = 0.65f),
                        maxLines = 1
                    )
                    if (card.hp > 0) {
                        Text(
                            text = "${card.hp} HP",
                            fontSize = if (isCompact) 10.sp else 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Pie de la tarjeta: ID y Controladores de Cantidad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ID Digital (ej: A1-096)
                    Text(
                        text = card.id,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.45f)
                    )

                    // Control de Copias / Badge de posesión
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .padding(horizontal = 2.dp, vertical = 2.dp)
                    ) {
                        // Botón Menos
                        Box(
                            modifier = Modifier
                                .size(if (isCompact) 20.dp else 24.dp)
                                .clip(CircleShape)
                                .background(if (card.quantity > 0) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable(enabled = card.quantity > 0) { onDecrement() }
                                .testTag("grid_decrement_${card.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Menos",
                                tint = if (card.quantity > 0) Color.White else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(if (isCompact) 10.dp else 12.dp)
                            )
                        }

                        // Cantidad
                        Text(
                            text = "${card.quantity}",
                            fontSize = if (isCompact) 11.sp else 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isOwned) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .testTag("grid_quantity_${card.id}")
                        )

                        // Botón Más
                        Box(
                            modifier = Modifier
                                .size(if (isCompact) 20.dp else 24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { onIncrement() }
                                .testTag("grid_increment_${card.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Más",
                                tint = Color.White,
                                modifier = Modifier.size(if (isCompact) 10.dp else 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Insignia visual para la rareza digital de Pokémon TCG Pocket en la tarjeta.
 */
@Composable
private fun RarityEmblem(rarity: PocketRarity, isCompact: Boolean) {
    val isCrown = rarity == PocketRarity.CROWN
    val isImmersive = rarity == PocketRarity.THREE_STARS
    val isEx = rarity == PocketRarity.FOUR_DIAMONDS

    val color = when {
        isCrown -> Color(0xFFFFD700)
        isImmersive -> Color(0xFFEC4899)
        isEx -> Color(0xFF00F0FF)
        rarity == PocketRarity.TWO_STARS || rarity == PocketRarity.ONE_STAR -> Color(0xFF38BDF8)
        else -> Color.White.copy(alpha = 0.85f)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = rarity.symbol,
            fontSize = if (isCompact) 11.sp else 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            letterSpacing = 1.sp
        )
    }
}

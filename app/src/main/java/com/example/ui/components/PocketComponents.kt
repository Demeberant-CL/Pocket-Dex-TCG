package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PocketEnergyType
import com.example.data.local.entity.PocketRarity

/**
 * Insignia visual para los tipos de energía del juego Pokémon TCG Pocket.
 */
@Composable
fun EnergyBadge(energyType: String, modifier: Modifier = Modifier) {
    val energy = PocketEnergyType.fromString(energyType)
    val (bgColor, textColor) = when (energy) {
        PocketEnergyType.GRASS -> Color(0xFF15803D) to Color.White
        PocketEnergyType.FIRE -> Color(0xFFDC2626) to Color.White
        PocketEnergyType.WATER -> Color(0xFF0284C7) to Color.White
        PocketEnergyType.LIGHTNING -> Color(0xFFD97706) to Color.Black
        PocketEnergyType.PSYCHIC -> Color(0xFF7C3AED) to Color.White
        PocketEnergyType.FIGHTING -> Color(0xFFB45309) to Color.White
        PocketEnergyType.DARKNESS -> Color(0xFF312E81) to Color.White
        PocketEnergyType.METAL -> Color(0xFF475569) to Color.White
        PocketEnergyType.COLORLESS -> Color(0xFF6B7280) to Color.White
        PocketEnergyType.TRAINER -> Color(0xFF0D9488) to Color.White
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor.copy(alpha = 0.85f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = energy.emoji,
            fontSize = 12.sp,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = energy.displayName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

/**
 * Insignia para las rarezas oficiales de Pokémon TCG Pocket (Diamantes, Estrellas, Corona y Promo).
 */
@Composable
fun RarityBadge(rarityStr: String, modifier: Modifier = Modifier) {
    val rarity = PocketRarity.fromString(rarityStr)
    val isCrown = rarity == PocketRarity.CROWN
    val isImmersive = rarity == PocketRarity.THREE_STARS
    val isEx = rarity == PocketRarity.FOUR_DIAMONDS

    val gradient = when {
        isCrown -> Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFE066)))
        isImmersive -> Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF3B82F6)))
        isEx -> Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF06B6D4)))
        else -> Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF475569)))
    }

    val textColor = when {
        isCrown -> Color(0xFF451A03)
        else -> Color.White
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(gradient)
            .border(
                width = if (isCrown || isImmersive) 1.dp else 0.5.dp,
                color = if (isCrown) Color(0xFFFFE57F) else Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "${rarity.symbol} ${rarity.displayName.substringBefore(" (")}",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}

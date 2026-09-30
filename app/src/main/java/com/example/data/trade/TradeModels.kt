package com.example.data.trade

import com.example.data.local.entity.CardEntity
import com.example.data.local.entity.PocketRarity

/**
 * Representa una carta coincidente para un intercambio de Pokémon TCG Pocket.
 */
data class TradeMatch(
    val card: CardEntity,
    val extraCopiesAvailable: Int, // copias extras disponibles para entregar (quantity - 1)
    val rarity: PocketRarity,
    val tierLevel: Int
)

/**
 * Representa un intercambio 1:1 equitativo y balanceado según el sistema de rarezas
 * oficial de Pokémon TCG Pocket (mismo nivel de diamantes, estrellas o corona).
 */
data class BalancedTradePair(
    val cardGiven: CardEntity, // Tu carta duplicada que tu amigo quiere
    val cardReceived: CardEntity, // La carta duplicada de tu amigo que tú tienes en tu Wishlist
    val tierLevel: Int,
    val tierName: String
)

/**
 * Datos de la colección o perfil compartidos de un amigo.
 */
data class FriendTradeProfile(
    val friendName: String = "Amigo Entrenador",
    val wishlistCardIds: Set<String> = emptySet(),
    val availableDuplicates: List<CardEntity> = emptyList()
)

/**
 * Resultado completo del análisis comparativo entre la colección del usuario y su amigo.
 */
data class TradeComparisonResult(
    val friendName: String,
    val matchesToGive: List<TradeMatch>, // Cartas que TÚ puedes darle a tu amigo (coincidencia: tus duplicados vs su wishlist)
    val matchesToReceive: List<TradeMatch>, // Cartas que TU AMIGO puede darte (coincidencia: sus duplicados vs tu wishlist)
    val optimalFairTrades: List<BalancedTradePair>, // Intercambios perfectos 1:1 de idéntico valor de rareza
    val totalPossibleTrades: Int
) {
    val hasTradesAvailable: Boolean
        get() = matchesToGive.isNotEmpty() || matchesToReceive.isNotEmpty()
}

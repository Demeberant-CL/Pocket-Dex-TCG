package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad principal para la persistencia de cartas de Pokémon TCG Pocket.
 * Diseñada específicamente para el formato digital:
 * - Mazos de 20 cartas
 * - Rarezas nativas de Pocket (1 a 4 Diamantes, 1 a 3 Estrellas, Corona y Promo)
 * - Energías del juego
 * - Control de posesión (isOwned) y stock de copias (quantity) para intercambios y construcción de mazos.
 */
@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey
    val id: String, // Identificador único digital, ej: "A1-001", "A1-096", "PROMO-001"
    val name: String, // Nombre del Pokémon o Entrenador, ej: "Charizard ex"
    val expansion: String, // Nombre de la expansión digital: "Genetic Apex", "Mythical Island", "Space-Time Smackdown", etc.
    val rarity: String, // Rareza de Pocket: "ONE_DIAMOND", "TWO_DIAMONDS", "THREE_DIAMONDS", "FOUR_DIAMONDS", "ONE_STAR", "TWO_STARS", "THREE_STARS", "CROWN", "PROMO"
    val energyType: String, // Tipo de energía: "Grass", "Fire", "Water", "Lightning", "Psychic", "Fighting", "Darkness", "Metal", "Colorless", "Trainer"
    val isOwned: Boolean = false, // Indica si el jugador posee al menos una copia de la carta
    val quantity: Int = 0, // Cantidad de copias en el inventario digital (2+ = duplicados para trade)
    val packName: String = "", // Sobre de apertura temático: "Charizard", "Mewtwo", "Pikachu", "Mew", etc.
    val stage: String = "Basic", // Etapa evolutiva: "Basic", "Stage 1", "Stage 2", "Item", "Supporter"
    val hp: Int = 0, // Puntos de vida (HP) en caso de Pokémon (0 para Entrenadores)
    val cardNumber: Int = 0, // Número secuencial dentro de la expansión (ej: 96)
    val isWishlist: Boolean = false, // Marcada para la lista de deseos / intercambios futuros
    val updatedAt: Long = System.currentTimeMillis() // Marca temporal para auditoría y sincronización
)

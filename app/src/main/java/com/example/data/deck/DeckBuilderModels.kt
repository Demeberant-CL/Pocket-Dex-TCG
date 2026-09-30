package com.example.data.deck

import com.example.data.local.entity.CardEntity
import java.util.UUID

/**
 * Estrategias disponibles para el Asistente Generador de Mazos de Pokémon TCG Pocket.
 */
enum class DeckStrategy(val displayName: String, val description: String) {
    META_OPTIMAL(
        "Meta Óptimo (Tier S / A)",
        "Analiza tu inventario y ensambla el arquetipo más dominante del meta competitivo (Mewtwo/Gardevoir, Pikachu Aggro, Starmie/Articuno, etc.)."
    ),
    MONO_TYPE(
        "Monotipo Elemental",
        "Construye un mazo consistente enfocado en un único tipo de energía para evitar problemas de atasco de energía."
    ),
    AGGRO_SPEED(
        "Agresivo y Rápido (Tempo)",
        "Prioriza Pokémon Básicos de bajo coste de energía (1-2) y movilidad ágil (coste de retirada bajo) para presionar en turnos 1-3."
    ),
    STAGE2_HYPER_CARRY(
        "Evolutivo de Alto Poder (ex)",
        "Estructura cadenas evolutivas completas de Fase 2 o Pokémon ex con alta vida (150-190 HP) y daño devastador."
    )
}

/**
 * Representa una entrada de carta dentro de un mazo con su cantidad (1 o 2 copias).
 */
data class DeckCardEntry(
    val card: CardEntity,
    val count: Int, // En Pokémon TCG Pocket el límite es 2 copias por nombre
    val isOwnedInCollection: Boolean = true
)

/**
 * Análisis exhaustivo del mazo de 20 cartas generado por el motor inteligente.
 */
data class DeckAnalysis(
    val totalCards: Int, // Debe ser exactamente 20
    val pokemonCount: Int,
    val trainerCount: Int,
    val basicCount: Int,
    val evolutionCount: Int,
    val primaryEnergy: String,
    val averageHp: Int,
    val averageEnergyCost: Float,
    val synergyScore: Int, // 0 a 100
    val metaTier: String, // "Tier S", "Tier A", "Tier B", "Tier C"
    val strengths: List<String>,
    val weaknesses: List<String>,
    val missingCardsRecommendations: List<String>,
    val isDeckCompleteAndLegal: Boolean
)

/**
 * Representación completa de un mazo de Pokémon TCG Pocket.
 */
data class GeneratedDeck(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val archetype: String,
    val strategy: DeckStrategy,
    val primaryEnergy: String,
    val cards: List<DeckCardEntry>,
    val analysis: DeckAnalysis,
    val isFullyOwned: Boolean = true
) {
    val totalCardCount: Int
        get() = cards.sumOf { it.count }
}

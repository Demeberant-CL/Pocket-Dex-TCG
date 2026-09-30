package com.example.data.simulator

import com.example.data.local.entity.CardEntity
import com.example.data.local.entity.PocketRarity
import kotlin.random.Random

/**
 * Resultado de una apertura de sobre de Pokémon TCG Pocket.
 */
data class OpenedPackResult(
    val packName: String,
    val cards: List<CardEntity>, // Exactamente 5 cartas
    val isGodPack: Boolean,
    val highestRarity: PocketRarity,
    val newCardsCount: Int = 0,
    val duplicateCardsCount: Int = 0
)

/**
 * Simulador de apertura de sobres digitales de Pokémon TCG Pocket.
 * Implementa las probabilidades matemáticas oficiales del juego digital:
 * - 5 cartas por sobre.
 * - Posibilidad de "God Pack" (Sobre Raro): 0.05% de probabilidad (1 entre 2000), donde las 5 cartas son ☆ o superior.
 * - Ranuras 1 a 3: Cartas de 1 Diamante (100%).
 * - Ranura 4: 90% ♢♢, 5% ♢♢♢, 1.666% ♢♢♢♢ (ex), 2.572% ☆, 0.5% ☆☆, 0.222% ☆☆☆, 0.04% 👑.
 * - Ranura 5: 60% ♢♢, 20% ♢♢♢, 6.664% ♢♢♢♢ (ex), 10.288% ☆, 2% ☆☆, 0.888% ☆☆☆, 0.16% 👑.
 */
class PocketPackOpeningSimulator {

    companion object {
        // Tasa de Sobre Raro (God Pack): 0.05%
        const val GOD_PACK_PROBABILITY = 0.0005
    }

    /**
     * Simula la apertura de un sobre seleccionando 5 cartas según las tasas reales.
     */
    fun openPack(
        catalog: List<CardEntity>,
        selectedPack: String = "Charizard"
    ): OpenedPackResult {
        // Filtrar cartas asociadas al sobre seleccionado o cartas neutrales
        val packPool = catalog.filter {
            it.packName.equals(selectedPack, ignoreCase = true) || it.packName.isEmpty()
        }.ifEmpty { catalog }

        // Evaluar si ocurre un "God Pack" (0.05%)
        val isGodPack = Random.nextDouble() < GOD_PACK_PROBABILITY

        val openedCards = if (isGodPack) {
            // God Pack: Las 5 cartas provienen de rarezas raras (1 Estrella hasta Corona)
            generateGodPackCards(packPool)
        } else {
            // Sobre estándar de 5 cartas
            generateStandardPackCards(packPool)
        }

        val highestRarity = openedCards
            .map { PocketRarity.fromString(it.rarity) }
            .maxByOrNull { it.tierLevel } ?: PocketRarity.ONE_DIAMOND

        return OpenedPackResult(
            packName = selectedPack,
            cards = openedCards,
            isGodPack = isGodPack,
            highestRarity = highestRarity
        )
    }

    private fun generateStandardPackCards(pool: List<CardEntity>): List<CardEntity> {
        val result = mutableListOf<CardEntity>()

        // Ranuras 1, 2 y 3: Cartas comunes de 1 Diamante (100% probabilidad)
        val oneDiamondPool = pool.filter { PocketRarity.fromString(it.rarity) == PocketRarity.ONE_DIAMOND }
            .ifEmpty { pool }
        repeat(3) {
            result.add(oneDiamondPool.random())
        }

        // Ranura 4: Probabilidades oficiales del juego
        val slot4Rarity = rollSlot4Rarity()
        result.add(sampleCardByRarity(pool, slot4Rarity))

        // Ranura 5: Probabilidades oficiales de ranura estelar del juego
        val slot5Rarity = rollSlot5Rarity()
        result.add(sampleCardByRarity(pool, slot5Rarity))

        return result
    }

    private fun generateGodPackCards(pool: List<CardEntity>): List<CardEntity> {
        val rarePool = pool.filter {
            val r = PocketRarity.fromString(it.rarity)
            r.tierLevel >= PocketRarity.ONE_STAR.tierLevel
        }.ifEmpty { pool }

        val result = mutableListOf<CardEntity>()
        repeat(5) {
            result.add(rarePool.random())
        }
        return result
    }

    /**
     * Probabilidades para la Ranura 4 en Pokémon TCG Pocket:
     * - ♢♢ (2 Diamantes): 90.00%
     * - ♢♢♢ (3 Diamantes): 5.00%
     * - ♢♢♢♢ (4 Diamantes / ex): 1.666%
     * - ☆ (1 Estrella / AR): 2.572%
     * - ☆☆ (2 Estrellas / SR): 0.500%
     * - ☆☆☆ (3 Estrellas / Inmersiva): 0.222%
     * - 👑 (Corona Dorada): 0.040%
     */
    private fun rollSlot4Rarity(): PocketRarity {
        val roll = Random.nextDouble() * 100.0
        var cumulative = 0.0

        // 👑 Corona (0.040%)
        cumulative += 0.040
        if (roll < cumulative) return PocketRarity.CROWN

        // ☆☆☆ Inmersiva (0.222%)
        cumulative += 0.222
        if (roll < cumulative) return PocketRarity.THREE_STARS

        // ☆☆ 2 Estrellas (0.500%)
        cumulative += 0.500
        if (roll < cumulative) return PocketRarity.TWO_STARS

        // ☆ 1 Estrella (2.572%)
        cumulative += 2.572
        if (roll < cumulative) return PocketRarity.ONE_STAR

        // ♢♢♢♢ 4 Diamantes / ex (1.666%)
        cumulative += 1.666
        if (roll < cumulative) return PocketRarity.FOUR_DIAMONDS

        // ♢♢♢ 3 Diamantes (5.000%)
        cumulative += 5.000
        if (roll < cumulative) return PocketRarity.THREE_DIAMONDS

        // ♢♢ 2 Diamantes (90.000%)
        return PocketRarity.TWO_DIAMONDS
    }

    /**
     * Probabilidades para la Ranura 5 en Pokémon TCG Pocket:
     * - ♢♢ (2 Diamantes): 60.000%
     * - ♢♢♢ (3 Diamantes): 20.000%
     * - ♢♢♢♢ (4 Diamantes / ex): 6.664%
     * - ☆ (1 Estrella / AR): 10.288%
     * - ☆☆ (2 Estrellas / SR): 2.000%
     * - ☆☆☆ (3 Estrellas / Inmersiva): 0.888%
     * - 👑 (Corona Dorada): 0.160%
     */
    private fun rollSlot5Rarity(): PocketRarity {
        val roll = Random.nextDouble() * 100.0
        var cumulative = 0.0

        // 👑 Corona (0.160%)
        cumulative += 0.160
        if (roll < cumulative) return PocketRarity.CROWN

        // ☆☆☆ Inmersiva (0.888%)
        cumulative += 0.888
        if (roll < cumulative) return PocketRarity.THREE_STARS

        // ☆☆ 2 Estrellas (2.000%)
        cumulative += 2.000
        if (roll < cumulative) return PocketRarity.TWO_STARS

        // ☆ 1 Estrella (10.288%)
        cumulative += 10.288
        if (roll < cumulative) return PocketRarity.ONE_STAR

        // ♢♢♢♢ 4 Diamantes / ex (6.664%)
        cumulative += 6.664
        if (roll < cumulative) return PocketRarity.FOUR_DIAMONDS

        // ♢♢♢ 3 Diamantes (20.000%)
        cumulative += 20.000
        if (roll < cumulative) return PocketRarity.THREE_DIAMONDS

        // ♢♢ 2 Diamantes (60.000%)
        return PocketRarity.TWO_DIAMONDS
    }

    private fun sampleCardByRarity(pool: List<CardEntity>, targetRarity: PocketRarity): CardEntity {
        val matching = pool.filter { PocketRarity.fromString(it.rarity) == targetRarity }
        if (matching.isNotEmpty()) {
            return matching.random()
        }
        // Fallback al elemento más cercano
        return pool.minByOrNull {
            kotlin.math.abs(PocketRarity.fromString(it.rarity).tierLevel - targetRarity.tierLevel)
        } ?: pool.random()
    }
}

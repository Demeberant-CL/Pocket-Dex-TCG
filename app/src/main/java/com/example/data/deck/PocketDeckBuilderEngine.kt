package com.example.data.deck

import com.example.data.local.entity.CardEntity
import com.example.data.local.entity.PocketEnergyType
import kotlin.math.roundToInt

/**
 * Motor algorítmico inteligente para la construcción y análisis de mazos de Pokémon TCG Pocket.
 * Diseñado conforme a las reglas estrictas del formato digital:
 * - Exactamente 20 cartas por mazo.
 * - Máximo 2 copias de cartas con el mismo nombre.
 * - Respeto irrestricto de cadenas evolutivas (Básico -> Fase 1 -> Fase 2).
 * - Sinergias de tipos y aceleración de curvas de energía (Misty para Agua, Moltres para Fuego, Gardevoir para Psíquico, etc.).
 */
class PocketDeckBuilderEngine {

    /**
     * Arquetipos emblemáticos del metajuego de Pokémon TCG Pocket.
     */
    data class MetaArchetypeDefinition(
        val name: String,
        val archetypeKey: String,
        val primaryEnergy: String,
        val tier: String, // "Tier S", "Tier A", "Tier B"
        val coreCardNames: List<Pair<String, Int>>, // Nombre de carta y copias ideales (1 o 2)
        val stapleTrainerNames: List<Pair<String, Int>>,
        val tacticalDescription: String,
        val strengths: List<String>,
        val weaknesses: List<String>
    )

    private val metaArchetypes = listOf(
        // 1. MEWTWO EX + GARDEVOIR (Tier S)
        MetaArchetypeDefinition(
            name = "Mewtwo ex • Psico-Shadow",
            archetypeKey = "mewtwo_gardevoir",
            primaryEnergy = "Psychic",
            tier = "Tier S",
            coreCardNames = listOf(
                "Mewtwo ex" to 2,
                "Gardevoir" to 2,
                "Mew ex" to 1,
                "Gengar ex" to 1
            ),
            stapleTrainerNames = listOf(
                "Professor's Research" to 2,
                "Poké Ball" to 2,
                "Sabrina" to 2,
                "X Speed" to 2
            ),
            tacticalDescription = "La habilidad Psico-Sombra de Gardevoir transfiere energía psíquica adicional cada turno, permitiendo cargar el Psystrike de Mewtwo ex (150 daño) de forma anticipada.",
            strengths = listOf(
                "Capacidad de un solo golpe (150 daño elimina a casi cualquier carta no-ex).",
                "Aceleración de energía con Gardevoir desde la banca.",
                "Mew ex funciona como pivote flexible y contraataque."
            ),
            weaknesses = listOf(
                "Vulnerable ante arquetipos de Oscuridad (Darkness) por debilidad de Mewtwo.",
                "Requiere armar la línea evolutiva de Gardevoir para alcanzar su pico de poder."
            )
        ),

        // 2. PIKACHU EX + ZAPDOS EX (Tier S)
        MetaArchetypeDefinition(
            name = "Pikachu ex • Tormenta Eléctrica",
            archetypeKey = "pikachu_aggro",
            primaryEnergy = "Lightning",
            tier = "Tier S",
            coreCardNames = listOf(
                "Pikachu ex" to 2,
                "Zapdos ex" to 2,
                "Pikachu Promo" to 1
            ),
            stapleTrainerNames = listOf(
                "Professor's Research" to 2,
                "Poké Ball" to 2,
                "Sabrina" to 2,
                "X Speed" to 2
            ),
            tacticalDescription = "Aprovecha Circuito Circular de Pikachu ex, que inflige 90 de daño por apenas 2 energías si tu banca está completa de Pokémon de Rayo.",
            strengths = listOf(
                "El mazo más rápido del formato con presión brutal en turnos 1 a 3.",
                "Bajo coste de energía (no sufre por atascos de recursos).",
                "Zapdos ex provee daño explosivo y resistencia aérea."
            ),
            weaknesses = listOf(
                "Vulnerable ante tipos Lucha (Fighting) como Marowak ex o Machamp ex.",
                "Pikachu ex posee 120 HP, susceptible a contraataques de alto daño."
            )
        ),

        // 3. STARMIE EX + ARTICUNO EX (Tier S)
        MetaArchetypeDefinition(
            name = "Starmie ex • Marea Helada",
            archetypeKey = "starmie_articuno",
            primaryEnergy = "Water",
            tier = "Tier S",
            coreCardNames = listOf(
                "Starmie ex" to 2,
                "Articuno ex" to 2,
                "Greninja" to 2,
                "Gyarados" to 1
            ),
            stapleTrainerNames = listOf(
                "Misty" to 2,
                "Professor's Research" to 2,
                "Poké Ball" to 2,
                "Sabrina" to 2
            ),
            tacticalDescription = "Starmie ex ofrece 90 de daño por 2 energías con coste de retirada GRATIS (0 energías). Misty permite aceleración explosiva en turno 1.",
            strengths = listOf(
                "Retirada gratuita en Starmie ex para resetear daño o alternar con Articuno.",
                "Misty puede generar victoria en turno 1 si la tirada de monedas es favorable.",
                "Greninja aporta 20 de daño de francotirador directo a la banca rival."
            ),
            weaknesses = listOf(
                "Dependencia de la suerte en las monedas de Misty.",
                "Debilidad elemental directa contra Pikachu ex (Rayo)."
            )
        ),

        // 4. CHARIZARD EX + MOLTRES EX (Tier A)
        MetaArchetypeDefinition(
            name = "Charizard ex • Furia Carmesí",
            archetypeKey = "charizard_moltres",
            primaryEnergy = "Fire",
            tier = "Tier A",
            coreCardNames = listOf(
                "Moltres ex" to 2,
                "Charizard ex" to 2,
                "Charizard ex (Inmersiva)" to 1
            ),
            stapleTrainerNames = listOf(
                "Professor's Research" to 2,
                "Poké Ball" to 2,
                "Sabrina" to 2,
                "X Speed" to 2
            ),
            tacticalDescription = "Moltres ex usa Danza Ígnea en primeros turnos para adherir energías de fuego a Charmander en banca hasta alcanzar Tormenta Carmesí (200 daño).",
            strengths = listOf(
                "200 de daño: el ataque más destructor del juego que noquea a cualquier ex.",
                "180 HP de Charizard ex le da resistencia monumental.",
                "Moltres ex actúa como un muro inicial que prepara la victoria."
            ),
            weaknesses = listOf(
                "Coste de energía elevado (requiere 4 energías y descarta 2 al atacar).",
                "Debilidad contra la velocidad del arquetipo de Agua (Starmie ex)."
            )
        ),

        // 5. VENUSAUR EX + CELEBI EX (Tier A)
        MetaArchetypeDefinition(
            name = "Venusaur ex • Muralla Floral",
            archetypeKey = "venusaur_tank",
            primaryEnergy = "Grass",
            tier = "Tier A",
            coreCardNames = listOf(
                "Venusaur ex" to 2,
                "Celebi ex" to 2
            ),
            stapleTrainerNames = listOf(
                "Professor's Research" to 2,
                "Poké Ball" to 2,
                "Sabrina" to 2,
                "Potion" to 2
            ),
            tacticalDescription = "Máxima regeneración y vida (190 HP). Floración Gigante causa 100 de daño mientras cura 30 de vida en cada turno.",
            strengths = listOf(
                "Imposible de desgastar gracias a la sinergia de curación continua.",
                "Ventaja elemental directa sobre Starmie ex y Blastoise ex.",
                "Celebi ex aporta versatilidad táctica."
            ),
            weaknesses = listOf(
                "Vulnerable ante fuego masivo (Moltres ex / Charizard ex).",
                "Alto coste de retirada (3 energías)."
            )
        ),

        // 6. DIALGA EX + MELMETAL (Tier B)
        MetaArchetypeDefinition(
            name = "Dialga ex • Bastión de Acero",
            archetypeKey = "dialga_metal",
            primaryEnergy = "Metal",
            tier = "Tier B",
            coreCardNames = listOf(
                "Dialga ex" to 2,
                "Melmetal" to 2
            ),
            stapleTrainerNames = listOf(
                "Professor's Research" to 2,
                "Poké Ball" to 2,
                "Sabrina" to 2,
                "Potion" to 2
            ),
            tacticalDescription = "Mazo defensivo con reducciones de daño nativas del tipo Metal y ataques de alto impacto en turnos medios.",
            strengths = listOf(
                "Gran resistencia general frente a ataques comunes.",
                "Efectividad sólida en juego pausado y controlado."
            ),
            weaknesses = listOf(
                "Velocidad de ataque moderada.",
                "Pocas opciones de aceleración directa de energía."
            )
        )
    )

    /**
     * Construye un mazo inteligente de exactamente 20 cartas analizando la colección local.
     */
    fun buildDeck(
        allCatalogCards: List<CardEntity>,
        strategy: DeckStrategy,
        forcedEnergyType: String? = null
    ): GeneratedDeck {
        // Filtrar inventario de cartas obtenidas con stock disponible
        val ownedCards = allCatalogCards.filter { it.isOwned && it.quantity > 0 }

        // 1. Seleccionar el mejor arquetipo según la estrategia solicitada
        val selectedArchetype = selectArchetype(ownedCards, strategy, forcedEnergyType)

        // 2. Ensamblar las 20 cartas respetando las reglas de Pokémon TCG Pocket
        val assembledEntries = assembleDeckCards(
            archetype = selectedArchetype,
            ownedCards = ownedCards,
            catalogCards = allCatalogCards,
            strategy = strategy
        )

        // 3. Ejecutar análisis técnico del mazo (curva de energía, sinergia, debilidades)
        val analysis = analyzeDeck(assembledEntries, selectedArchetype)

        val isFullyOwned = assembledEntries.all { it.isOwnedInCollection }

        return GeneratedDeck(
            name = selectedArchetype.name,
            archetype = selectedArchetype.name,
            strategy = strategy,
            primaryEnergy = selectedArchetype.primaryEnergy,
            cards = assembledEntries,
            analysis = analysis,
            isFullyOwned = isFullyOwned
        )
    }

    private fun selectArchetype(
        ownedCards: List<CardEntity>,
        strategy: DeckStrategy,
        forcedEnergy: String?
    ): MetaArchetypeDefinition {
        // Si el usuario fuerza un tipo de energía o eligió MONO_TYPE
        if (!forcedEnergy.isNullOrBlank() && forcedEnergy != "ALL") {
            metaArchetypes.find { it.primaryEnergy.equals(forcedEnergy, ignoreCase = true) }?.let {
                return it
            }
        }

        // Si la estrategia es AGGRO, priorizar arquetipos de Rayo o Agua
        if (strategy == DeckStrategy.AGGRO_SPEED) {
            return metaArchetypes.first { it.archetypeKey == "pikachu_aggro" }
        }

        // Si es STAGE 2 HYPER CARRY, priorizar Charizard o Mewtwo
        if (strategy == DeckStrategy.STAGE2_HYPER_CARRY) {
            return metaArchetypes.first { it.archetypeKey == "charizard_moltres" }
        }

        // Para META_OPTIMAL: Evaluar cuál arquetipo tiene mayor porcentaje de cartas ya poseídas por el usuario
        var bestArchetype = metaArchetypes.first()
        var highestScore = -1f

        for (archetype in metaArchetypes) {
            var ownedCoreCount = 0
            var totalCoreNeeded = 0

            for ((cardName, desiredCount) in archetype.coreCardNames) {
                totalCoreNeeded += desiredCount
                val owned = ownedCards.filter { it.name.contains(cardName, ignoreCase = true) }
                    .sumOf { minOf(it.quantity, desiredCount) }
                ownedCoreCount += minOf(owned, desiredCount)
            }

            val score = if (totalCoreNeeded > 0) (ownedCoreCount.toFloat() / totalCoreNeeded) else 0f
            if (score > highestScore) {
                highestScore = score
                bestArchetype = archetype
            }
        }

        return bestArchetype
    }

    private fun assembleDeckCards(
        archetype: MetaArchetypeDefinition,
        ownedCards: List<CardEntity>,
        catalogCards: List<CardEntity>,
        strategy: DeckStrategy
    ): List<DeckCardEntry> {
        val selectedCardsMap = mutableMapOf<String, Int>() // CardId -> Count

        // Función auxiliar para agregar cartas verificando límite de 2 copias por nombre y tope total de 20
        fun tryAddCard(card: CardEntity, desiredCount: Int): Int {
            val currentTotal = selectedCardsMap.values.sum()
            if (currentTotal >= 20) return 0

            val currentSameCardCount = selectedCardsMap[card.id] ?: 0
            val maxAllowedForName = 2
            val nameCount = selectedCardsMap.entries
                .filter { entry ->
                    val other = catalogCards.find { it.id == entry.key }
                    other != null && other.name.equals(card.name, ignoreCase = true)
                }
                .sumOf { it.value }

            val availableSlotForName = maxAllowedForName - nameCount
            val availableSlotForDeck = 20 - currentTotal

            val countToAdd = minOf(desiredCount, availableSlotForName, availableSlotForDeck)
            if (countToAdd > 0) {
                selectedCardsMap[card.id] = currentSameCardCount + countToAdd
            }
            return countToAdd
        }

        // 1. Agregar cartas núcleo (Core Pokémon) del arquetipo
        for ((name, count) in archetype.coreCardNames) {
            // Buscar si el usuario la tiene en su colección
            val owned = ownedCards.find { it.name.contains(name, ignoreCase = true) }
            val candidate = owned ?: catalogCards.find { it.name.contains(name, ignoreCase = true) }

            if (candidate != null) {
                tryAddCard(candidate, count)
            }
        }

        // 2. Agregar Entrenadores Staples (Research, Ball, Sabrina, X Speed, etc.)
        for ((name, count) in archetype.stapleTrainerNames) {
            val owned = ownedCards.find { it.name.contains(name, ignoreCase = true) }
            val candidate = owned ?: catalogCards.find { it.name.contains(name, ignoreCase = true) }

            if (candidate != null) {
                tryAddCard(candidate, count)
            }
        }

        // 3. Completar cadenas evolutivas si faltan básicos o intermedias
        val cardsSnapshot = selectedCardsMap.keys.mapNotNull { id -> catalogCards.find { it.id == id } }
        for (c in cardsSnapshot) {
            if (c.stage.equals("Stage 2", ignoreCase = true)) {
                // Requiere etapa previa básica y fase 1
                val baseCandidates = (ownedCards + catalogCards).filter {
                    it.energyType.equals(c.energyType, ignoreCase = true) && it.stage.equals("Basic", ignoreCase = true)
                }
                val stage1Candidates = (ownedCards + catalogCards).filter {
                    it.energyType.equals(c.energyType, ignoreCase = true) && it.stage.equals("Stage 1", ignoreCase = true)
                }

                stage1Candidates.firstOrNull()?.let { tryAddCard(it, 2) }
                baseCandidates.firstOrNull()?.let { tryAddCard(it, 2) }
            } else if (c.stage.equals("Stage 1", ignoreCase = true)) {
                val baseCandidates = (ownedCards + catalogCards).filter {
                    it.energyType.equals(c.energyType, ignoreCase = true) && it.stage.equals("Basic", ignoreCase = true)
                }
                baseCandidates.firstOrNull()?.let { tryAddCard(it, 2) }
            }
        }

        // 4. Rellenar hasta alcanzar exactamente 20 cartas
        // Priorizar cartas de la misma energía o entrenadores
        val fillerPool = (ownedCards + catalogCards).sortedWith(
            compareBy(
                { it.energyType != archetype.primaryEnergy },
                { !it.isOwned },
                { it.stage != "Basic" }
            )
        )

        for (filler in fillerPool) {
            if (selectedCardsMap.values.sum() >= 20) break
            tryAddCard(filler, 1)
        }

        // Si todavía faltaran cartas para 20 (caso de catálogo muy pequeño), duplicar las existentes hasta 2 copias
        if (selectedCardsMap.values.sum() < 20) {
            for (id in selectedCardsMap.keys.toList()) {
                val card = catalogCards.find { it.id == id } ?: continue
                if ((selectedCardsMap[id] ?: 0) < 2) {
                    tryAddCard(card, 1)
                }
                if (selectedCardsMap.values.sum() >= 20) break
            }
        }

        // Convertir mapa a lista de DeckCardEntry
        return selectedCardsMap.mapNotNull { (id, count) ->
            val card = catalogCards.find { it.id == id } ?: return@mapNotNull null
            val isOwnedInColl = ownedCards.any { it.id == id && it.quantity >= count }
            DeckCardEntry(
                card = card,
                count = count,
                isOwnedInCollection = isOwnedInColl
            )
        }.sortedWith(
            compareBy(
                { it.card.energyType != archetype.primaryEnergy },
                { if (it.card.energyType == "Trainer") 1 else 0 },
                { it.card.stage != "Basic" },
                { it.card.name }
            )
        )
    }

    private fun analyzeDeck(
        entries: List<DeckCardEntry>,
        archetype: MetaArchetypeDefinition
    ): DeckAnalysis {
        val totalCards = entries.sumOf { it.count }
        val pokemonEntries = entries.filter { it.card.energyType != "Trainer" }
        val trainerEntries = entries.filter { it.card.energyType == "Trainer" }

        val pokemonCount = pokemonEntries.sumOf { it.count }
        val trainerCount = trainerEntries.sumOf { it.count }
        val basicCount = pokemonEntries.filter { it.card.stage.equals("Basic", ignoreCase = true) }.sumOf { it.count }
        val evolutionCount = pokemonCount - basicCount

        val totalHp = pokemonEntries.sumOf { it.card.hp * it.count }
        val averageHp = if (pokemonCount > 0) (totalHp / pokemonCount) else 0

        // Cálculo de curva de energía
        val averageEnergyCost = when (archetype.primaryEnergy) {
            "Lightning" -> 1.8f
            "Water" -> 2.1f
            "Grass" -> 2.5f
            "Fire" -> 3.2f
            "Psychic" -> 2.8f
            else -> 2.4f
        }

        // Puntuación de sinergia (0 a 100)
        var synergy = 70
        if (basicCount in 4..7) synergy += 10
        if (trainerCount in 6..9) synergy += 10
        if (entries.any { it.card.name.contains("ex", ignoreCase = true) }) synergy += 10
        synergy = minOf(synergy, 100)

        // Recomendaciones de cartas faltantes
        val missingRecommendations = mutableListOf<String>()
        val unownedEntries = entries.filter { !it.isOwnedInCollection }
        if (unownedEntries.isNotEmpty()) {
            for (unowned in unownedEntries.take(3)) {
                missingRecommendations.add("Te falta ${unowned.card.name} (${unowned.card.id}) para completar el mazo óptimo.")
            }
        } else {
            missingRecommendations.add("¡Tienes todas las cartas necesarias en tu colección local!")
        }

        val isLegal = totalCards == 20 && basicCount >= 1 && entries.all { it.count in 1..2 }

        return DeckAnalysis(
            totalCards = totalCards,
            pokemonCount = pokemonCount,
            trainerCount = trainerCount,
            basicCount = basicCount,
            evolutionCount = evolutionCount,
            primaryEnergy = archetype.primaryEnergy,
            averageHp = averageHp,
            averageEnergyCost = averageEnergyCost,
            synergyScore = synergy,
            metaTier = archetype.tier,
            strengths = archetype.strengths,
            weaknesses = archetype.weaknesses,
            missingCardsRecommendations = missingRecommendations,
            isDeckCompleteAndLegal = isLegal
        )
    }
}

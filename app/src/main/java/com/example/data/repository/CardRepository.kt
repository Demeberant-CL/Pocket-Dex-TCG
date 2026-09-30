package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.deck.DeckStrategy
import com.example.data.deck.GeneratedDeck
import com.example.data.deck.PocketDeckBuilderEngine
import com.example.data.local.dao.CardDao
import com.example.data.local.dao.DeckDao
import com.example.data.local.entity.CardEntity
import com.example.data.local.entity.DeckEntity
import com.example.data.local.entity.PocketEnergyType
import com.example.data.local.entity.PocketRarity
import com.example.data.sample.InitialPocketData
import com.example.data.trade.FriendTradeProfile
import com.example.data.trade.TradeComparisonResult
import com.example.data.trade.TradeComparisonService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Estrategias de actualización para la importación inteligente de cartas (Upsert).
 */
enum class UpsertStrategy {
    /**
     * Fusión inteligente: Si la carta ya existe, actualiza los metadatos pero conserva y combina
     * el progreso del usuario (cantidad máxima de copias, estado obtenido y lista de deseos).
     */
    SMART_MERGE,

    /**
     * Sobrescribe completamente el registro existente con los datos exactos del CSV.
     */
    OVERWRITE_EXISTING,

    /**
     * Actualiza la información técnica de la carta (expansión, rareza, HP, energía)
     * manteniendo intactos el estado de posesión y la cantidad de copias del jugador local.
     */
    PRESERVE_LOCAL_OWNERSHIP
}

/**
 * Resumen detallado del resultado de la importación CSV.
 */
data class CsvImportSummary(
    val totalRowsProcessed: Int,
    val insertedCount: Int,
    val updatedCount: Int,
    val skippedCount: Int,
    val errors: List<String>
)

/**
 * Repositorio de cartas para Pokémon TCG Pocket (Arquitectura MVVM / Clean Architecture).
 * Abstrae el DAO y centraliza la lógica de negocio, cálculos de colección y el parser CSV.
 */
class CardRepository(
    private val cardDao: CardDao,
    private val context: Context,
    private val deckDao: DeckDao? = null
) {
    private val deckBuilderEngine = PocketDeckBuilderEngine()
    private val tradeComparisonService = TradeComparisonService(cardDao)

    // Streams reactivos con Room
    val allCards: Flow<List<CardEntity>> = cardDao.getAllCards()
    val ownedCards: Flow<List<CardEntity>> = cardDao.getOwnedCards()
    val duplicateCards: Flow<List<CardEntity>> = cardDao.getDuplicateCards()
    val wishlistCards: Flow<List<CardEntity>> = cardDao.getWishlistCards()
    val expansions: Flow<List<String>> = cardDao.getExpansions()
    val totalCount: Flow<Int> = cardDao.getTotalCount()
    val ownedCount: Flow<Int> = cardDao.getOwnedCount()
    val totalCopiesCount: Flow<Int> = cardDao.getTotalCopiesCount()
    val savedDecks: Flow<List<DeckEntity>> = deckDao?.getAllDecks() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    /**
     * Compara reactivamente con Flow la lista de duplicados del usuario (quantity > 1)
     * contra la wishlist de un amigo para encontrar intercambios óptimos.
     */
    fun observeTradeComparison(friendProfileFlow: Flow<FriendTradeProfile>): Flow<TradeComparisonResult> {
        return tradeComparisonService.observeTradeComparison(friendProfileFlow)
    }

    suspend fun compareTradesDirect(
        friendWishlistIds: Set<String>,
        friendName: String = "Amigo",
        friendDuplicates: List<CardEntity> = emptyList()
    ): TradeComparisonResult {
        return tradeComparisonService.compareTradesDirect(friendWishlistIds, friendName, friendDuplicates)
    }

    fun parseWishlistString(rawText: String): Set<String> {
        return tradeComparisonService.parseWishlistString(rawText)
    }

    /**
     * Construye un mazo inteligente de exactamente 20 cartas analizando las cartas obtenidas y sinergias.
     */
    fun generateBalancedDeck(
        allCards: List<CardEntity>,
        strategy: DeckStrategy,
        forcedEnergy: String? = null
    ): GeneratedDeck {
        return deckBuilderEngine.buildDeck(allCards, strategy, forcedEnergy)
    }

    suspend fun saveDeck(deck: GeneratedDeck) = withContext(Dispatchers.IO) {
        val pairsSerialized = deck.cards.joinToString(",") { "${it.card.id}:${it.count}" }
        val entity = DeckEntity(
            id = deck.id,
            name = deck.name,
            archetype = deck.archetype,
            strategy = deck.strategy.name,
            primaryEnergy = deck.primaryEnergy,
            cardPairsSerialized = pairsSerialized,
            synergyScore = deck.analysis.synergyScore,
            metaTier = deck.analysis.metaTier,
            createdAt = System.currentTimeMillis()
        )
        deckDao?.insertDeck(entity)
    }

    suspend fun deleteDeck(deckId: String) = withContext(Dispatchers.IO) {
        deckDao?.deleteDeckById(deckId)
    }

    fun getCardsByExpansion(expansion: String): Flow<List<CardEntity>> =
        cardDao.getCardsByExpansion(expansion)

    fun getCardById(id: String): Flow<CardEntity?> =
        cardDao.getCardById(id)

    suspend fun upsertCard(card: CardEntity) = withContext(Dispatchers.IO) {
        cardDao.upsertCard(card)
    }

    suspend fun incrementQuantity(id: String) = withContext(Dispatchers.IO) {
        cardDao.incrementQuantity(id)
    }

    suspend fun decrementQuantity(id: String) = withContext(Dispatchers.IO) {
        cardDao.decrementQuantity(id)
    }

    suspend fun setOwnership(id: String, isOwned: Boolean, quantity: Int) = withContext(Dispatchers.IO) {
        cardDao.updateOwnership(id, isOwned, quantity)
    }

    suspend fun toggleWishlist(card: CardEntity) = withContext(Dispatchers.IO) {
        cardDao.updateWishlist(card.id, !card.isWishlist)
    }

    suspend fun resetToInitialData() = withContext(Dispatchers.IO) {
        cardDao.clearAllCards()
        cardDao.upsertCards(InitialPocketData.getInitialCards())
    }

    /**
     * Parsea e importa un archivo CSV accesible a través de un Uri (Storage Access Framework / SAF).
     * Aplica la estrategia de actualización inteligente (Upsert) seleccionada.
     *
     * Soporta delimitadores por coma (`,`) o punto y coma (`;`).
     * Columnas aceptadas en el encabezado:
     * id, nombre/name, expansion, rareza/rarity, energia/energyType, obtenida/isOwned, cantidad/quantity, sobre/pack, hp, stage, numero/cardNumber, wishlist
     */
    suspend fun importCardsFromCsv(
        uri: Uri,
        strategy: UpsertStrategy = UpsertStrategy.SMART_MERGE
    ): Result<CsvImportSummary> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("No fue posible abrir el archivo seleccionado en el almacenamiento."))

            val content = inputStream.use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
            }

            parseAndUpsertCsvContent(content, strategy)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parsea y ejecuta la estrategia de Upsert sobre una cadena de texto en formato CSV.
     * Permite probar importaciones directamente o desde fuentes locales / remotas.
     */
    suspend fun parseAndUpsertCsvContent(
        csvText: String,
        strategy: UpsertStrategy = UpsertStrategy.SMART_MERGE
    ): Result<CsvImportSummary> = withContext(Dispatchers.IO) {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("El archivo CSV está completamente vacío."))
        }

        // Detectar delimitador inspeccionando la primera línea
        val headerLine = lines.first()
        val delimiter = when {
            headerLine.count { it == ';' } > headerLine.count { it == ',' } -> ";"
            headerLine.count { it == '\t' } > headerLine.count { it == ',' } -> "\t"
            else -> ","
        }

        val rawHeaders = splitCsvLine(headerLine, delimiter)
        val headerMap = rawHeaders.mapIndexed { index, name ->
            normalizeHeader(name) to index
        }.toMap()

        // Validar presencia de columnas esenciales mínimas
        val idIdx = headerMap["id"] ?: headerMap["card_id"] ?: headerMap["codigo"]
        val nameIdx = headerMap["name"] ?: headerMap["nombre"]

        if (idIdx == null || nameIdx == null) {
            return@withContext Result.failure(
                IllegalArgumentException("El CSV debe contener al menos las columnas 'id' y 'nombre' (o 'name'). Encontradas: $rawHeaders")
            )
        }

        val expansionIdx = headerMap["expansion"] ?: headerMap["set"] ?: headerMap["coleccion"]
        val rarityIdx = headerMap["rarity"] ?: headerMap["rareza"]
        val energyIdx = headerMap["energytype"] ?: headerMap["energy"] ?: headerMap["energia"] ?: headerMap["tipo"]
        val isOwnedIdx = headerMap["isowned"] ?: headerMap["obtenida"] ?: headerMap["owned"] ?: headerMap["poseida"]
        val quantityIdx = headerMap["quantity"] ?: headerMap["cantidad"] ?: headerMap["copias"]
        val packIdx = headerMap["packname"] ?: headerMap["pack"] ?: headerMap["sobre"]
        val stageIdx = headerMap["stage"] ?: headerMap["fase"] ?: headerMap["etapa"]
        val hpIdx = headerMap["hp"] ?: headerMap["ps"] ?: headerMap["vida"]
        val cardNumberIdx = headerMap["cardnumber"] ?: headerMap["numero"] ?: headerMap["num"]
        val wishlistIdx = headerMap["iswishlist"] ?: headerMap["wishlist"] ?: headerMap["deseada"]

        var inserted = 0
        var updated = 0
        var skipped = 0
        val errors = mutableListOf<String>()
        val cardsToUpsert = mutableListOf<CardEntity>()

        // Procesar filas de datos (omitir cabecera)
        for (lineIndex in 1 until lines.size) {
            val line = lines[lineIndex]
            if (line.isBlank() || line.startsWith("#")) {
                skipped++
                continue
            }

            val tokens = splitCsvLine(line, delimiter)
            val id = tokens.getOrNull(idIdx)?.trim().orEmpty()
            val name = tokens.getOrNull(nameIdx)?.trim().orEmpty()

            if (id.isEmpty() || name.isEmpty()) {
                errors.add("Línea ${lineIndex + 1}: ID o Nombre vacío. Fila omitida.")
                skipped++
                continue
            }

            try {
                val expansion = expansionIdx?.let { tokens.getOrNull(it)?.trim() }?.ifEmpty { null } ?: "Genetic Apex"
                val rawRarity = rarityIdx?.let { tokens.getOrNull(it)?.trim() } ?: "ONE_DIAMOND"
                val rarity = PocketRarity.fromString(rawRarity).code
                val rawEnergy = energyIdx?.let { tokens.getOrNull(it)?.trim() } ?: "Colorless"
                val energyType = PocketEnergyType.fromString(rawEnergy).code

                val parsedQuantity = quantityIdx?.let { tokens.getOrNull(it)?.trim()?.toIntOrNull() } ?: 0
                val parsedIsOwned = isOwnedIdx?.let {
                    val raw = tokens.getOrNull(it)?.trim()?.lowercase() ?: ""
                    raw == "true" || raw == "1" || raw == "si" || raw == "yes" || raw == "s"
                } ?: (parsedQuantity > 0)

                val packName = packIdx?.let { tokens.getOrNull(it)?.trim() }.orEmpty()
                val stage = stageIdx?.let { tokens.getOrNull(it)?.trim() }?.ifEmpty { "Basic" } ?: "Basic"
                val hp = hpIdx?.let { tokens.getOrNull(it)?.trim()?.toIntOrNull() } ?: 0
                val cardNumber = cardNumberIdx?.let { tokens.getOrNull(it)?.trim()?.toIntOrNull() } ?: 0
                val isWishlist = wishlistIdx?.let {
                    val raw = tokens.getOrNull(it)?.trim()?.lowercase() ?: ""
                    raw == "true" || raw == "1" || raw == "si" || raw == "yes"
                } ?: false

                val incomingCard = CardEntity(
                    id = id,
                    name = name,
                    expansion = expansion,
                    rarity = rarity,
                    energyType = energyType,
                    isOwned = parsedIsOwned,
                    quantity = if (parsedIsOwned && parsedQuantity == 0) 1 else parsedQuantity,
                    packName = packName,
                    stage = stage,
                    hp = hp,
                    cardNumber = cardNumber,
                    isWishlist = isWishlist,
                    updatedAt = System.currentTimeMillis()
                )

                // Estrategia de Upsert Inteligente
                val existingCard = cardDao.findCardById(id)
                if (existingCard != null) {
                    val finalCard = when (strategy) {
                        UpsertStrategy.SMART_MERGE -> {
                            // Fusión: Preservar cantidad máxima y estado owned/wishlist existente
                            val finalQuantity = maxOf(existingCard.quantity, incomingCard.quantity)
                            val finalOwned = existingCard.isOwned || incomingCard.isOwned || finalQuantity > 0
                            val finalWishlist = existingCard.isWishlist || incomingCard.isWishlist
                            incomingCard.copy(
                                isOwned = finalOwned,
                                quantity = finalQuantity,
                                isWishlist = finalWishlist,
                                packName = incomingCard.packName.ifEmpty { existingCard.packName },
                                stage = incomingCard.stage.ifEmpty { existingCard.stage },
                                hp = if (incomingCard.hp > 0) incomingCard.hp else existingCard.hp,
                                cardNumber = if (incomingCard.cardNumber > 0) incomingCard.cardNumber else existingCard.cardNumber
                            )
                        }
                        UpsertStrategy.PRESERVE_LOCAL_OWNERSHIP -> {
                            // Actualizar catálogo pero conservar el inventario del usuario tal cual
                            incomingCard.copy(
                                isOwned = existingCard.isOwned,
                                quantity = existingCard.quantity,
                                isWishlist = existingCard.isWishlist
                            )
                        }
                        UpsertStrategy.OVERWRITE_EXISTING -> {
                            incomingCard
                        }
                    }
                    cardsToUpsert.add(finalCard)
                    updated++
                } else {
                    cardsToUpsert.add(incomingCard)
                    inserted++
                }
            } catch (e: Exception) {
                errors.add("Línea ${lineIndex + 1} ($id): Error al procesar datos - ${e.localizedMessage}")
                skipped++
            }
        }

        if (cardsToUpsert.isNotEmpty()) {
            cardDao.upsertCards(cardsToUpsert)
        }

        Result.success(
            CsvImportSummary(
                totalRowsProcessed = lines.size - 1,
                insertedCount = inserted,
                updatedCount = updated,
                skippedCount = skipped,
                errors = errors
            )
        )
    }

    /**
     * Genera una cadena CSV con el formato estándar recomendado para Pokémon TCG Pocket.
     * Útil para exportación, plantillas o pruebas locales del simulador.
     */
    fun generateSampleCsvContent(): String {
        return buildString {
            appendLine("id,name,expansion,rarity,energyType,isOwned,quantity,packName,stage,hp,cardNumber,isWishlist")
            appendLine("A1-001,Bulbasaur,Genetic Apex,ONE_DIAMOND,Grass,true,2,Mewtwo,Basic,70,1,false")
            appendLine("A1-002,Ivysaur,Genetic Apex,TWO_DIAMONDS,Grass,true,1,Mewtwo,Stage 1,90,2,false")
            appendLine("A1-003,Venusaur,Genetic Apex,THREE_DIAMONDS,Grass,false,0,Mewtwo,Stage 2,160,3,true")
            appendLine("A1-004,Venusaur ex,Genetic Apex,FOUR_DIAMONDS,Grass,true,1,Mewtwo,Stage 2,190,4,false")
            appendLine("A1-035,Charmander,Genetic Apex,ONE_DIAMOND,Fire,true,3,Charizard,Basic,60,35,false")
            appendLine("A1-036,Charmeleon,Genetic Apex,TWO_DIAMONDS,Fire,true,2,Charizard,Stage 1,90,36,false")
            appendLine("A1-096,Charizard ex,Genetic Apex,FOUR_DIAMONDS,Fire,true,2,Charizard,Stage 2,180,96,false")
            appendLine("A1-280,Charizard ex (Inmersiva),Genetic Apex,THREE_STARS,Fire,false,0,Charizard,Stage 2,180,280,true")
            appendLine("A1-284,Charizard ex (Corona),Genetic Apex,CROWN,Fire,false,0,Charizard,Stage 2,180,284,true")
            appendLine("A1a-023,Mew ex,Mythical Island,FOUR_DIAMONDS,Psychic,true,1,Mew,Basic,130,23,false")
            appendLine("A2-054,Dialga ex,Space-Time Smackdown,FOUR_DIAMONDS,Metal,true,1,Dialga,Basic,150,54,false")
        }
    }

    /**
     * Exporta toda la colección actual a formato CSV listo para compartir o respaldar.
     */
    suspend fun exportCurrentCollectionCsv(cards: List<CardEntity>): String = withContext(Dispatchers.Default) {
        buildString {
            appendLine("id,name,expansion,rarity,energyType,isOwned,quantity,packName,stage,hp,cardNumber,isWishlist")
            for (c in cards) {
                appendLine("${escapeCsv(c.id)},${escapeCsv(c.name)},${escapeCsv(c.expansion)},${c.rarity},${c.energyType},${c.isOwned},${c.quantity},${escapeCsv(c.packName)},${escapeCsv(c.stage)},${c.hp},${c.cardNumber},${c.isWishlist}")
            }
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun normalizeHeader(header: String): String {
        return header.trim().lowercase().replace("_", "").replace(" ", "").replace("-", "")
    }

    private fun splitCsvLine(line: String, delimiter: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        val charDelim = delimiter.firstOrNull() ?: ','

        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"')
                        i++ // Saltar comilla escapada
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == charDelim && !inQuotes -> {
                    result.add(sb.toString())
                    sb.clear()
                }
                else -> {
                    sb.append(c)
                }
            }
            i++
        }
        result.add(sb.toString())
        return result
    }
}

package com.example.data.trade

import com.example.data.local.dao.CardDao
import com.example.data.local.entity.CardEntity
import com.example.data.local.entity.PocketRarity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Servicio reactivo de alto rendimiento para el análisis y comparación de intercambios (Trade Zone)
 * en Pokémon TCG Pocket utilizando Corrutinas y Flow.
 *
 * Compara eficientemente:
 * 1. La lista de duplicados del usuario (quantity > 1) contra la Wishlist del amigo.
 * 2. La Wishlist del usuario contra los duplicados que el amigo ofrece.
 * 3. Calcula emparejamientos equitativos 1:1 respetando los tiers de rareza de Pokémon TCG Pocket.
 */
class TradeComparisonService(
    private val cardDao: CardDao
) {

    /**
     * Flujo reactivo (Flow) continuo que escucha cambios en la base de datos Room del usuario
     * (cuando abre sobres o modifica stock) y los compara en segundo plano con el perfil de un amigo.
     *
     * Ejecuta el procesamiento intensivo en [Dispatchers.Default] para no bloquear el hilo principal.
     */
    fun observeTradeComparison(
        friendProfileFlow: Flow<FriendTradeProfile>
    ): Flow<TradeComparisonResult> {
        return combine(
            cardDao.getDuplicateCards(), // Flow reactivo: cartas con quantity > 1
            cardDao.getWishlistCards(),  // Flow reactivo: cartas deseadas por el usuario
            friendProfileFlow            // Flow del perfil del amigo (Wishlist + Duplicados)
        ) { myDuplicates, myWishlist, friendProfile ->
            computeOptimalTrades(
                myDuplicates = myDuplicates,
                myWishlist = myWishlist,
                friendProfile = friendProfile
            )
        }.flowOn(Dispatchers.Default)
    }

    /**
     * Función suspendida puntual para comparar colecciones bajo demanda (por ejemplo al pegar
     * un código o importar un perfil de amigo).
     */
    suspend fun compareTradesDirect(
        friendWishlistIds: Set<String>,
        friendName: String = "Amigo",
        friendDuplicates: List<CardEntity> = emptyList()
    ): TradeComparisonResult = withContext(Dispatchers.Default) {
        val profile = FriendTradeProfile(
            friendName = friendName,
            wishlistCardIds = friendWishlistIds,
            availableDuplicates = friendDuplicates
        )
        // Consultar estado actual directo
        val myDuplicates = cardDao.findDuplicatesDirect()
        val myWishlist = cardDao.findWishlistDirect()

        computeOptimalTrades(
            myDuplicates = myDuplicates,
            myWishlist = myWishlist,
            friendProfile = profile
        )
    }

    /**
     * Algoritmo de comparación O(N + M) utilizando tablas Hash (HashSet / HashMap)
     * para máxima eficiencia y respuesta instantánea.
     */
    private fun computeOptimalTrades(
        myDuplicates: List<CardEntity>,
        myWishlist: List<CardEntity>,
        friendProfile: FriendTradeProfile
    ): TradeComparisonResult {
        // Normalización para búsquedas O(1) insensibles a mayúsculas
        val friendWishlistSet = HashSet<String>(friendProfile.wishlistCardIds.size)
        friendProfile.wishlistCardIds.forEach { id ->
            val clean = id.trim().uppercase()
            if (clean.isNotEmpty()) {
                friendWishlistSet.add(clean)
            }
        }

        // 1. Cartas que TÚ PUEDES DARLE A TU AMIGO:
        // Tus cartas con quantity > 1 que coinciden con su Wishlist
        val matchesToGive = mutableListOf<TradeMatch>()
        for (card in myDuplicates) {
            val normalizedId = card.id.trim().uppercase()
            // Validar que realmente sea duplicado y que el amigo la desee
            if (card.quantity > 1 && friendWishlistSet.contains(normalizedId)) {
                val rarity = PocketRarity.fromString(card.rarity)
                matchesToGive.add(
                    TradeMatch(
                        card = card,
                        extraCopiesAvailable = card.quantity - 1, // Puedes ceder hasta (quantity - 1) copias
                        rarity = rarity,
                        tierLevel = rarity.tierLevel
                    )
                )
            }
        }
        // Ordenar por mayor rareza y nombre
        matchesToGive.sortByDescending { it.tierLevel }

        // 2. Cartas que TU AMIGO PUEDE DARTE A TI:
        // Duplicados del amigo que están en TU Wishlist
        val myWishlistMap = myWishlist.associateBy { it.id.trim().uppercase() }
        val matchesToReceive = mutableListOf<TradeMatch>()

        for (friendCard in friendProfile.availableDuplicates) {
            val normalizedId = friendCard.id.trim().uppercase()
            if (myWishlistMap.containsKey(normalizedId)) {
                val rarity = PocketRarity.fromString(friendCard.rarity)
                matchesToReceive.add(
                    TradeMatch(
                        card = friendCard,
                        extraCopiesAvailable = maxOf(1, friendCard.quantity - 1),
                        rarity = rarity,
                        tierLevel = rarity.tierLevel
                    )
                )
            }
        }
        matchesToReceive.sortByDescending { it.tierLevel }

        // 3. GENERAR INTERCAMBIOS ÓPTIMOS EQUITATIVOS 1:1 (Fair Trade Matching)
        // En Pokémon TCG Pocket, los intercambios justos se realizan entre cartas del mismo tier de rareza
        // (ej: 4 Diamantes ex por 4 Diamantes ex, 1 Estrella por 1 Estrella, etc.)
        val optimalFairTrades = mutableListOf<BalancedTradePair>()

        val giveByTier = matchesToGive.groupBy { it.tierLevel }.mapValues { it.value.toMutableList() }
        val receiveByTier = matchesToReceive.groupBy { it.tierLevel }.mapValues { it.value.toMutableList() }

        // Recorrer tiers desde los más altos (Corona y Estrellas) hasta diamantes
        val allTiers = (giveByTier.keys + receiveByTier.keys).sortedDescending()

        for (tier in allTiers) {
            val givePool = giveByTier[tier] ?: mutableListOf()
            val receivePool = receiveByTier[tier] ?: mutableListOf()

            while (givePool.isNotEmpty() && receivePool.isNotEmpty()) {
                val giveMatch = givePool.removeAt(0)
                val receiveMatch = receivePool.removeAt(0)

                optimalFairTrades.add(
                    BalancedTradePair(
                        cardGiven = giveMatch.card,
                        cardReceived = receiveMatch.card,
                        tierLevel = tier,
                        tierName = giveMatch.rarity.displayName
                    )
                )
            }
        }

        return TradeComparisonResult(
            friendName = friendProfile.friendName,
            matchesToGive = matchesToGive,
            matchesToReceive = matchesToReceive,
            optimalFairTrades = optimalFairTrades,
            totalPossibleTrades = matchesToGive.size + matchesToReceive.size
        )
    }

    /**
     * Utilidad para parsear listas de IDs de Wishlist compartidas en formato texto/CSV.
     * Ejemplo: "A1-096, A1-129; A1-004\nA1-084"
     */
    fun parseWishlistString(rawText: String): Set<String> {
        return rawText.split(',', ';', '\n', ' ', '\t')
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() && it.contains("-") }
            .toSet()
    }
}

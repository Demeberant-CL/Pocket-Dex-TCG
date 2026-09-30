package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.dao.CardDao
import com.example.data.local.entity.CardEntity
import com.example.data.repository.CardRepository
import com.example.data.repository.UpsertStrategy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CardDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var cardDao: CardDao
    private lateinit var repository: CardRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        cardDao = database.cardDao()
        repository = CardRepository(cardDao, context)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testUpsertAndQueryCards() = runBlocking {
        val card = CardEntity(
            id = "A1-096",
            name = "Charizard ex",
            expansion = "Genetic Apex",
            rarity = "FOUR_DIAMONDS",
            energyType = "Fire",
            isOwned = true,
            quantity = 2
        )

        cardDao.upsertCard(card)

        val retrieved = cardDao.findCardById("A1-096")
        assertNotNull(retrieved)
        assertEquals("Charizard ex", retrieved?.name)
        assertEquals(2, retrieved?.quantity)
        assertEquals(true, retrieved?.isOwned)
    }

    @Test
    fun testSmartMergeUpsertStrategy() = runBlocking {
        // Estado previo en base de datos: el usuario ya tiene 3 copias de Charizard
        cardDao.upsertCard(
            CardEntity(
                id = "A1-096",
                name = "Charizard ex",
                expansion = "Genetic Apex",
                rarity = "FOUR_DIAMONDS",
                energyType = "Fire",
                isOwned = true,
                quantity = 3,
                isWishlist = true
            )
        )

        // CSV entrante con información actualizada (por ejemplo, HP o sobre) pero con quantity = 1
        val incomingCsv = """
            id,name,expansion,rarity,energyType,isOwned,quantity,packName,stage,hp,cardNumber,isWishlist
            A1-096,Charizard ex,Genetic Apex,FOUR_DIAMONDS,Fire,true,1,Charizard,Stage 2,180,96,false
        """.trimIndent()

        val result = repository.parseAndUpsertCsvContent(incomingCsv, UpsertStrategy.SMART_MERGE)
        assertTrue(result.isSuccess)

        val mergedCard = cardDao.findCardById("A1-096")
        assertNotNull(mergedCard)
        // SMART_MERGE debe preservar el stock mayor de 3 copias y el estado wishlist
        assertEquals(3, mergedCard?.quantity)
        assertEquals(true, mergedCard?.isOwned)
        assertEquals(true, mergedCard?.isWishlist)
        assertEquals(180, mergedCard?.hp)
        assertEquals("Charizard", mergedCard?.packName)
    }

    @Test
    fun testDeckBuilderProducesExactly20Cards() = runBlocking {
        val initialCards = com.example.data.sample.InitialPocketData.getInitialCards()
        val deck = repository.generateBalancedDeck(
            allCards = initialCards,
            strategy = com.example.data.deck.DeckStrategy.META_OPTIMAL
        )

        assertNotNull(deck)
        assertEquals(20, deck.totalCardCount)
        assertTrue(deck.analysis.basicCount >= 1)
        assertTrue(deck.cards.all { it.count in 1..2 })
    }

    @Test
    fun testTradeComparisonWithCoroutinesAndFlow() = runBlocking {
        // El usuario tiene duplicados de Moltres ex (quantity = 2) y Greninja (quantity = 3)
        cardDao.upsertCard(
            CardEntity(
                id = "A1-047",
                name = "Moltres ex",
                expansion = "Genetic Apex",
                rarity = "FOUR_DIAMONDS",
                energyType = "Fire",
                isOwned = true,
                quantity = 2 // 1 copia extra disponible para trade
            )
        )
        cardDao.upsertCard(
            CardEntity(
                id = "A1-089",
                name = "Greninja",
                expansion = "Genetic Apex",
                rarity = "THREE_DIAMONDS",
                energyType = "Water",
                isOwned = true,
                quantity = 3 // 2 copias extras disponibles para trade
            )
        )
        // Y el usuario tiene en su Wishlist a Zapdos ex (A1-103, 4 Diamantes)
        cardDao.upsertCard(
            CardEntity(
                id = "A1-103",
                name = "Zapdos ex",
                expansion = "Genetic Apex",
                rarity = "FOUR_DIAMONDS",
                energyType = "Lightning",
                isOwned = false,
                quantity = 0,
                isWishlist = true
            )
        )

        // El amigo tiene en su Wishlist a Moltres ex (A1-047) y ofrece un duplicado de Zapdos ex (A1-103)
        val friendProfile = com.example.data.trade.FriendTradeProfile(
            friendName = "Ash Ketchum",
            wishlistCardIds = setOf("A1-047", "A1-999"),
            availableDuplicates = listOf(
                CardEntity(
                    id = "A1-103",
                    name = "Zapdos ex",
                    expansion = "Genetic Apex",
                    rarity = "FOUR_DIAMONDS",
                    energyType = "Lightning",
                    isOwned = true,
                    quantity = 2
                )
            )
        )

        val flow = repository.observeTradeComparison(kotlinx.coroutines.flow.flowOf(friendProfile))
        val result = flow.first()

        // Verificaciones
        assertNotNull(result)
        assertEquals("Ash Ketchum", result.friendName)
        // 1. Debe haber encontrado a Moltres ex como coincidencia que podemos entregar
        assertEquals(1, result.matchesToGive.size)
        assertEquals("A1-047", result.matchesToGive[0].card.id)
        assertEquals(1, result.matchesToGive[0].extraCopiesAvailable)

        // 2. Debe haber encontrado a Zapdos ex como coincidencia que podemos recibir
        assertEquals(1, result.matchesToReceive.size)
        assertEquals("A1-103", result.matchesToReceive[0].card.id)

        // 3. Debe haber generado un intercambio 1:1 equitativo (ambas son 4 Diamantes / ex)
        assertEquals(1, result.optimalFairTrades.size)
        val fairTrade = result.optimalFairTrades[0]
        assertEquals("Moltres ex", fairTrade.cardGiven.name)
        assertEquals("Zapdos ex", fairTrade.cardReceived.name)
        assertEquals(fairTrade.cardGiven.rarity, fairTrade.cardReceived.rarity)
    }
}

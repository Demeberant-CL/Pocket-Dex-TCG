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
}

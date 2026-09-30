package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para la gestión reactiva de cartas en Pokémon TCG Pocket.
 * Proporciona soporte para Upsert (inserción o actualización inteligente), filtros avanzados,
 * consultas reactivas con Flow y gestión de inventario/duplicados para intercambios.
 */
@Dao
interface CardDao {

    /**
     * Inserta o actualiza una carta individual de forma inteligente.
     * Si la clave primaria (id) ya existe, actualiza todos los campos.
     */
    @Upsert
    suspend fun upsertCard(card: CardEntity)

    /**
     * Inserta o actualiza un lote completo de cartas (utilizado en la importación masiva de CSV).
     */
    @Upsert
    suspend fun upsertCards(cards: List<CardEntity>)

    /**
     * Inserción directa con estrategia de reemplazo si es requerida por casos específicos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    /**
     * Obtiene el catálogo completo de cartas ordenadas por expansión y número.
     */
    @Query("SELECT * FROM cards ORDER BY expansion ASC, cardNumber ASC, id ASC")
    fun getAllCards(): Flow<List<CardEntity>>

    /**
     * Busca una carta específica por su ID reactivamente.
     */
    @Query("SELECT * FROM cards WHERE id = :id")
    fun getCardById(id: String): Flow<CardEntity?>

    /**
     * Consulta directa síncrona/suspendida para verificar existencia previa durante importaciones.
     */
    @Query("SELECT * FROM cards WHERE id = :id LIMIT 1")
    suspend fun findCardById(id: String): CardEntity?

    /**
     * Obtiene únicamente las cartas que el usuario ya ha obtenido (isOwned = 1).
     */
    @Query("SELECT * FROM cards WHERE isOwned = 1 ORDER BY expansion ASC, cardNumber ASC")
    fun getOwnedCards(): Flow<List<CardEntity>>

    /**
     * Obtiene cartas duplicadas (quantity > 1).
     * Crítico para el Módulo 4 de Intercambios en Pokémon TCG Pocket.
     */
    @Query("SELECT * FROM cards WHERE quantity > 1 ORDER BY quantity DESC, name ASC")
    fun getDuplicateCards(): Flow<List<CardEntity>>

    /**
     * Obtiene las cartas marcadas en la lista de deseos (Wishlist).
     */
    @Query("SELECT * FROM cards WHERE isWishlist = 1 ORDER BY expansion ASC, cardNumber ASC")
    fun getWishlistCards(): Flow<List<CardEntity>>

    /**
     * Filtra cartas por expansión específica (ej: "Genetic Apex", "Mythical Island").
     */
    @Query("SELECT * FROM cards WHERE expansion = :expansion ORDER BY cardNumber ASC, id ASC")
    fun getCardsByExpansion(expansion: String): Flow<List<CardEntity>>

    /**
     * Lista de todas las expansiones únicas registradas en la base de datos.
     */
    @Query("SELECT DISTINCT expansion FROM cards ORDER BY expansion ASC")
    fun getExpansions(): Flow<List<String>>

    /**
     * Contador total de cartas registradas en la base de datos.
     */
    @Query("SELECT COUNT(*) FROM cards")
    fun getTotalCount(): Flow<Int>

    /**
     * Contador de cartas únicas que el jugador posee.
     */
    @Query("SELECT COUNT(*) FROM cards WHERE isOwned = 1")
    fun getOwnedCount(): Flow<Int>

    /**
     * Contador de copias totales (sumatoria de quantity de todas las cartas).
     */
    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cards")
    fun getTotalCopiesCount(): Flow<Int>

    /**
     * Actualiza el estado de posesión y la cantidad de copias de una carta.
     */
    @Query("UPDATE cards SET isOwned = :isOwned, quantity = :quantity, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateOwnership(
        id: String,
        isOwned: Boolean,
        quantity: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    /**
     * Incrementa en 1 la cantidad de copias y asegura que isOwned sea true.
     */
    @Query("UPDATE cards SET quantity = quantity + 1, isOwned = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun incrementQuantity(id: String, updatedAt: Long = System.currentTimeMillis())

    /**
     * Decrementa en 1 la cantidad de copias. Si llega a 0, isOwned pasa a false.
     */
    @Query("""
        UPDATE cards 
        SET quantity = CASE WHEN quantity > 1 THEN quantity - 1 ELSE 0 END,
            isOwned = CASE WHEN quantity > 1 THEN 1 ELSE 0 END,
            updatedAt = :updatedAt
        WHERE id = :id
    """)
    suspend fun decrementQuantity(id: String, updatedAt: Long = System.currentTimeMillis())

    /**
     * Cambia el estado de lista de deseos (Wishlist) de una carta.
     */
    @Query("UPDATE cards SET isWishlist = :isWishlist WHERE id = :id")
    suspend fun updateWishlist(id: String, isWishlist: Boolean)

    /**
     * Eliminación de todo el catálogo si el usuario desea reiniciar datos.
     */
    @Query("DELETE FROM cards")
    suspend fun clearAllCards()
}

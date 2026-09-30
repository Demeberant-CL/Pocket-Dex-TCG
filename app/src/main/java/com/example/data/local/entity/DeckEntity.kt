package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para almacenar mazos de 20 cartas guardados por el usuario.
 */
@Entity(tableName = "saved_decks")
data class DeckEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val archetype: String,
    val strategy: String,
    val primaryEnergy: String,
    val cardPairsSerialized: String, // Formato compacto "cardId:count,cardId:count"
    val synergyScore: Int,
    val metaTier: String,
    val createdAt: Long = System.currentTimeMillis()
)

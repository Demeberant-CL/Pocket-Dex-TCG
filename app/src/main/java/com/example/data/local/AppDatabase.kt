package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CardDao
import com.example.data.local.dao.DeckDao
import com.example.data.local.entity.CardEntity
import com.example.data.local.entity.DeckEntity
import com.example.data.sample.InitialPocketData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos Room principal para la aplicación Pokémon TCG Pocket.
 * Gestiona de forma local y persistente el inventario de cartas, colecciones,
 * duplicados, listas de deseos y mazos construidos por el jugador digital.
 */
@Database(
    entities = [CardEntity::class, DeckEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cardDao(): CardDao
    abstract fun deckDao(): DeckDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pokemon_tcg_pocket_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabasePrepopulateCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback para precargar cartas iniciales del ecosistema Pokémon TCG Pocket
     * la primera vez que se crea la base de datos.
     */
    private class DatabasePrepopulateCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dao = getDatabase(context).cardDao()
                    dao.upsertCards(InitialPocketData.getInitialCards())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

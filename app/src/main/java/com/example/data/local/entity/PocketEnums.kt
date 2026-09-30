package com.example.data.local.entity

/**
 * Rarezas oficiales del juego móvil Pokémon TCG Pocket.
 * Estructuradas en Diamantes (comunes a ultra raras), Estrellas (ilustraciones especiales e inmersivas) y Corona (doradas supremas).
 */
enum class PocketRarity(
    val code: String,
    val displayName: String,
    val symbol: String,
    val tierLevel: Int
) {
    ONE_DIAMOND("ONE_DIAMOND", "1 Diamante", "♢", 1),
    TWO_DIAMONDS("TWO_DIAMONDS", "2 Diamantes", "♢♢", 2),
    THREE_DIAMONDS("THREE_DIAMONDS", "3 Diamantes", "♢♢♢", 3),
    FOUR_DIAMONDS("FOUR_DIAMONDS", "4 Diamantes (ex)", "♢♢♢♢", 4),
    ONE_STAR("ONE_STAR", "1 Estrella (Arte Alternativo)", "☆", 5),
    TWO_STARS("TWO_STARS", "2 Estrellas (Full Art / SR)", "☆☆", 6),
    THREE_STARS("THREE_STARS", "3 Estrellas (Inmersiva)", "☆☆☆", 7),
    CROWN("CROWN", "Corona Dorada", "👑", 8),
    PROMO("PROMO", "Promocional", "PROMO", 0);

    companion object {
        fun fromString(value: String): PocketRarity {
            val normalized = value.trim().uppercase()
            return when {
                normalized.contains("CROWN") || normalized.contains("CORONA") || normalized.contains("👑") -> CROWN
                normalized.contains("THREE_STARS") || normalized.contains("3 ESTRELLAS") || normalized.contains("☆☆☆") || normalized.contains("INMERSIVA") || normalized.contains("IMMERSIVE") -> THREE_STARS
                normalized.contains("TWO_STARS") || normalized.contains("2 ESTRELLAS") || normalized.contains("☆☆") -> TWO_STARS
                normalized.contains("ONE_STAR") || normalized.contains("1 ESTRELLA") || normalized.contains("☆") -> ONE_STAR
                normalized.contains("FOUR_DIAMONDS") || normalized.contains("4 DIAMANTES") || normalized.contains("♢♢♢♢") || normalized.contains("EX") -> FOUR_DIAMONDS
                normalized.contains("THREE_DIAMONDS") || normalized.contains("3 DIAMANTES") || normalized.contains("♢♢♢") -> THREE_DIAMONDS
                normalized.contains("TWO_DIAMONDS") || normalized.contains("2 DIAMANTES") || normalized.contains("♢♢") -> TWO_DIAMONDS
                normalized.contains("ONE_DIAMOND") || normalized.contains("1 DIAMANTE") || normalized.contains("♢") -> ONE_DIAMOND
                normalized.contains("PROMO") -> PROMO
                else -> ONE_DIAMOND
            }
        }
    }
}

/**
 * Tipos de Energía presentes en Pokémon TCG Pocket y entrenadores.
 */
enum class PocketEnergyType(
    val code: String,
    val displayName: String,
    val emoji: String
) {
    GRASS("Grass", "Planta", "🌿"),
    FIRE("Fire", "Fuego", "🔥"),
    WATER("Water", "Agua", "💧"),
    LIGHTNING("Lightning", "Rayo", "⚡"),
    PSYCHIC("Psychic", "Psíquico", "🔮"),
    FIGHTING("Fighting", "Lucha", "🥊"),
    DARKNESS("Darkness", "Oscuridad", "🌑"),
    METAL("Metal", "Metal", "⚙️"),
    COLORLESS("Colorless", "Incoloro", "⚪"),
    TRAINER("Trainer", "Entrenador", "🎒");

    companion object {
        fun fromString(value: String): PocketEnergyType {
            val normalized = value.trim().lowercase()
            return when {
                normalized.contains("grass") || normalized.contains("planta") -> GRASS
                normalized.contains("fire") || normalized.contains("fuego") -> FIRE
                normalized.contains("water") || normalized.contains("agua") -> WATER
                normalized.contains("lightning") || normalized.contains("rayo") || normalized.contains("electric") -> LIGHTNING
                normalized.contains("psychic") || normalized.contains("psiquico") || normalized.contains("psíquico") -> PSYCHIC
                normalized.contains("fighting") || normalized.contains("lucha") -> FIGHTING
                normalized.contains("dark") || normalized.contains("oscuridad") || normalized.contains("siniestro") -> DARKNESS
                normalized.contains("metal") || normalized.contains("acero") -> METAL
                normalized.contains("colorless") || normalized.contains("incoloro") || normalized.contains("normal") -> COLORLESS
                normalized.contains("trainer") || normalized.contains("entrenador") || normalized.contains("supporter") || normalized.contains("item") || normalized.contains("objeto") -> TRAINER
                else -> COLORLESS
            }
        }
    }
}

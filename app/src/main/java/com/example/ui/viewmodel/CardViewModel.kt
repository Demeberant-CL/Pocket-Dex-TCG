package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.deck.DeckStrategy
import com.example.data.deck.GeneratedDeck
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CardEntity
import com.example.data.local.entity.DeckEntity
import com.example.data.local.entity.PocketRarity
import com.example.data.repository.CardRepository
import com.example.data.repository.CsvImportSummary
import com.example.data.repository.UpsertStrategy
import com.example.data.simulator.OpenedPackResult
import com.example.data.trade.FriendTradeProfile
import com.example.data.trade.TradeComparisonResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Pestañas principales de navegación en la aplicación de Pokémon TCG Pocket.
 */
enum class CollectionTab(val title: String) {
    ALL("Catálogo"),
    OWNED("En Álbum"),
    DUPLICATES("Trades"),
    WISHLIST("Deseadas"),
    DECK_BUILDER("Deck Builder IA"),
    PACK_OPENING("Sobres")
}

/**
 * Criterio de ordenamiento para el catálogo.
 */
enum class SortOption(val displayName: String) {
    NUMBER("Por Número / Set"),
    NAME("Por Nombre (A-Z)"),
    QUANTITY("Por Copias (Mayor a menor)"),
    RARITY("Por Rareza (Mayor a menor)")
}

data class CardsUiState(
    val cards: List<CardEntity> = emptyList(),
    val totalCardsInCatalog: Int = 0,
    val uniqueOwnedCount: Int = 0,
    val totalCopiesCount: Int = 0,
    val duplicateCardsCount: Int = 0,
    val wishlistCount: Int = 0,
    val searchQuery: String = "",
    val selectedExpansion: String = "ALL",
    val selectedEnergy: String = "ALL",
    val selectedRarity: String = "ALL",
    val selectedTab: CollectionTab = CollectionTab.ALL,
    val sortOption: SortOption = SortOption.NUMBER,
    val gridColumns: Int = 2,
    val selectedCardForDetail: CardEntity? = null,
    val isImporting: Boolean = false,
    val importSummary: CsvImportSummary? = null,
    val errorMessage: String? = null,
    val showImportSheet: Boolean = false,
    // Módulo 3: Deck Builder IA
    val generatedDeck: GeneratedDeck? = null,
    val savedDecks: List<DeckEntity> = emptyList(),
    val isGeneratingDeck: Boolean = false,
    val selectedDeckStrategy: DeckStrategy = DeckStrategy.META_OPTIMAL,
    val selectedDeckEnergy: String? = null,
    val deckNoticeMessage: String? = null,
    // Módulo 4: Zona de Intercambios (Trades)
    val tradeComparison: TradeComparisonResult = TradeComparisonResult(
        friendName = "Rival Gary",
        matchesToGive = emptyList(),
        matchesToReceive = emptyList(),
        optimalFairTrades = emptyList(),
        totalPossibleTrades = 0
    ),
    val friendWishlistInput: String = "A1-047, A1-096, A1-089",
    // Módulo 5: Simulador de Apertura de Sobres
    val openedPack: OpenedPackResult? = null,
    val isOpeningPack: Boolean = false
) {
    val completionPercentage: Float
        get() = if (totalCardsInCatalog > 0) (uniqueOwnedCount.toFloat() / totalCardsInCatalog) * 100f else 0f

    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() || selectedExpansion != "ALL" || selectedEnergy != "ALL" || selectedRarity != "ALL"
}

class CardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CardRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CardRepository(
            cardDao = database.cardDao(),
            context = application,
            deckDao = database.deckDao()
        )
    }

    private val _searchQuery = MutableStateFlow("")
    private val _selectedExpansion = MutableStateFlow("ALL")
    private val _selectedEnergy = MutableStateFlow("ALL")
    private val _selectedRarity = MutableStateFlow("ALL")
    private val _selectedTab = MutableStateFlow(CollectionTab.ALL)
    private val _sortOption = MutableStateFlow(SortOption.NUMBER)
    private val _gridColumns = MutableStateFlow(2)
    private val _selectedCardForDetail = MutableStateFlow<CardEntity?>(null)
    private val _isImporting = MutableStateFlow(false)
    private val _importSummary = MutableStateFlow<CsvImportSummary?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _showImportSheet = MutableStateFlow(false)

    // Estados para el Módulo 3: Deck Builder IA
    private val _generatedDeck = MutableStateFlow<GeneratedDeck?>(null)
    private val _isGeneratingDeck = MutableStateFlow(false)
    private val _selectedDeckStrategy = MutableStateFlow(DeckStrategy.META_OPTIMAL)
    private val _selectedDeckEnergy = MutableStateFlow<String?>("Psychic")
    private val _deckNoticeMessage = MutableStateFlow<String?>(null)

    // Estados para el Módulo 4: Zona de Intercambios (Trades)
    private val _friendWishlistInput = MutableStateFlow("A1-047, A1-096, A1-089")
    private val _friendTradeProfile = MutableStateFlow(
        FriendTradeProfile(
            friendName = "Rival Gary",
            wishlistCardIds = setOf("A1-047", "A1-096", "A1-089"),
            availableDuplicates = listOf(
                CardEntity("A1-103", "Zapdos ex", "Genetic Apex", "FOUR_DIAMONDS", "Lightning", isOwned = true, quantity = 2),
                CardEntity("A1-056", "Blastoise ex", "Genetic Apex", "FOUR_DIAMONDS", "Water", isOwned = true, quantity = 2)
            )
        )
    )

    // Estados para el Módulo 5: Simulador de Apertura de Sobres
    private val _openedPack = MutableStateFlow<OpenedPackResult?>(null)
    private val _isOpeningPack = MutableStateFlow(false)

    val uiState: StateFlow<CardsUiState> = combine(
        repository.allCards,
        repository.savedDecks,
        _searchQuery,
        _selectedExpansion,
        _selectedEnergy,
        _selectedRarity,
        _selectedTab,
        _sortOption,
        _gridColumns,
        _selectedCardForDetail,
        _generatedDeck,
        _selectedDeckStrategy,
        _selectedDeckEnergy,
        _deckNoticeMessage,
        repository.observeTradeComparison(_friendTradeProfile),
        _friendWishlistInput,
        _openedPack,
        _isOpeningPack
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val allCards = args[0] as List<CardEntity>
        @Suppress("UNCHECKED_CAST")
        val savedDecks = args[1] as List<DeckEntity>
        val query = args[2] as String
        val expansion = args[3] as String
        val energy = args[4] as String
        val rarity = args[5] as String
        val tab = args[6] as CollectionTab
        val sort = args[7] as SortOption
        val gridCols = args[8] as Int
        val detailCard = args[9] as CardEntity?
        val genDeck = args[10] as GeneratedDeck?
        val deckStrat = args[11] as DeckStrategy
        val deckEnergy = args[12] as String?
        val deckNotice = args[13] as String?
        val tradeResult = args[14] as TradeComparisonResult
        val friendWishlistInput = args[15] as String
        val openedPack = args[16] as OpenedPackResult?
        val isOpening = args[17] as Boolean

        val totalInCatalog = allCards.size
        val uniqueOwned = allCards.count { it.isOwned }
        val totalCopies = allCards.sumOf { it.quantity }
        val duplicatesCount = allCards.count { it.quantity > 1 }
        val wishlistCount = allCards.count { it.isWishlist }

        val updatedDetailCard = detailCard?.let { selected ->
            allCards.find { it.id == selected.id } ?: selected
        }

        // Filtrado reactivo integral
        var filtered = allCards.filter { card ->
            val matchesTab = when (tab) {
                CollectionTab.ALL -> true
                CollectionTab.OWNED -> card.isOwned
                CollectionTab.DUPLICATES -> card.quantity > 1
                CollectionTab.WISHLIST -> card.isWishlist
                CollectionTab.DECK_BUILDER -> true
                CollectionTab.PACK_OPENING -> true
            }

            val matchesQuery = query.isBlank() ||
                card.name.contains(query, ignoreCase = true) ||
                card.id.contains(query, ignoreCase = true)

            val matchesExpansion = expansion == "ALL" || card.expansion.equals(expansion, ignoreCase = true)
            val matchesEnergy = energy == "ALL" || card.energyType.equals(energy, ignoreCase = true)
            val matchesRarity = rarity == "ALL" || card.rarity.equals(rarity, ignoreCase = true)

            matchesTab && matchesQuery && matchesExpansion && matchesEnergy && matchesRarity
        }

        // Ordenamiento
        filtered = when (sort) {
            SortOption.NUMBER -> filtered.sortedWith(compareBy({ it.expansion }, { it.cardNumber }, { it.id }))
            SortOption.NAME -> filtered.sortedBy { it.name }
            SortOption.QUANTITY -> filtered.sortedByDescending { it.quantity }
            SortOption.RARITY -> filtered.sortedByDescending { PocketRarity.fromString(it.rarity).tierLevel }
        }

        CardsUiState(
            cards = filtered,
            totalCardsInCatalog = totalInCatalog,
            uniqueOwnedCount = uniqueOwned,
            totalCopiesCount = totalCopies,
            duplicateCardsCount = duplicatesCount,
            wishlistCount = wishlistCount,
            searchQuery = query,
            selectedExpansion = expansion,
            selectedEnergy = energy,
            selectedRarity = rarity,
            selectedTab = tab,
            sortOption = sort,
            gridColumns = gridCols,
            selectedCardForDetail = updatedDetailCard,
            isImporting = _isImporting.value,
            importSummary = _importSummary.value,
            errorMessage = _errorMessage.value,
            showImportSheet = _showImportSheet.value,
            generatedDeck = genDeck,
            savedDecks = savedDecks,
            isGeneratingDeck = _isGeneratingDeck.value,
            selectedDeckStrategy = deckStrat,
            selectedDeckEnergy = deckEnergy,
            deckNoticeMessage = deckNotice,
            tradeComparison = tradeResult,
            friendWishlistInput = friendWishlistInput,
            openedPack = openedPack,
            isOpeningPack = isOpening
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CardsUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onExpansionSelected(expansion: String) {
        _selectedExpansion.value = expansion
    }

    fun onEnergySelected(energy: String) {
        _selectedEnergy.value = if (_selectedEnergy.value == energy && energy != "ALL") "ALL" else energy
    }

    fun onRaritySelected(rarity: String) {
        _selectedRarity.value = if (_selectedRarity.value == rarity && rarity != "ALL") "ALL" else rarity
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedExpansion.value = "ALL"
        _selectedEnergy.value = "ALL"
        _selectedRarity.value = "ALL"
    }

    fun onTabSelected(tab: CollectionTab) {
        _selectedTab.value = tab
        // Si entra por primera vez a Deck Builder y no hay mazo generado, generar uno meta óptimo automáticamente
        if (tab == CollectionTab.DECK_BUILDER && _generatedDeck.value == null) {
            generateDeck(DeckStrategy.META_OPTIMAL)
        }
    }

    fun onSortSelected(sort: SortOption) {
        _sortOption.value = sort
    }

    fun toggleGridColumns() {
        _gridColumns.value = if (_gridColumns.value == 2) 3 else 2
    }

    fun selectCardForDetail(card: CardEntity?) {
        _selectedCardForDetail.value = card
    }

    fun setShowImportSheet(show: Boolean) {
        _showImportSheet.value = show
    }

    fun dismissSummary() {
        _importSummary.value = null
        _errorMessage.value = null
    }

    fun incrementQuantity(card: CardEntity) {
        viewModelScope.launch {
            repository.incrementQuantity(card.id)
        }
    }

    fun decrementQuantity(card: CardEntity) {
        viewModelScope.launch {
            repository.decrementQuantity(card.id)
        }
    }

    fun toggleOwned(card: CardEntity) {
        viewModelScope.launch {
            if (card.isOwned) {
                repository.setOwnership(card.id, isOwned = false, quantity = 0)
            } else {
                repository.setOwnership(card.id, isOwned = true, quantity = 1)
            }
        }
    }

    fun toggleWishlist(card: CardEntity) {
        viewModelScope.launch {
            repository.toggleWishlist(card)
        }
    }

    // --- Módulo 3: Métodos del Asistente Deck Builder IA ---

    fun onDeckStrategyChanged(strategy: DeckStrategy) {
        _selectedDeckStrategy.value = strategy
        generateDeck(strategy, _selectedDeckEnergy.value)
    }

    fun onDeckEnergyChanged(energy: String) {
        _selectedDeckEnergy.value = energy
        generateDeck(_selectedDeckStrategy.value, energy)
    }

    fun generateDeck(strategy: DeckStrategy, forcedEnergy: String? = null) {
        viewModelScope.launch {
            _isGeneratingDeck.value = true
            val catalog = uiState.value.cards.ifEmpty {
                // Si la vista filtrada está vacía, usar el catálogo completo
                com.example.data.sample.InitialPocketData.getInitialCards()
            }
            val deck = repository.generateBalancedDeck(
                allCards = catalog,
                strategy = strategy,
                forcedEnergy = if (strategy == DeckStrategy.MONO_TYPE) forcedEnergy else null
            )
            _generatedDeck.value = deck
            _isGeneratingDeck.value = false
            _deckNoticeMessage.value = "¡Mazo de 20 cartas generado exitosamente para ${deck.archetype}!"
        }
    }

    fun saveCurrentDeck() {
        val current = _generatedDeck.value ?: return
        viewModelScope.launch {
            repository.saveDeck(current)
            _deckNoticeMessage.value = "¡Mazo '${current.name}' guardado en la base de datos local!"
        }
    }

    fun deleteSavedDeck(deckId: String) {
        viewModelScope.launch {
            repository.deleteDeck(deckId)
            _deckNoticeMessage.value = "Mazo eliminado."
        }
    }

    fun clearDeckNotice() {
        _deckNoticeMessage.value = null
    }

    // --- Módulo 4: Métodos de la Zona de Intercambios (Trades) ---

    fun onFriendWishlistInputChanged(text: String) {
        _friendWishlistInput.value = text
        val parsedIds = repository.parseWishlistString(text)
        _friendTradeProfile.value = _friendTradeProfile.value.copy(wishlistCardIds = parsedIds)
    }

    fun applyFriendTradePreset(name: String, wishlistIds: Set<String>, duplicates: List<CardEntity>) {
        _friendWishlistInput.value = wishlistIds.joinToString(", ")
        _friendTradeProfile.value = FriendTradeProfile(
            friendName = name,
            wishlistCardIds = wishlistIds,
            availableDuplicates = duplicates
        )
    }

    // --- Módulo 5: Métodos de Apertura de Sobres ---

    fun openPack(packName: String) {
        viewModelScope.launch {
            _isOpeningPack.value = true
            // Breve retardo visual para simular la animación de rasgar el sobre
            kotlinx.coroutines.delay(450)
            val catalog = uiState.value.cards.ifEmpty {
                com.example.data.sample.InitialPocketData.getInitialCards()
            }
            val result = repository.simulatePackOpening(catalog, packName)
            _openedPack.value = result
            _isOpeningPack.value = false
        }
    }

    fun addOpenedPackCardsToCollection(cards: List<CardEntity>) {
        viewModelScope.launch {
            val (newCards, dupes) = repository.addOpenedCardsToCollection(cards)
            _deckNoticeMessage.value = "¡Sobre añadido al inventario! (+$newCards nuevas, +$dupes duplicadas para trade)"
        }
    }

    fun importCsvFromUri(uri: Uri, strategy: UpsertStrategy) {
        viewModelScope.launch {
            _isImporting.value = true
            _errorMessage.value = null
            _importSummary.value = null

            val result = repository.importCardsFromCsv(uri, strategy)
            result.onSuccess { summary ->
                _importSummary.value = summary
                _showImportSheet.value = false
            }.onFailure { err ->
                _errorMessage.value = err.localizedMessage ?: "Error al importar el archivo."
            }
            _isImporting.value = false
        }
    }

    fun importSampleCsv(strategy: UpsertStrategy) {
        viewModelScope.launch {
            _isImporting.value = true
            _errorMessage.value = null
            val sampleData = repository.generateSampleCsvContent()
            val result = repository.parseAndUpsertCsvContent(sampleData, strategy)
            result.onSuccess { summary ->
                _importSummary.value = summary
                _showImportSheet.value = false
            }.onFailure { err ->
                _errorMessage.value = err.localizedMessage ?: "Error al procesar plantilla de prueba."
            }
            _isImporting.value = false
        }
    }

    fun resetCatalogToDefaults() {
        viewModelScope.launch {
            repository.resetToInitialData()
        }
    }
}

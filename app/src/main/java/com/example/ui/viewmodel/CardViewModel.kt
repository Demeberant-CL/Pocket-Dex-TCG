package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CardEntity
import com.example.data.local.entity.PocketRarity
import com.example.data.repository.CardRepository
import com.example.data.repository.CsvImportSummary
import com.example.data.repository.UpsertStrategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Pestañas principales de navegación en la colección de Pokémon TCG Pocket.
 */
enum class CollectionTab(val title: String) {
    ALL("Catálogo"),
    OWNED("En Álbum"),
    DUPLICATES("Trades"),
    WISHLIST("Deseadas")
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
    val gridColumns: Int = 2, // 2 para vista detallada, 3 para vista tipo álbum
    val selectedCardForDetail: CardEntity? = null,
    val isImporting: Boolean = false,
    val importSummary: CsvImportSummary? = null,
    val errorMessage: String? = null,
    val showImportSheet: Boolean = false
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
        repository = CardRepository(database.cardDao(), application)
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

    val uiState: StateFlow<CardsUiState> = combine(
        repository.allCards,
        _searchQuery,
        _selectedExpansion,
        _selectedEnergy,
        _selectedRarity,
        _selectedTab,
        _sortOption,
        _gridColumns,
        _selectedCardForDetail,
        _isImporting,
        _importSummary,
        _errorMessage,
        _showImportSheet
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val allCards = args[0] as List<CardEntity>
        val query = args[1] as String
        val expansion = args[2] as String
        val energy = args[3] as String
        val rarity = args[4] as String
        val tab = args[5] as CollectionTab
        val sort = args[6] as SortOption
        val gridCols = args[7] as Int
        val detailCard = args[8] as CardEntity?
        val isImporting = args[9] as Boolean
        val importSummary = args[10] as CsvImportSummary?
        val errorMsg = args[11] as String?
        val showSheet = args[12] as Boolean

        val totalInCatalog = allCards.size
        val uniqueOwned = allCards.count { it.isOwned }
        val totalCopies = allCards.sumOf { it.quantity }
        val duplicatesCount = allCards.count { it.quantity > 1 }
        val wishlistCount = allCards.count { it.isWishlist }

        // Si hay una carta seleccionada para detalle, mantener sus datos actualizados
        val updatedDetailCard = detailCard?.let { selected ->
            allCards.find { it.id == selected.id } ?: selected
        }

        // Filtrado reactivo integral
        var filtered = allCards.filter { card ->
            // Filtro por Tab
            val matchesTab = when (tab) {
                CollectionTab.ALL -> true
                CollectionTab.OWNED -> card.isOwned
                CollectionTab.DUPLICATES -> card.quantity > 1
                CollectionTab.WISHLIST -> card.isWishlist
            }

            // Filtro por Búsqueda (Nombre o ID)
            val matchesQuery = query.isBlank() ||
                card.name.contains(query, ignoreCase = true) ||
                card.id.contains(query, ignoreCase = true)

            // Filtro por Expansión
            val matchesExpansion = expansion == "ALL" || card.expansion.equals(expansion, ignoreCase = true)

            // Filtro por Tipo de Energía
            val matchesEnergy = energy == "ALL" || card.energyType.equals(energy, ignoreCase = true)

            // Filtro por Rareza
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
            isImporting = isImporting,
            importSummary = importSummary,
            errorMessage = errorMsg,
            showImportSheet = showSheet
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
        // Toggle si se vuelve a presionar el mismo
        _selectedEnergy.value = if (_selectedEnergy.value == energy && energy != "ALL") "ALL" else energy
    }

    fun onRaritySelected(rarity: String) {
        // Toggle si se vuelve a presionar la misma rareza
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

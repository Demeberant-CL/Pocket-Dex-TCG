package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CardDetailBottomSheet
import com.example.ui.components.CollectionFilterHeader
import com.example.ui.components.DeckBuilderSection
import com.example.ui.components.ImportCsvDialog
import com.example.ui.components.ImportResultDialog
import com.example.ui.components.PocketCardGridItem
import com.example.ui.components.PocketDashboardHeader
import com.example.ui.components.TradeZoneSection
import com.example.ui.viewmodel.CardViewModel
import com.example.ui.viewmodel.CollectionTab

/**
 * Pantalla principal de la aplicación Pokémon TCG Pocket:
 * - Pestañas: Catálogo, En Álbum, Duplicados (Trade), Deseadas (Wishlist) y Deck Builder IA.
 * - Cuadrícula reactiva LazyVerticalGrid con tarjetas estilizadas.
 * - Barra superior con filtros reactivos por tipo y rareza vía StateFlow.
 * - Módulo 3: Asistente Deck Builder IA optimizado para 20 cartas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    viewModel: CardViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Pocket Dex TCG",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.resetCatalogToDefaults() },
                        modifier = Modifier.testTag("reset_catalog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restablecer catálogo inicial",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setShowImportSheet(true) },
                        modifier = Modifier.testTag("open_import_csv_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileOpen,
                            contentDescription = "Importar CSV",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (state.selectedTab != CollectionTab.DECK_BUILDER) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.setShowImportSheet(true) },
                    icon = { Icon(Icons.Default.FileOpen, contentDescription = null) },
                    text = { Text("Importar CSV", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("fab_import_csv")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Pestañas principales
            PrimaryTabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth()
            ) {
                CollectionTab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.onTabSelected(tab) },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}"),
                        text = {
                            Text(
                                text = when (tab) {
                                    CollectionTab.ALL -> "Catálogo (${state.totalCardsInCatalog})"
                                    CollectionTab.OWNED -> "Álbum (${state.uniqueOwnedCount})"
                                    CollectionTab.DUPLICATES -> "Trades (${state.duplicateCardsCount})"
                                    CollectionTab.WISHLIST -> "Wishlist (${state.wishlistCount})"
                                    CollectionTab.DECK_BUILDER -> "✨ Deck IA"
                                },
                                fontWeight = if (state.selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            // Vista condicional: Deck Builder IA, Zona de Trades, o Cuadrícula de Colección
            when (state.selectedTab) {
                CollectionTab.DECK_BUILDER -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        DeckBuilderSection(
                            generatedDeck = state.generatedDeck,
                            savedDecks = state.savedDecks,
                            isGenerating = state.isGeneratingDeck,
                            selectedStrategy = state.selectedDeckStrategy,
                            onStrategySelected = viewModel::onDeckStrategyChanged,
                            selectedEnergy = state.selectedDeckEnergy,
                            onEnergySelected = viewModel::onDeckEnergyChanged,
                            onGenerateDeck = { viewModel.generateDeck(state.selectedDeckStrategy, state.selectedDeckEnergy) },
                            onSaveDeck = viewModel::saveCurrentDeck,
                            onDeleteSavedDeck = viewModel::deleteSavedDeck,
                            onSelectCardForDetail = viewModel::selectCardForDetail,
                            noticeMessage = state.deckNoticeMessage,
                            onDismissNotice = viewModel::clearDeckNotice
                        )
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
                CollectionTab.DUPLICATES -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        TradeZoneSection(
                            comparisonResult = state.tradeComparison,
                            onFriendWishlistInputChanged = viewModel::onFriendWishlistInputChanged,
                            currentWishlistInput = state.friendWishlistInput,
                            onApplyPreset = viewModel::applyFriendTradePreset,
                            onSelectCardForDetail = viewModel::selectCardForDetail
                        )
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
                else -> {
                    // Cuadrícula reactiva LazyVerticalGrid con tarjetas estilizadas
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(state.gridColumns),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("cards_vertical_grid"),
                        contentPadding = PaddingValues(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                    // 1. Cabecera: Métricas del Dashboard (ocupa todo el ancho del grid)
                    item(span = { GridItemSpan(state.gridColumns) }) {
                        PocketDashboardHeader(state = state)
                    }

                    // 2. Barra Superior con Filtros Reactivos por Tipo y Rareza usando StateFlow
                    item(span = { GridItemSpan(state.gridColumns) }) {
                        CollectionFilterHeader(
                            searchQuery = state.searchQuery,
                            onSearchChanged = viewModel::onSearchQueryChanged,
                            selectedEnergy = state.selectedEnergy,
                            onEnergySelected = viewModel::onEnergySelected,
                            selectedRarity = state.selectedRarity,
                            onRaritySelected = viewModel::onRaritySelected,
                            currentSort = state.sortOption,
                            onSortSelected = viewModel::onSortSelected,
                            gridColumns = state.gridColumns,
                            onToggleGridColumns = viewModel::toggleGridColumns,
                            hasActiveFilters = state.hasActiveFilters,
                            onClearFilters = viewModel::clearAllFilters
                        )
                    }

                    // 3. Indicador de resultados
                    item(span = { GridItemSpan(state.gridColumns) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${state.cards.size} cartas encontradas",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (state.hasActiveFilters) {
                                Text(
                                    text = "Filtros activos",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // 4. Cartas en la Cuadrícula estilizada
                    if (state.cards.isEmpty()) {
                        item(span = { GridItemSpan(state.gridColumns) }) {
                            EmptyStateView(
                                tab = state.selectedTab,
                                hasFilter = state.hasActiveFilters,
                                onClearFilters = viewModel::clearAllFilters
                            )
                        }
                    } else {
                        items(
                            items = state.cards,
                            key = { it.id }
                        ) { card ->
                            PocketCardGridItem(
                                card = card,
                                onClick = { viewModel.selectCardForDetail(card) },
                                onIncrement = { viewModel.incrementQuantity(card) },
                                onDecrement = { viewModel.decrementQuantity(card) },
                                onToggleWishlist = { viewModel.toggleWishlist(card) },
                                isCompact = state.gridColumns == 3
                            )
                        }
                    }

                    // Espacio inferior para no tapar con el FloatingActionButton
                    item(span = { GridItemSpan(state.gridColumns) }) {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}

    // Modal de Inspección Detallada de Carta
    CardDetailBottomSheet(
        card = state.selectedCardForDetail,
        onDismiss = { viewModel.selectCardForDetail(null) },
        onIncrement = { state.selectedCardForDetail?.let { viewModel.incrementQuantity(it) } },
        onDecrement = { state.selectedCardForDetail?.let { viewModel.decrementQuantity(it) } },
        onToggleOwned = { state.selectedCardForDetail?.let { viewModel.toggleOwned(it) } },
        onToggleWishlist = { state.selectedCardForDetail?.let { viewModel.toggleWishlist(it) } }
    )

    // Modal de Importación CSV
    ImportCsvDialog(
        isOpen = state.showImportSheet,
        isImporting = state.isImporting,
        onDismiss = { viewModel.setShowImportSheet(false) },
        onImportUri = { uri, strategy -> viewModel.importCsvFromUri(uri, strategy) },
        onImportSample = { strategy -> viewModel.importSampleCsv(strategy) }
    )

    // Diálogo de Resumen de Importación
    ImportResultDialog(
        summary = state.importSummary,
        errorMessage = state.errorMessage,
        onDismiss = { viewModel.dismissSummary() }
    )
}

@Composable
private fun EmptyStateView(
    tab: CollectionTab,
    hasFilter: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = when {
                    hasFilter -> "No hay cartas con los filtros seleccionados"
                    tab == CollectionTab.OWNED -> "Aún no tienes cartas marcadas como obtenidas"
                    tab == CollectionTab.DUPLICATES -> "¡No tienes cartas duplicadas para intercambio!"
                    tab == CollectionTab.WISHLIST -> "Tu lista de deseos está vacía"
                    else -> "No hay cartas en el catálogo"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when {
                    hasFilter -> "Prueba cambiando el tipo de energía, la rareza o la búsqueda."
                    tab == CollectionTab.OWNED -> "Usa los botones '+' en las cartas de la cuadrícula o importa tu colección desde un archivo CSV."
                    tab == CollectionTab.DUPLICATES -> "Cuando obtengas 2 o más copias de una misma carta en los sobres, aparecerán aquí para gestionar trades."
                    tab == CollectionTab.WISHLIST -> "Toca el corazón en cualquier tarjeta para guardarla en tu Wishlist."
                    else -> "Pulsa en 'Importar CSV' o restablece el catálogo inicial."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (hasFilter) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onClearFilters,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Restablecer Filtros")
                }
            }
        }
    }
}

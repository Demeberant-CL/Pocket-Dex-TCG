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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.SearchOff
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CardItemView
import com.example.ui.components.ImportCsvDialog
import com.example.ui.components.ImportResultDialog
import com.example.ui.components.PocketDashboardHeader
import com.example.ui.components.PocketFilterBar
import com.example.ui.viewmodel.CardViewModel
import com.example.ui.viewmodel.CollectionTab

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
                            fontWeight = FontWeight.Bold
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
            ExtendedFloatingActionButton(
                onClick = { viewModel.setShowImportSheet(true) },
                icon = { Icon(Icons.Default.FileOpen, contentDescription = null) },
                text = { Text("Importar CSV", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_import_csv")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Pestañas principales de colección
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
                                    CollectionTab.OWNED -> "En Álbum (${state.uniqueOwnedCount})"
                                    CollectionTab.DUPLICATES -> "Trades (${state.duplicateCardsCount})"
                                    CollectionTab.WISHLIST -> "Wishlist (${state.wishlistCount})"
                                },
                                fontWeight = if (state.selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("cards_lazy_column"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cabecera: Métricas del Dashboard
                item {
                    PocketDashboardHeader(state = state)
                }

                // Filtros y Búsqueda
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    PocketFilterBar(
                        searchQuery = state.searchQuery,
                        onSearchChanged = viewModel::onSearchQueryChanged,
                        selectedExpansion = state.selectedExpansion,
                        onExpansionSelected = viewModel::onExpansionSelected,
                        selectedEnergy = state.selectedEnergy,
                        onEnergySelected = viewModel::onEnergySelected,
                        currentSort = state.sortOption,
                        onSortSelected = viewModel::onSortSelected
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Cantidad de resultados mostrados
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mostrando ${state.cards.size} cartas",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (state.selectedExpansion != "ALL" || state.selectedEnergy != "ALL" || state.searchQuery.isNotEmpty()) {
                            Text(
                                text = "Filtros aplicados",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Lista de cartas
                if (state.cards.isEmpty()) {
                    item {
                        EmptyStateView(
                            tab = state.selectedTab,
                            hasFilter = state.searchQuery.isNotEmpty() || state.selectedExpansion != "ALL" || state.selectedEnergy != "ALL"
                        )
                    }
                } else {
                    items(
                        items = state.cards,
                        key = { it.id }
                    ) { card ->
                        CardItemView(
                            card = card,
                            onIncrement = { viewModel.incrementQuantity(card) },
                            onDecrement = { viewModel.decrementQuantity(card) },
                            onToggleOwned = { viewModel.toggleOwned(card) },
                            onToggleWishlist = { viewModel.toggleWishlist(card) }
                        )
                    }
                }

                // Margen inferior para que el FAB no tape el último elemento
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }

    // Modal de Importación CSV
    ImportCsvDialog(
        isOpen = state.showImportSheet,
        isImporting = state.isImporting,
        onDismiss = { viewModel.setShowImportSheet(false) },
        onImportUri = { uri, strategy -> viewModel.importCsvFromUri(uri, strategy) },
        onImportSample = { strategy -> viewModel.importSampleCsv(strategy) }
    )

    // Diálogo con resumen de importación / errores
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
                    hasFilter -> "No hay cartas que coincidan con los filtros"
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
                    hasFilter -> "Prueba ajustando la búsqueda, la expansión o el tipo de energía seleccionado."
                    tab == CollectionTab.OWNED -> "Usa los botones '+' en las cartas del catálogo o importa tu colección desde un archivo CSV."
                    tab == CollectionTab.DUPLICATES -> "Cuando consigas 2 o más copias de una misma carta en los sobres, aparecerán aquí para gestionar trades."
                    tab == CollectionTab.WISHLIST -> "Pulsa el icono de corazón en cualquier carta del catálogo para guardarla en tu Wishlist."
                    else -> "Pulsa en 'Importar CSV' o restablece el catálogo con el botón superior."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

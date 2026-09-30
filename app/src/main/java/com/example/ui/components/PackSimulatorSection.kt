package com.example.ui.components

import android.Manifest
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CardEntity
import com.example.data.local.entity.PocketEnergyType
import com.example.data.local.entity.PocketRarity
import com.example.data.simulator.OpenedPackResult
import com.example.notification.PackNotificationScheduler

/**
 * Pantalla interactiva del Simulador de Apertura de Sobres de Pokémon TCG Pocket (Módulo 5).
 * Implementa:
 * - Apertura de sobres de 5 cartas con tasas oficiales del juego (incluyendo 0.05% de God Pack).
 * - Guardado directo de las cartas obtenidas en la base de datos Room.
 * - Programación con WorkManager para notificaciones locales de recarga gratuita de sobres (12 horas).
 */
@Composable
fun PackSimulatorSection(
    openedPack: OpenedPackResult?,
    isOpening: Boolean,
    onOpenPack: (packName: String) -> Unit,
    onSaveToCollection: (List<CardEntity>) -> Unit,
    onSelectCardForDetail: (CardEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPackName by remember { mutableStateOf("Charizard") }
    var showOddsDialog by remember { mutableStateOf(false) }
    var isPackSaved by remember { mutableStateOf(false) }

    // Launcher para permiso de notificaciones en Android 13+ (POST_NOTIFICATIONS)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            PackNotificationScheduler.schedule12HourRecharge(context)
            Toast.makeText(context, "¡Notificación de recarga (12h) programada!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Permiso de notificaciones denegado.", Toast.LENGTH_SHORT).show()
        }
    }

    val packsList = listOf(
        Pair("Charizard", "🔥 Sobre Charizard"),
        Pair("Pikachu", "⚡ Sobre Pikachu"),
        Pair("Mewtwo", "🔮 Sobre Mewtwo"),
        Pair("Mew", "✨ Isla Mítica (Mew)")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Cabecera del Simulador de Sobres
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(listOf(Color(0xFFFFB800), Color(0xFFEF4444), Color(0xFF8B5CF6)))
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFFFFB800), Color(0xFFEF4444)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Apertura de Sobres",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Probabilidades Reales • Pokémon TCG Pocket",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showOddsDialog = true },
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tasas %", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selector de Sobre
                Text(
                    text = "SELECCIONA TU SOBRE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    packsList.forEach { (code, label) ->
                        val isSelected = selectedPackName == code
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPackName = code },
                            label = { Text(label) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Botón Abrir Sobre
                Button(
                    onClick = {
                        isPackSaved = false
                        onOpenPack(selectedPackName)
                    },
                    enabled = !isOpening,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("open_pack_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (selectedPackName) {
                            "Charizard" -> Color(0xFFDC2626)
                            "Pikachu" -> Color(0xFFD97706)
                            "Mewtwo" -> Color(0xFF7C3AED)
                            else -> Color(0xFF0284C7)
                        }
                    )
                ) {
                    if (isOpening) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Desgarrando sobre digital...")
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Abrir Sobre de 5 Cartas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // 2. Resultado del Sobre Abierto (5 Cartas)
        if (openedPack != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(
                    if (openedPack.isGodPack) 2.5.dp else 1.2.dp,
                    if (openedPack.isGodPack) Brush.sweepGradient(
                        listOf(Color(0xFFFFD700), Color(0xFFFFB300), Color(0xFFEC4899), Color(0xFFFFD700))
                    ) else Brush.linearGradient(
                        listOf(Color(0xFF38BDF8), Color(0xFF8B5CF6))
                    )
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Si es un God Pack
                    if (openedPack.isGodPack) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFD700).copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("👑", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "¡SOBRE RARO ÉPICO (GOD PACK)! (0.05%)",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFFD700)
                                    )
                                    Text(
                                        text = "¡Todas las 5 cartas son de rareza rara (☆ a 👑)! Eres 1 en 2000.",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    // Cabecera del resultado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sobre de ${openedPack.packName} (5 Cartas)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        RarityBadge(rarityStr = openedPack.highestRarity.name)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Despliegue de las 5 cartas
                    openedPack.cards.forEachIndexed { index, card ->
                        OpenedCardItem(
                            card = card,
                            slotNumber = index + 1,
                            onClick = { onSelectCardForDetail(card) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón para guardar en la base de datos local
                    Button(
                        onClick = {
                            onSaveToCollection(openedPack.cards)
                            isPackSaved = true
                            Toast.makeText(context, "¡5 cartas añadidas a tu inventario Room!", Toast.LENGTH_SHORT).show()
                        },
                        enabled = !isPackSaved,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPackSaved) Color(0xFF059669) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isPackSaved) Icons.Default.Check else Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPackSaved) "¡Cartas Guardadas en tu Álbum!" else "Añadir Cartas a mi Colección",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Tarjeta de Programación de Notificaciones con WorkManager
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Recarga de Sobres (12 Horas)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Notificación push local con WorkManager",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "En Pokémon TCG Pocket se recarga 1 sobre gratuito cada 12 horas. Programa una alerta de WorkManager para no perder energía de apertura.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón para probar notificación rápida en 5 segundos
                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                PackNotificationScheduler.scheduleQuickTestNotification(context, 5)
                                Toast.makeText(context, "Notificación programada para dentro de 5 segundos...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Probar (5s)", fontSize = 11.sp)
                    }

                    // Botón para programar ciclo real de 12 horas
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                PackNotificationScheduler.schedule12HourRecharge(context)
                                Toast.makeText(context, "¡Notificación oficial programada para dentro de 12 horas!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Activar Alerta 12h", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Diálogo de Probabilidades Oficiales
    if (showOddsDialog) {
        AlertDialog(
            onDismissRequest = { showOddsDialog = false },
            title = { Text("Tasas Oficiales Pokémon TCG Pocket", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "• God Pack (Sobre Raro): 0.05% (1 entre 2000)\n\n" +
                                "• Ranuras 1 a 3:\n  - 1 Diamante (♢): 100.00%\n\n" +
                                "• Ranura 4:\n" +
                                "  - 2 Diamantes (♢♢): 90.00%\n" +
                                "  - 3 Diamantes (♢♢♢): 5.00%\n" +
                                "  - 4 Diamantes (♢♢♢♢ ex): 1.666%\n" +
                                "  - 1 Estrella (☆ AR): 2.572%\n" +
                                "  - 2 Estrellas (☆☆ SR): 0.500%\n" +
                                "  - 3 Estrellas (☆☆☆ Inmersiva): 0.222%\n" +
                                "  - Corona Dorada (👑): 0.040%\n\n" +
                                "• Ranura 5 (Alta Rareza):\n" +
                                "  - 2 Diamantes (♢♢): 60.00%\n" +
                                "  - 3 Diamantes (♢♢♢): 20.00%\n" +
                                "  - 4 Diamantes (♢♢♢♢ ex): 6.664%\n" +
                                "  - 1 Estrella (☆ AR): 10.288%\n" +
                                "  - 2 Estrellas (☆☆ SR): 2.000%\n" +
                                "  - 3 Estrellas (☆☆☆ Inmersiva): 0.888%\n" +
                                "  - Corona Dorada (👑): 0.160%",
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showOddsDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
private fun OpenedCardItem(
    card: CardEntity,
    slotNumber: Int,
    onClick: () -> Unit
) {
    val energy = PocketEnergyType.fromString(card.energyType)
    val rarity = PocketRarity.fromString(card.rarity)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.8f),
        border = androidx.compose.foundation.BorderStroke(
            if (rarity.tierLevel >= PocketRarity.FOUR_DIAMONDS.tierLevel) 1.2.dp else 0.5.dp,
            if (rarity == PocketRarity.CROWN) Color(0xFFFFD700)
            else if (rarity == PocketRarity.THREE_STARS) Color(0xFFEC4899)
            else if (rarity == PocketRarity.FOUR_DIAMONDS) Color(0xFF00F0FF)
            else Color.White.copy(alpha = 0.15f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Ranura
                Text(
                    text = "#$slotNumber",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = energy.emoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = card.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Text(
                        text = "${card.expansion} • ${card.id}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            RarityBadge(rarityStr = card.rarity)
        }
    }
}

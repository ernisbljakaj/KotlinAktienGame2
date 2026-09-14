package com.example.kotlinaktiengame.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kotlinaktiengame.model.DepotPosition

@Composable
fun DepotScreen(
    depot: List<DepotPosition>,
    onVerkaufen: (String, Int) -> Unit
) {
    var selectedPosition by remember { mutableStateOf<DepotPosition?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    if (depot.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(32.dp)) {
                Text("Dein Depot ist leer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Kaufe Aktien im Aktienmarkt, um sie hier zu sehen.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        val gesamtWert = depot.sumOf { it.aktuellerWert }
        val gesamtGewinn = depot.sumOf { it.gewinnVerlust }

        Column(modifier = Modifier.fillMaxSize()) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Positionen", style = MaterialTheme.typography.titleSmall)
                        Text("${depot.size} Aktien", style = MaterialTheme.typography.labelMedium)
                    }
                    Divider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gesamtwert")
                        Text("%.2f CHF".format(gesamtWert), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gesamt Gewinn/Verlust")
                        val c = when {
                            gesamtGewinn > 0 -> Color(0xFF2E7D32)
                            gesamtGewinn < 0 -> Color(0xFFC62828)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        Text("%+.2f CHF".format(gesamtGewinn), color = c, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(depot) { pos ->
                    DepotCard(position = pos, onVerkaufenClick = {
                        selectedPosition = pos
                        showDialog = true
                    })
                }
            }
        }
    }

    if (showDialog && selectedPosition != null) {
        VerkaufDialog(
            position = selectedPosition!!,
            onDismiss = { showDialog = false },
            onConfirm = { anzahl ->
                onVerkaufen(selectedPosition!!.kuerzel, anzahl)
                showDialog = false
            }
        )
    }
}

@Composable
private fun DepotCard(position: DepotPosition, onVerkaufenClick: () -> Unit) {
    val isGewinn = position.gewinnVerlust >= 0
    val farbe = if (isGewinn) Color(0xFF2E7D32) else Color(0xFFC62828)

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(position.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(position.kuerzel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Card(colors = CardDefaults.cardColors(containerColor = farbe.copy(alpha = 0.15f))) {
                    Text(
                        text = "%+.2f %%".format(position.gewinnVerlustProzent),
                        color = farbe,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Divider()
            DepotRow("Anzahl", "${position.anzahl} Stück")
            DepotRow("Kaufpreis", "%.2f CHF".format(position.kaufpreis))
            DepotRow("Aktueller Preis", "%.2f CHF".format(position.aktuellerKurs))
            Divider()
            DepotRow("Aktueller Wert", "%.2f CHF".format(position.aktuellerWert), bold = true)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Gewinn / Verlust", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "%+.2f CHF".format(position.gewinnVerlust),
                    fontWeight = FontWeight.Bold,
                    color = farbe
                )
            }
            Button(onClick = onVerkaufenClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Verkaufen")
            }
        }
    }
}

@Composable
private fun DepotRow(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun VerkaufDialog(
    position: DepotPosition,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var anzahlText by remember { mutableStateOf("1") }
    val anzahl = anzahlText.toIntOrNull() ?: 0
    val erloes = position.aktuellerKurs * anzahl
    val kannVerkaufen = anzahl > 0 && anzahl <= position.anzahl

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aktie verkaufen: ${position.kuerzel}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${position.name} – Bestand: ${position.anzahl} Stück")
                Text("Aktueller Kurs: %.2f CHF".format(position.aktuellerKurs), style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = anzahlText,
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 5) anzahlText = it },
                    label = { Text("Anzahl (max. ${position.anzahl})") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (anzahl > 0) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Text("Erlös: %.2f CHF".format(erloes), fontWeight = FontWeight.Bold)
                            if (!kannVerkaufen) {
                                Text("Zu viele Aktien! Max: ${position.anzahl}", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, position.anzahl / 2, position.anzahl).distinct().filter { it > 0 }.take(3).forEach { v ->
                        AssistChip(onClick = { anzahlText = v.toString() }, label = { Text(if (v == position.anzahl) "Alle ($v)" else "$v") })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(anzahl) }, enabled = kannVerkaufen) { Text("Verkaufen") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}

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
import androidx.compose.ui.unit.sp
import com.example.kotlinaktiengame.model.Aktie

@Composable
fun MarketScreen(
    aktien: List<Aktie>,
    guthaben: Double,
    onKaufen: (Aktie, Int) -> Unit
) {
    var selectedAktie by remember { mutableStateOf<Aktie?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // Header-Text nur bei Änderung neu formatieren
    val guthabenText = remember(guthaben) { "%.2f CHF".format(guthaben) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Verfügbares Guthaben", style = MaterialTheme.typography.titleSmall)
                Text(guthabenText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = aktien, key = { it.kuerzel }) { aktie ->
                // Key + stabile Lambda -> RecyclerView-artiges Recycling, weniger Lag im Emulator
                AktieMarketCard(
                    aktie = aktie,
                    onKaufenClick = {
                        selectedAktie = aktie
                        showDialog = true
                    }
                )
            }
        }
    }

    if (showDialog && selectedAktie != null) {
        KaufDialog(
            aktie = selectedAktie!!,
            guthaben = guthaben,
            onDismiss = { showDialog = false },
            onConfirm = { anzahl ->
                onKaufen(selectedAktie!!, anzahl)
                showDialog = false
            }
        )
    }
}

@Composable
private fun AktieMarketCard(aktie: Aktie, onKaufenClick: () -> Unit) {
    val isPositiv = aktie.veraenderung >= 0
    val farbe = if (isPositiv) Color(0xFF2E7D32) else Color(0xFFC62828)
    // Strings cachen
    val kursText = remember(aktie.kurs) { "%.2f CHF".format(aktie.kurs) }
    val veraenderungText = remember(aktie.veraenderung) { "%+.2f %%".format(aktie.veraenderung) }
    val pfeil = if (isPositiv) "▲" else "▼"

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(aktie.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(aktie.kuerzel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Card(colors = CardDefaults.cardColors(containerColor = farbe.copy(alpha = 0.15f))) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(pfeil, color = farbe, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(veraenderungText, color = farbe, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Aktueller Kurs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(kursText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Button(onClick = onKaufenClick) { Text("Kaufen") }
            }
        }
    }
}

@Composable
private fun KaufDialog(
    aktie: Aktie,
    guthaben: Double,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var anzahlText by remember { mutableStateOf("1") }
    val anzahl = anzahlText.toIntOrNull() ?: 0
    val gesamtpreis = remember(aktie.kurs, anzahl) { aktie.kurs * anzahl }
    val kannKaufen = anzahl > 0 && gesamtpreis <= guthaben
    val maxAnzahl = remember(guthaben, aktie.kurs) { (guthaben / aktie.kurs).toInt() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kaufen: ${aktie.kuerzel}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${aktie.name} – %.2f CHF / Aktie".format(aktie.kurs))
                Text("Guthaben: %.2f CHF (max. $maxAnzahl)".format(guthaben), style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = anzahlText,
                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 5) anzahlText = it },
                    label = { Text("Anzahl") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (anzahl > 0) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Text("Gesamt: %.2f CHF".format(gesamtpreis), fontWeight = FontWeight.Bold)
                            Text("Danach: %.2f CHF".format(guthaben - gesamtpreis), style = MaterialTheme.typography.bodySmall)
                            if (!kannKaufen) Text("Nicht genug Guthaben!", color = Color.Red, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 5, 10, maxAnzahl).distinct().filter { it in 1..50 }.take(4).forEach { v ->
                        AssistChip(onClick = { anzahlText = v.toString() }, label = { Text("$v") })
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(anzahl) }, enabled = kannKaufen) { Text("Kaufen") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

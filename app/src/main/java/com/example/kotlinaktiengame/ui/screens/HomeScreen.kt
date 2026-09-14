package com.example.kotlinaktiengame.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    guthaben: Double,
    depotWert: Double,
    gesamtGewinn: Double,
    gesamtVermoegen: Double,
    onNavigateMarkt: () -> Unit,
    onNavigateDepot: () -> Unit,
    onReset: () -> Unit
) {
    // Strings werden 1x berechnet, nicht bei jedem Recomposition neu formatiert
    val guthabenText = remember(guthaben) { "%.2f CHF".format(guthaben) }
    val depotWertText = remember(depotWert) { "%.2f CHF".format(depotWert) }
    val vermoegenText = remember(gesamtVermoegen) { "%.2f CHF".format(gesamtVermoegen) }
    val gewinnText = remember(gesamtGewinn) { "%+.2f CHF".format(gesamtGewinn) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Kotlin Aktien Game",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Starte mit 10'000 CHF – kaufe & verkaufe Aktien!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Guthaben Card – keine Icons, nur Text (spart Icon-Font Ladezeit im Emulator)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⬢", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Aktuelles Guthaben", style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = guthabenText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Verfügbar zum Investieren", style = MaterialTheme.typography.bodySmall)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Depot Übersicht", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                HorizontalDivider()
                OverviewRow("Gesamtwert Depot", depotWertText)
                OverviewRow("Gesamtvermögen", vermoegenText, bold = true)
                HorizontalDivider()
                val gewinnColor = when {
                    gesamtGewinn > 0 -> Color(0xFF2E7D32)
                    gesamtGewinn < 0 -> Color(0xFFC62828)
                    else -> MaterialTheme.colorScheme.onSurface
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Gesamt Gewinn / Verlust", style = MaterialTheme.typography.bodyMedium)
                    Text(gewinnText, fontWeight = FontWeight.Bold, color = gewinnColor)
                }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Beispiel:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("Startkapital: 10'000 CHF", style = MaterialTheme.typography.bodySmall)
                        Text("Aktienkurs: 100 CHF → 10 Aktien kaufen = 1'000 CHF", style = MaterialTheme.typography.bodySmall)
                        Text("Verbleibendes Guthaben: 9'000 CHF", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Button(onClick = onNavigateMarkt, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("▦  Zum Aktienmarkt", fontSize = 16.sp)
        }

        OutlinedButton(onClick = onNavigateDepot, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("⬢  Zum eigenen Depot", fontSize = 16.sp)
        }

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("↺  Spiel zurücksetzen")
        }

        Text(
            text = "Hinweis: Kurse alle 4s ±2.5% – optimiert für Emulator (60fps). Lokal gespeichert.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OverviewRow(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

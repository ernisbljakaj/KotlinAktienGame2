package com.example.kotlinaktiengame.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

        // Guthaben Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Aktuelles Guthaben", style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = "%.2f CHF".format(guthaben),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Verfügbar zum Investieren",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Depot Übersicht Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Depot Übersicht", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Divider()
                OverviewRow("Gesamtwert Depot", "%.2f CHF".format(depotWert))
                OverviewRow("Gesamtvermögen", "%.2f CHF".format(gesamtVermoegen), bold = true)
                Divider()
                val gewinnColor = when {
                    gesamtGewinn > 0 -> Color(0xFF2E7D32)
                    gesamtGewinn < 0 -> Color(0xFFC62828)
                    else -> MaterialTheme.colorScheme.onSurface
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Gesamt Gewinn / Verlust", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "%+.2f CHF".format(gesamtGewinn),
                        fontWeight = FontWeight.Bold,
                        color = gewinnColor
                    )
                }
                // Beispiel aus Aufgabenstellung
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

        // Navigation Buttons
        Button(
            onClick = onNavigateMarkt,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Filled.ShoppingCart, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Zum Aktienmarkt", fontSize = 16.sp)
        }

        OutlinedButton(
            onClick = onNavigateDepot,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Zum eigenen Depot", fontSize = 16.sp)
        }

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Spiel zurücksetzen")
        }

        Text(
            text = "Hinweis: Kurse ändern sich alle 3,5 Sekunden automatisch (±3%). Alle Daten werden lokal gespeichert.",
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

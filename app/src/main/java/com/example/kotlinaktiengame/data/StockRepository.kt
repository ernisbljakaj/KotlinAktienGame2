package com.example.kotlinaktiengame.data

import com.example.kotlinaktiengame.model.Aktie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

object StockRepository {
    private val initialStocks = listOf(
        Aktie("Apple Inc.", "AAPL", 185.20),
        Aktie("Tesla Inc.", "TSLA", 242.50),
        Aktie("Nestlé S.A.", "NESN", 92.40),
        Aktie("Novartis AG", "NOVN", 88.10),
        Aktie("Roche Holding", "ROG", 210.75),
        Aktie("UBS Group", "UBSG", 27.80),
        Aktie("Swisscom AG", "SCMN", 512.00),
        Aktie("Logitech Intl", "LOGN", 68.30)
    )

    private val _aktien = MutableStateFlow(initialStocks)
    val aktien: StateFlow<List<Aktie>> = _aktien

    fun getAktie(kuerzel: String): Aktie? = _aktien.value.find { it.kuerzel == kuerzel }

    fun updateKurs(kuerzel: String, neuerKurs: Double) {
        _aktien.update { list ->
            list.map {
                if (it.kuerzel == kuerzel) {
                    val veraenderung = ((neuerKurs - it.startKurs) / it.startKurs * 100)
                    it.copy(kurs = neuerKurs, veraenderung = veraenderung)
                } else it
            }
        }
    }

    // Simuliert Kursbewegungen: zufällige Änderung -3% bis +3%
    fun simulateKursAenderung() {
        _aktien.update { list ->
            list.map { aktie ->
                val changePercent = Random.nextDouble(-0.03, 0.03)
                val neuerKurs = (aktie.kurs * (1 + changePercent)).coerceAtLeast(1.0)
                // runde auf 2 Dezimalstellen
                val gerundet = (neuerKurs * 100).toInt() / 100.0
                val veraenderung = ((gerundet - aktie.startKurs) / aktie.startKurs * 100)
                aktie.copy(kurs = gerundet, veraenderung = veraenderung)
            }
        }
    }

    fun restorePrices(prices: Map<String, Double>) {
        _aktien.update { list ->
            list.map { aktie ->
                prices[aktie.kuerzel]?.let { k ->
                    val veraenderung = ((k - aktie.startKurs) / aktie.startKurs * 100)
                    aktie.copy(kurs = k, veraenderung = veraenderung)
                } ?: aktie
            }
        }
    }
}

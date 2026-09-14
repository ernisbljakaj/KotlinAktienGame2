package com.example.kotlinaktiengame.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class DepotPosition(
    val name: String,
    val kuerzel: String,
    val anzahl: Int,
    val kaufpreis: Double, // durchschnittlicher Kaufpreis pro Aktie
    val aktuellerKurs: Double = kaufpreis
) {
    val gesamtKaufwert: Double get() = anzahl * kaufpreis
    val aktuellerWert: Double get() = anzahl * aktuellerKurs
    val gewinnVerlust: Double get() = aktuellerWert - gesamtKaufwert
    val gewinnVerlustProzent: Double get() = if (kaufpreis == 0.0) 0.0 else ((aktuellerKurs - kaufpreis) / kaufpreis * 100)
}

fun DepotPosition.formatGewinn(): String {
    val sign = if (gewinnVerlust >= 0) "+" else ""
    return "$sign%.2f CHF (%.2f %%)".format(gewinnVerlust, gewinnVerlustProzent)
}

package com.example.kotlinaktiengame.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Aktie(
    val name: String,
    val kuerzel: String,
    val kurs: Double,
    val veraenderung: Double = 0.0, // in Prozent
    val startKurs: Double = kurs
)

fun Aktie.formatKurs(): String = "%.2f CHF".format(kurs)

fun Aktie.formatVeraenderung(): String {
    val sign = if (veraenderung >= 0) "+" else ""
    return "$sign%.2f %%".format(veraenderung)
}

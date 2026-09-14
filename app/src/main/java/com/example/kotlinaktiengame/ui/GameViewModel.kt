package com.example.kotlinaktiengame.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinaktiengame.data.GameRepository
import com.example.kotlinaktiengame.data.StockRepository
import com.example.kotlinaktiengame.model.Aktie
import com.example.kotlinaktiengame.model.DepotPosition
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)

    private val _guthaben = MutableStateFlow(GameRepository.STARTKAPITAL)
    val guthaben: StateFlow<Double> = _guthaben

    private val _depot = MutableStateFlow<List<DepotPosition>>(emptyList())
    val depot: StateFlow<List<DepotPosition>> = _depot

    val aktien: StateFlow<List<Aktie>> = StockRepository.aktien

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    // Abgeleitete Werte
    val depotGesamtwert: StateFlow<Double> = combine(_depot, aktien) { depot, _ -> depot.sumOf { it.aktuellerWert } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val depotKaufwert: StateFlow<Double> = _depot.map { depot -> depot.sumOf { it.gesamtKaufwert } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val gesamtGewinnVerlust: StateFlow<Double> = combine(depotGesamtwert, depotKaufwert) { wert, kauf -> wert - kauf }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val gesamtVermoegen: StateFlow<Double> = combine(_guthaben, depotGesamtwert) { guthaben, depotWert -> guthaben + depotWert }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameRepository.STARTKAPITAL)

    private var kursJob: Job? = null

    init {
        viewModelScope.launch {
            // Lade gespeicherte Daten
            repository.guthabenFlow.collect { _guthaben.value = it }
        }
        viewModelScope.launch {
            repository.depotFlow.collect { _depot.value = it }
        }
        viewModelScope.launch {
            repository.kurseFlow.collect { kurse ->
                if (kurse.isNotEmpty()) {
                    StockRepository.restorePrices(kurse)
                    // Depot aktuelle Kurse synchronisieren
                    syncDepotKurse()
                }
            }
        }

        // Immer wenn sich Aktienkurse ändern, Depot aktualisieren
        viewModelScope.launch {
            aktien.collect {
                syncDepotKurse()
                // Kurse persistieren
                val map = it.associate { a -> a.kuerzel to a.kurs }
                repository.saveKurse(map)
            }
        }

        starteKursSimulation()
    }

    private fun syncDepotKurse() {
        val kurse = StockRepository.aktien.value.associateBy { it.kuerzel }
        _depot.value = _depot.value.map { pos ->
            val aktuellerKurs = kurse[pos.kuerzel]?.kurs ?: pos.aktuellerKurs
            pos.copy(aktuellerKurs = aktuellerKurs)
        }
    }

    private fun starteKursSimulation() {
        kursJob?.cancel()
        kursJob = viewModelScope.launch {
            while (true) {
                delay(3500)
                StockRepository.simulateKursAenderung()
            }
        }
    }

    fun kaufeAktie(aktie: Aktie, anzahl: Int) {
        if (anzahl <= 0) {
            _message.value = "Ungültige Anzahl"
            return
        }
        val kosten = aktie.kurs * anzahl
        if (kosten > _guthaben.value) {
            _message.value = "Nicht genug Guthaben! Benötigt: %.2f CHF, Guthaben: %.2f CHF".format(kosten, _guthaben.value)
            return
        }
        val neuesGuthaben = _guthaben.value - kosten
        _guthaben.value = neuesGuthaben

        val bestehende = _depot.value.find { it.kuerzel == aktie.kuerzel }
        val neuesDepot = if (bestehende != null) {
            val neueAnzahl = bestehende.anzahl + anzahl
            val neuerKaufpreis = (bestehende.kaufpreis * bestehende.anzahl + aktie.kurs * anzahl) / neueAnzahl
            _depot.value.map {
                if (it.kuerzel == aktie.kuerzel) it.copy(
                    anzahl = neueAnzahl,
                    kaufpreis = neuerKaufpreis,
                    aktuellerKurs = aktie.kurs
                ) else it
            }
        } else {
            _depot.value + DepotPosition(
                name = aktie.name,
                kuerzel = aktie.kuerzel,
                anzahl = anzahl,
                kaufpreis = aktie.kurs,
                aktuellerKurs = aktie.kurs
            )
        }
        _depot.value = neuesDepot
        _message.value = "$anzahl x ${aktie.kuerzel} gekauft für %.2f CHF".format(kosten)

        viewModelScope.launch {
            repository.saveGuthaben(neuesGuthaben)
            repository.saveDepot(neuesDepot)
        }
    }

    fun verkaufeAktie(kuerzel: String, anzahl: Int) {
        val position = _depot.value.find { it.kuerzel == kuerzel }
        if (position == null) {
            _message.value = "Position nicht gefunden"
            return
        }
        if (anzahl <= 0 || anzahl > position.anzahl) {
            _message.value = "Ungültige Anzahl (max. ${position.anzahl})"
            return
        }
        val aktuellerKurs = StockRepository.getAktie(kuerzel)?.kurs ?: position.aktuellerKurs
        val erloes = aktuellerKurs * anzahl
        val neuesGuthaben = _guthaben.value + erloes
        _guthaben.value = neuesGuthaben

        val neuesDepot = if (anzahl == position.anzahl) {
            _depot.value.filter { it.kuerzel != kuerzel }
        } else {
            _depot.value.map {
                if (it.kuerzel == kuerzel) it.copy(anzahl = it.anzahl - anzahl, aktuellerKurs = aktuellerKurs) else it
            }
        }
        _depot.value = neuesDepot
        _message.value = "$anzahl x $kuerzel verkauft für %.2f CHF".format(erloes)

        viewModelScope.launch {
            repository.saveGuthaben(neuesGuthaben)
            repository.saveDepot(neuesDepot)
        }
    }

    fun resetSpiel() {
        viewModelScope.launch {
            repository.reset()
            _guthaben.value = GameRepository.STARTKAPITAL
            _depot.value = emptyList()
            // Kurse zurücksetzen auf Start
            StockRepository.aktien.value.forEach {
                StockRepository.updateKurs(it.kuerzel, it.startKurs)
            }
            _message.value = "Spiel zurückgesetzt – 10'000 CHF Startkapital"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

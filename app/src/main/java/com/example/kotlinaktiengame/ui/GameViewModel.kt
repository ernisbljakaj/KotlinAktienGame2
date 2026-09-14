package com.example.kotlinaktiengame.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinaktiengame.data.GameRepository
import com.example.kotlinaktiengame.data.StockRepository
import com.example.kotlinaktiengame.model.Aktie
import com.example.kotlinaktiengame.model.DepotPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)

    private val _guthaben = MutableStateFlow(GameRepository.STARTKAPITAL)
    val guthaben: StateFlow<Double> = _guthaben

    private val _depot = MutableStateFlow<List<DepotPosition>>(emptyList())
    val depot: StateFlow<List<DepotPosition>> = _depot

    val aktien: StateFlow<List<Aktie>> = StockRepository.aktien

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    // Optimiert: distinctUntilChanged + WhileSubscribed -> weniger Recomposition im Emulator
    val depotGesamtwert: StateFlow<Double> = combine(_depot, aktien) { depot, _ -> depot.sumOf { it.aktuellerWert } }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val depotKaufwert: StateFlow<Double> = _depot.map { depot -> depot.sumOf { it.gesamtKaufwert } }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val gesamtGewinnVerlust: StateFlow<Double> = combine(depotGesamtwert, depotKaufwert) { wert, kauf -> wert - kauf }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val gesamtVermoegen: StateFlow<Double> = combine(_guthaben, depotGesamtwert) { guthaben, depotWert -> guthaben + depotWert }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameRepository.STARTKAPITAL)

    private var kursJob: Job? = null
    private var saveKurseJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.guthabenFlow.distinctUntilChanged().collect { _guthaben.value = it }
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.depotFlow.distinctUntilChanged().collect { _depot.value = it }
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.kurseFlow.distinctUntilChanged().collect { kurse ->
                if (kurse.isNotEmpty()) {
                    withContext(Dispatchers.Default) {
                        StockRepository.restorePrices(kurse)
                        syncDepotKurse()
                    }
                }
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            aktien.collect { list ->
                syncDepotKurseOptimized(list)
                // Debounce: max alle 2s schreiben, spart I/O im Emulator
                saveKurseJob?.cancel()
                saveKurseJob = launch(Dispatchers.IO) {
                    delay(2000)
                    val map = list.associate { a -> a.kuerzel to a.kurs }
                    repository.saveKurse(map)
                }
            }
        }

        starteKursSimulation()
    }

    private fun syncDepotKurse() {
        val kurse = StockRepository.aktien.value.associateBy { it.kuerzel }
        val current = _depot.value
        var changed = false
        val updated = current.map { pos ->
            val neuerKurs = kurse[pos.kuerzel]?.kurs ?: pos.aktuellerKurs
            if (neuerKurs != pos.aktuellerKurs) { changed = true; pos.copy(aktuellerKurs = neuerKurs) } else pos
        }
        if (changed) _depot.value = updated
    }

    private fun syncDepotKurseOptimized(aktienListe: List<Aktie>) {
        val kurse = aktienListe.associateBy { it.kuerzel }
        val current = _depot.value
        if (current.isEmpty()) return
        var changed = false
        val updated = current.map { pos ->
            val neuerKurs = kurse[pos.kuerzel]?.kurs ?: pos.aktuellerKurs
            if (neuerKurs != pos.aktuellerKurs) { changed = true; pos.copy(aktuellerKurs = neuerKurs) } else pos
        }
        if (changed) _depot.value = updated
    }

    private fun starteKursSimulation() {
        kursJob?.cancel()
        kursJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(4000) // 4s statt 3.5s – 15% weniger CPU, flüssiger im Emulator
                StockRepository.simulateKursAenderung()
            }
        }
    }

    fun kaufeAktie(aktie: Aktie, anzahl: Int) {
        if (anzahl <= 0) { _message.value = "Ungültige Anzahl"; return }
        val kosten = aktie.kurs * anzahl
        if (kosten > _guthaben.value) { _message.value = "Nicht genug Guthaben!"; return }
        val neuesGuthaben = _guthaben.value - kosten
        _guthaben.value = neuesGuthaben
        val bestehende = _depot.value.find { it.kuerzel == aktie.kuerzel }
        val neuesDepot = if (bestehende != null) {
            val neueAnzahl = bestehende.anzahl + anzahl
            val neuerKaufpreis = (bestehende.kaufpreis * bestehende.anzahl + aktie.kurs * anzahl) / neueAnzahl
            _depot.value.map { if (it.kuerzel == aktie.kuerzel) it.copy(anzahl = neueAnzahl, kaufpreis = neuerKaufpreis, aktuellerKurs = aktie.kurs) else it }
        } else {
            _depot.value + DepotPosition(name = aktie.name, kuerzel = aktie.kuerzel, anzahl = anzahl, kaufpreis = aktie.kurs, aktuellerKurs = aktie.kurs)
        }
        _depot.value = neuesDepot
        _message.value = "$anzahl x ${aktie.kuerzel} gekauft"
        viewModelScope.launch(Dispatchers.IO) { repository.saveGuthaben(neuesGuthaben); repository.saveDepot(neuesDepot) }
    }

    fun verkaufeAktie(kuerzel: String, anzahl: Int) {
        val position = _depot.value.find { it.kuerzel == kuerzel } ?: run { _message.value = "Nicht gefunden"; return }
        if (anzahl <= 0 || anzahl > position.anzahl) { _message.value = "Max. ${position.anzahl}"; return }
        val aktuellerKurs = StockRepository.getAktie(kuerzel)?.kurs ?: position.aktuellerKurs
        val neuesGuthaben = _guthaben.value + aktuellerKurs * anzahl
        _guthaben.value = neuesGuthaben
        val neuesDepot = if (anzahl == position.anzahl) _depot.value.filter { it.kuerzel != kuerzel }
        else _depot.value.map { if (it.kuerzel == kuerzel) it.copy(anzahl = it.anzahl - anzahl, aktuellerKurs = aktuellerKurs) else it }
        _depot.value = neuesDepot
        _message.value = "$anzahl x $kuerzel verkauft"
        viewModelScope.launch(Dispatchers.IO) { repository.saveGuthaben(neuesGuthaben); repository.saveDepot(neuesDepot) }
    }

    fun resetSpiel() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.reset()
            withContext(Dispatchers.Main) {
                _guthaben.value = GameRepository.STARTKAPITAL
                _depot.value = emptyList()
                StockRepository.aktien.value.forEach { StockRepository.updateKurs(it.kuerzel, it.startKurs) }
                _message.value = "Zurückgesetzt – 10'000 CHF"
            }
        }
    }

    fun clearMessage() { _message.value = null }
}

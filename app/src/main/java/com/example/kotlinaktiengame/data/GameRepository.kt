package com.example.kotlinaktiengame.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.kotlinaktiengame.model.DepotPosition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "aktien_game_prefs")

class GameRepository(private val context: Context) {

    companion object {
        private val KEY_GUTHABEN = doublePreferencesKey("guthaben")
        private val KEY_DEPOT = stringPreferencesKey("depot_json")
        private val KEY_KURSE = stringPreferencesKey("kurse_json")
        const val STARTKAPITAL = 10000.0
    }

    private val json = Json { ignoreUnknownKeys = true }

    val guthabenFlow: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[KEY_GUTHABEN] ?: STARTKAPITAL
    }

    val depotFlow: Flow<List<DepotPosition>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_DEPOT] ?: "[]"
        try {
            json.decodeFromString<List<DepotPosition>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    val kurseFlow: Flow<Map<String, Double>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_KURSE] ?: "{}"
        try {
            json.decodeFromString<Map<String, Double>>(raw)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    suspend fun saveGuthaben(betrag: Double) {
        context.dataStore.edit { it[KEY_GUTHABEN] = betrag }
    }

    suspend fun saveDepot(depot: List<DepotPosition>) {
        val encoded = json.encodeToString(depot)
        context.dataStore.edit { it[KEY_DEPOT] = encoded }
    }

    suspend fun saveKurse(kurse: Map<String, Double>) {
        val encoded = json.encodeToString(kurse)
        context.dataStore.edit { it[KEY_KURSE] = encoded }
    }

    suspend fun reset() {
        context.dataStore.edit {
            it[KEY_GUTHABEN] = STARTKAPITAL
            it[KEY_DEPOT] = "[]"
            it[KEY_KURSE] = "{}"
        }
    }
}

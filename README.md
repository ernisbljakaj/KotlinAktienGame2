# Kotlin Aktien Game

Handy-App in Kotlin (Jetpack Compose) – Aktien-Simulation für Android.

## Features (geplant gemäss Aufgabenstellung)

- **Startkapital 10'000 CHF**, virtuelles Guthaben
- **Anzeige**: aktuelles Guthaben, Gesamtwert Depot, Gesamt Gewinn/Verlust
- **Aktienmarkt**: Name, Kürzel, aktueller Kurs, Veränderung (%), Kaufen-Button
- **Depot**: Name, Anzahl, Kaufpreis, aktueller Preis, aktueller Wert, Gewinn/Verlust, Verkaufen-Button
- **Kauf/Verkauf**: Mengen-Dialog, Guthaben-Prüfung, Preis = Kurs * Anzahl
- **Kurs-Simulation**: alle 3.5s ±3% zufällig, Gewinn/Verlust live
- **Lokale Speicherung**: DataStore (guthaben, depot, kurse) – bleibt nach Neustart
- **Beispiel**: 10 Aktien à 100 CHF → 1'000 CHF bezahlt, Guthaben 9'000 CHF

## Bildschirme

1. **Startbildschirm** – Übersicht Guthaben/Depot/Gewinn + Buttons zu Markt & Depot
2. **Aktienmarkt** – 8 Aktien (AAPL, TSLA, NESN, NOVN, ROG, UBSG, SCMN, LOGN) mit Kaufen-Dialog
3. **Depot** – Eigene Positionen mit Verkaufen-Dialog

## Technik

- Kotlin 2.1.0, Compose BOM 2026.05.01, Material3
- Navigation Compose, ViewModel, DataStore Preferences, Kotlinx Serialization
- Package: `com.example.kotlinaktiengame`

## Starten

1. In Android Studio öffnen: `AndroidStudioProjects/KotlinAktienGame`
2. Sync Gradle, dann Run auf Emulator/Gerät (minSdk 24)

## Reset

Auf Startbildschirm → "Spiel zurücksetzen" setzt Guthaben auf 10'000 CHF und leert Depot.
